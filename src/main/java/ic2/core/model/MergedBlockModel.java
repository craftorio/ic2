// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.model;

import net.minecraft.client.renderer.block.model.ItemOverrideList;
import javax.vecmath.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.state.IBlockState;
import java.util.Iterator;
import ic2.core.util.ReflectionUtil;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;
import net.minecraft.client.renderer.block.model.BakedQuad;
import java.util.List;
import java.lang.reflect.Field;
import net.minecraft.client.renderer.block.model.IBakedModel;

public class MergedBlockModel implements IBakedModel
{
    private static final Field SPRITE;
    private static final byte[][] uvMap;
    private static final int quadVertexCount = 4;
    private static final int xDataIndex = 0;
    private static final int yDataIndex = 1;
    private static final int zDataIndex = 2;
    private static final int colorDataIndex = 3;
    private static final int uDataIndex = 4;
    private static final int vDataIndex = 5;
    private final IBakedModel parent;
    private final List<BakedQuad>[] mergedFaceQuads;
    private final int[] retextureStart;
    private static final int textureStride = 1;
    private final float[][] currentUvs;
    private final int[][] currentColorMultipliers;
    
    public MergedBlockModel(final IBakedModel parent, final List<BakedQuad>[] mergedFaceQuads, final int[] retextureStart) {
        this.currentUvs = new float[6][];
        this.currentColorMultipliers = new int[6][];
        this.parent = parent;
        this.mergedFaceQuads = mergedFaceQuads;
        this.retextureStart = retextureStart;
    }
    
    public MergedBlockModel copy() {
        final List<BakedQuad>[] newMergedQuads = new List[this.mergedFaceQuads.length];
        for (int side = 0; side < this.mergedFaceQuads.length; ++side) {
            final List<BakedQuad> mergedFaceQuads = this.mergedFaceQuads[side];
            final List<BakedQuad> newMergedFaceQuads = new ArrayList<BakedQuad>(mergedFaceQuads);
            for (int i = this.retextureStart[side]; i < mergedFaceQuads.size(); ++i) {
                final BakedQuad oldQuad = mergedFaceQuads.get(i);
                final int[] vertexData = Arrays.copyOf(oldQuad.getVertexData(), oldQuad.getVertexData().length);
                final BakedQuad newQuad = new BakedQuad(vertexData, oldQuad.getTintIndex(), oldQuad.getFace(), oldQuad.getSprite(), oldQuad.shouldApplyDiffuseLighting(), oldQuad.getFormat());
                newMergedFaceQuads.set(i, newQuad);
            }
        }
        return new MergedBlockModel(this.parent, newMergedQuads, this.retextureStart);
    }
    
    public void setSprite(final float[][] uvs, final int[][] colorMultipliers, final TextureAtlasSprite[][] sprites) {
        for (int i = 0; i < 6; ++i) {
            final boolean matchingUvs = Arrays.equals(uvs[i], this.currentUvs[i]);
            final boolean matchingColorMul = Arrays.equals(colorMultipliers[i], this.currentColorMultipliers[i]);
            if (!matchingUvs || !matchingColorMul) {
                if (!matchingUvs) {
                    this.currentUvs[i] = uvs[i];
                }
                if (!matchingColorMul) {
                    this.currentColorMultipliers[i] = colorMultipliers[i];
                }
                if (this.currentColorMultipliers[i] != null) {
                    this.setSpriteUnchecked(this.mergedFaceQuads[i], this.retextureStart[i], this.currentUvs[i], MergedBlockModel.uvMap[i / 2], this.currentColorMultipliers[i], sprites[i]);
                }
            }
        }
    }
    
    private void setSpriteUnchecked(final List<BakedQuad> quads, final int retextureStart, final float[] uvs, final byte[] uvMap, final int[] colorMultipliers, final TextureAtlasSprite[] sprites) {
        if (quads.size() - retextureStart > colorMultipliers.length) {
            throw new IllegalStateException(String.format("mismatched size/stride/multipliers: retex-quads=%d, stride=%d, muls=%d", quads.size() - retextureStart, 1, colorMultipliers.length));
        }
        if (uvs.length != colorMultipliers.length * 4) {
            throw new IllegalStateException(String.format("mismatched uvs/multipliers: uvs=%d, muls=%d", uvs.length, colorMultipliers.length));
        }
        for (int texture = 0; texture < colorMultipliers.length; ++texture) {
            final float uS = uvs[texture * 4];
            final float vS = uvs[texture * 4 + 1];
            final float uE = uvs[texture * 4 + 2];
            final float vE = uvs[texture * 4 + 3];
            final float du = uE - uS;
            final float dv = vE - vS;
            final int colorMultiplier = mapColor(colorMultipliers[texture]);
            final TextureAtlasSprite sprite = sprites[texture];
            for (int i = 0; i < 1; ++i) {
                final BakedQuad quad = quads.get(retextureStart + texture + i);
                final int[] vertexData = quad.getVertexData();
                for (int j = 0; j < 4; ++j) {
                    final int offset = j * VdUtil.dataStride;
                    vertexData[offset + 3] = colorMultiplier;
                    final float x = Float.intBitsToFloat(vertexData[offset + 0]);
                    final float y = Float.intBitsToFloat(vertexData[offset + 1]);
                    final float z = Float.intBitsToFloat(vertexData[offset + 2]);
                    vertexData[offset + 4] = Float.floatToRawIntBits(uS + du * (x * uvMap[0] + y * uvMap[1] + z * uvMap[2]));
                    vertexData[offset + 5] = Float.floatToRawIntBits(vS + dv * (x * uvMap[3] + y * uvMap[4] + z * uvMap[5]));
                }
            }
        }
        for (final BakedQuad quad2 : quads) {
            if (quad2.getSprite() == null) {
                ReflectionUtil.setValue(quad2, MergedBlockModel.SPRITE, ModelLoader.White.INSTANCE);
            }
        }
    }
    
    private static int mapColor(final int color) {
        final int a = color >>> 24;
        if (a > 0) {
            return (color & 0xFF00FF00) | (color & 0xFF) << 16 | (color & 0xFF0000) >> 16;
        }
        return 0xFF000000 | (color & 0xFF00) | (color & 0xFF) << 16 | (color & 0xFF0000) >> 16;
    }
    
    public List<BakedQuad> getQuads(final IBlockState state, final EnumFacing side, final long rand) {
        if (side == null) {
            return this.parent.getQuads(state, side, rand);
        }
        return this.mergedFaceQuads[side.ordinal()];
    }
    
    public boolean isAmbientOcclusion() {
        return this.parent.isAmbientOcclusion();
    }
    
    public boolean isGui3d() {
        return this.parent.isGui3d();
    }
    
    public boolean isBuiltInRenderer() {
        return this.parent.isBuiltInRenderer();
    }
    
    public TextureAtlasSprite getParticleTexture() {
        return this.parent.getParticleTexture();
    }
    
    @Deprecated
    public ItemCameraTransforms getItemCameraTransforms() {
        return this.parent.getItemCameraTransforms();
    }
    
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(final ItemCameraTransforms.TransformType cameraTransformType) {
        return (Pair<? extends IBakedModel, Matrix4f>)Pair.of((Object)this, this.parent.handlePerspective(cameraTransformType).getRight());
    }
    
    public ItemOverrideList getOverrides() {
        return this.parent.getOverrides();
    }
    
    static {
        SPRITE = ReflectionUtil.getField(BakedQuad.class, TextureAtlasSprite.class);
        uvMap = new byte[][] { { 1, 0, 0, 0, 0, 1 }, { 1, 0, 0, 0, 1, 0 }, { 0, 0, 1, 0, 1, 0 } };
    }
}
