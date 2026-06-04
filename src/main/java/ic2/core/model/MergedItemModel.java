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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;
import net.minecraft.client.renderer.block.model.BakedQuad;
import java.util.List;
import net.minecraft.client.renderer.block.model.IBakedModel;

public class MergedItemModel implements IBakedModel
{
    private static final int xDataIndex = 0;
    private static final int yDataIndex = 1;
    private static final int colorDataIndex = 3;
    private static final int uDataIndex = 4;
    private static final int vDataIndex = 5;
    private final IBakedModel parent;
    private final List<BakedQuad> mergedQuads;
    private final int retextureStart;
    private final int textureStride;
    private float[] currentUvs;
    private int[] currentColorMultipliers;
    
    public MergedItemModel(final IBakedModel parent, final List<BakedQuad> mergedQuads, final int retextureStart, final int textureStride) {
        this.parent = parent;
        this.mergedQuads = mergedQuads;
        this.retextureStart = retextureStart;
        this.textureStride = textureStride;
    }
    
    public MergedItemModel copy() {
        final List<BakedQuad> newMergedQuads = new ArrayList<BakedQuad>(this.mergedQuads);
        for (int i = this.retextureStart; i < this.mergedQuads.size(); ++i) {
            final BakedQuad oldQuad = this.mergedQuads.get(i);
            final int[] vertexData = Arrays.copyOf(oldQuad.getVertexData(), oldQuad.getVertexData().length);
            final BakedQuad newQuad = new BakedQuad(vertexData, oldQuad.getTintIndex(), oldQuad.getFace(), oldQuad.getSprite(), oldQuad.shouldApplyDiffuseLighting(), oldQuad.getFormat());
            newMergedQuads.set(i, newQuad);
        }
        return new MergedItemModel(this.parent, newMergedQuads, this.retextureStart, this.textureStride);
    }
    
    public void setSprite(final TextureAtlasSprite sprite, final int colorMultiplier, final float uSShift, final float vSShift, final float uEShift, final float vEShift) {
        final boolean matchingUvs = this.currentUvs != null && this.currentUvs.length == 4 && this.currentUvs[0] == sprite.getMinU() && this.currentUvs[1] == sprite.getMinV() && this.currentUvs[2] == sprite.getMaxU() && this.currentUvs[3] == sprite.getMaxV();
        final boolean matchingColorMul = this.currentColorMultipliers != null && this.currentColorMultipliers[0] == colorMultiplier;
        if (!matchingUvs || !matchingColorMul) {
            if (!matchingUvs) {
                this.currentUvs = new float[] { sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV() };
            }
            if (!matchingColorMul) {
                this.currentColorMultipliers = new int[] { colorMultiplier };
            }
            this.setSpriteUnchecked(uSShift, vSShift, uEShift, vEShift);
        }
    }
    
    public void setSprite(final float[] uvs, final int[] colorMultipliers, final float uSShift, final float vSShift, final float uEShift, final float vEShift) {
        final boolean matchingUvs = Arrays.equals(uvs, this.currentUvs);
        final boolean matchingColorMul = Arrays.equals(colorMultipliers, this.currentColorMultipliers);
        if (!matchingUvs || !matchingColorMul) {
            if (!matchingUvs) {
                this.currentUvs = uvs;
            }
            if (!matchingColorMul) {
                this.currentColorMultipliers = colorMultipliers;
            }
            this.setSpriteUnchecked(uSShift, vSShift, uEShift, vEShift);
        }
    }
    
    private void setSpriteUnchecked(final float uSShift, final float vSShift, final float uEShift, final float vEShift) {
        if (this.mergedQuads.size() - this.retextureStart > this.textureStride * this.currentColorMultipliers.length) {
            throw new IllegalStateException(String.format("mismatched size/stride/multipliers: retex-quads=%d, stride=%d, muls=%d", this.mergedQuads.size() - this.retextureStart, this.textureStride, this.currentColorMultipliers.length));
        }
        if (this.currentUvs.length != this.currentColorMultipliers.length * 4) {
            throw new IllegalStateException(String.format("mismatched uvs/multipliers: uvs=%d, muls=%d", this.currentUvs.length, this.currentColorMultipliers.length));
        }
        int texture = 0;
        for (int baseIdx = this.retextureStart; baseIdx < this.mergedQuads.size(); baseIdx += this.textureStride) {
            float uS = this.currentUvs[texture * 4];
            float vS = this.currentUvs[texture * 4 + 1];
            final float uE = this.currentUvs[texture * 4 + 2];
            final float vE = this.currentUvs[texture * 4 + 3];
            float du = uE - uS;
            float dv = vE - vS;
            du /= uEShift - uSShift;
            uS -= du * uSShift;
            dv /= vEShift - vSShift;
            vS -= dv * (1.0f - vEShift);
            final int colorMultiplier = mapColor(this.currentColorMultipliers[texture]);
            for (int i = 0; i < this.textureStride; ++i) {
                final int[] vertexData = this.mergedQuads.get(baseIdx + i).getVertexData();
                for (int j = 0; j < 4; ++j) {
                    final int offset = j * VdUtil.dataStride;
                    vertexData[offset + 3] = colorMultiplier;
                    final float x = Float.intBitsToFloat(vertexData[offset + 0]);
                    final float y = Float.intBitsToFloat(vertexData[offset + 1]);
                    vertexData[offset + 4] = Float.floatToRawIntBits(uS + x * du);
                    vertexData[offset + 5] = Float.floatToRawIntBits(vS + y * dv);
                }
            }
            ++texture;
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
        if (side != null) {
            return this.parent.getQuads(state, side, rand);
        }
        return this.mergedQuads;
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
}
