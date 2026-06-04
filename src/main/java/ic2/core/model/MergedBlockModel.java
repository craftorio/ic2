package ic2.core.model;

import ic2.core.util.ReflectionUtil;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.vecmath.Matrix4f;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ModelLoader;
import org.apache.commons.lang3.tuple.Pair;

public class MergedBlockModel implements IBakedModel {
  public MergedBlockModel(IBakedModel parent, List<BakedQuad>[] mergedFaceQuads, int[] retextureStart) {
    this.currentUvs = new float[6][];
    this.currentColorMultipliers = new int[6][];
    this.parent = parent;
    this.mergedFaceQuads = mergedFaceQuads;
    this.retextureStart = retextureStart;
  }
  
  public MergedBlockModel copy() {
    List[] arrayOfList = new List[this.mergedFaceQuads.length];
    for (int side = 0; side < this.mergedFaceQuads.length; side++) {
      List<BakedQuad> mergedFaceQuads = this.mergedFaceQuads[side];
      List<BakedQuad> newMergedFaceQuads = new ArrayList<>(mergedFaceQuads);
      for (int i = this.retextureStart[side]; i < mergedFaceQuads.size(); i++) {
        BakedQuad oldQuad = mergedFaceQuads.get(i);
        int[] vertexData = Arrays.copyOf(oldQuad.getVertexData(), (oldQuad.getVertexData()).length);
        BakedQuad newQuad = new BakedQuad(vertexData, oldQuad.getTintIndex(), oldQuad.getFace(), oldQuad.getSprite(), oldQuad.shouldApplyDiffuseLighting(), oldQuad.getFormat());
        newMergedFaceQuads.set(i, newQuad);
      } 
    } 
    return new MergedBlockModel(this.parent, (List<BakedQuad>[])arrayOfList, this.retextureStart);
  }
  
  public void setSprite(float[][] uvs, int[][] colorMultipliers, TextureAtlasSprite[][] sprites) {
    for (int i = 0; i < 6; i++) {
      boolean matchingUvs = Arrays.equals(uvs[i], this.currentUvs[i]);
      boolean matchingColorMul = Arrays.equals(colorMultipliers[i], this.currentColorMultipliers[i]);
      if (!matchingUvs || !matchingColorMul) {
        if (!matchingUvs)
          this.currentUvs[i] = uvs[i]; 
        if (!matchingColorMul)
          this.currentColorMultipliers[i] = colorMultipliers[i]; 
        if (this.currentColorMultipliers[i] != null)
          setSpriteUnchecked(this.mergedFaceQuads[i], this.retextureStart[i], this.currentUvs[i], uvMap[i / 2], this.currentColorMultipliers[i], sprites[i]); 
      } 
    } 
  }
  
  private void setSpriteUnchecked(List<BakedQuad> quads, int retextureStart, float[] uvs, byte[] uvMap, int[] colorMultipliers, TextureAtlasSprite[] sprites) {
    if (quads.size() - retextureStart > colorMultipliers.length)
      throw new IllegalStateException(String.format("mismatched size/stride/multipliers: retex-quads=%d, stride=%d, muls=%d", new Object[] { Integer.valueOf(quads.size() - retextureStart), Integer.valueOf(1), Integer.valueOf(colorMultipliers.length) })); 
    if (uvs.length != colorMultipliers.length * 4)
      throw new IllegalStateException(String.format("mismatched uvs/multipliers: uvs=%d, muls=%d", new Object[] { Integer.valueOf(uvs.length), Integer.valueOf(colorMultipliers.length) })); 
    for (int texture = 0; texture < colorMultipliers.length; texture++) {
      float uS = uvs[texture * 4];
      float vS = uvs[texture * 4 + 1];
      float uE = uvs[texture * 4 + 2];
      float vE = uvs[texture * 4 + 3];
      float du = uE - uS;
      float dv = vE - vS;
      int colorMultiplier = mapColor(colorMultipliers[texture]);
      TextureAtlasSprite sprite = sprites[texture];
      for (int i = 0; i < 1; i++) {
        BakedQuad quad = quads.get(retextureStart + texture + i);
        int[] vertexData = quad.getVertexData();
        for (int j = 0; j < 4; j++) {
          int offset = j * VdUtil.dataStride;
          vertexData[offset + 3] = colorMultiplier;
          float x = Float.intBitsToFloat(vertexData[offset + 0]);
          float y = Float.intBitsToFloat(vertexData[offset + 1]);
          float z = Float.intBitsToFloat(vertexData[offset + 2]);
          vertexData[offset + 4] = Float.floatToRawIntBits(uS + du * (x * uvMap[0] + y * uvMap[1] + z * uvMap[2]));
          vertexData[offset + 5] = Float.floatToRawIntBits(vS + dv * (x * uvMap[3] + y * uvMap[4] + z * uvMap[5]));
        } 
      } 
    } 
    for (BakedQuad quad : quads) {
      if (quad.getSprite() == null)
        ReflectionUtil.setValue(quad, SPRITE, ModelLoader.White.INSTANCE); 
    } 
  }
  
  private static final Field SPRITE = ReflectionUtil.getField(BakedQuad.class, TextureAtlasSprite.class);
  
  private static final byte[][] uvMap = new byte[][] { { 1, 0, 0, 0, 0, 1 }, { 1, 0, 0, 0, 1, 0 }, { 0, 0, 1, 0, 1, 0 } };
  
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
  
  private static int mapColor(int color) {
    int a = color >>> 24;
    if (a > 0)
      return color & 0xFF00FF00 | (color & 0xFF) << 16 | (color & 0xFF0000) >> 16; 
    return 0xFF000000 | color & 0xFF00 | (color & 0xFF) << 16 | (color & 0xFF0000) >> 16;
  }
  
  public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
    if (side == null)
      return this.parent.getQuads(state, side, rand); 
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
  
  public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
    return Pair.of(this, this.parent.handlePerspective(cameraTransformType).getRight());
  }
  
  public ItemOverrideList getOverrides() {
    return this.parent.getOverrides();
  }
}
