package ic2.core.model;

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
import org.apache.commons.lang3.tuple.Pair;

public class MergedItemModel implements IBakedModel {
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
  
  public MergedItemModel(IBakedModel parent, List<BakedQuad> mergedQuads, int retextureStart, int textureStride) {
    this.parent = parent;
    this.mergedQuads = mergedQuads;
    this.retextureStart = retextureStart;
    this.textureStride = textureStride;
  }
  
  public MergedItemModel copy() {
    List<BakedQuad> newMergedQuads = new ArrayList<>(this.mergedQuads);
    for (int i = this.retextureStart; i < this.mergedQuads.size(); i++) {
      BakedQuad oldQuad = this.mergedQuads.get(i);
      int[] vertexData = Arrays.copyOf(oldQuad.func_178209_a(), (oldQuad.func_178209_a()).length);
      BakedQuad newQuad = new BakedQuad(vertexData, oldQuad.func_178211_c(), oldQuad.func_178210_d(), oldQuad.func_187508_a(), oldQuad.shouldApplyDiffuseLighting(), oldQuad.getFormat());
      newMergedQuads.set(i, newQuad);
    } 
    return new MergedItemModel(this.parent, newMergedQuads, this.retextureStart, this.textureStride);
  }
  
  public void setSprite(TextureAtlasSprite sprite, int colorMultiplier, float uSShift, float vSShift, float uEShift, float vEShift) {
    boolean matchingUvs = (this.currentUvs != null && this.currentUvs.length == 4 && this.currentUvs[0] == sprite.func_94209_e() && this.currentUvs[1] == sprite.func_94206_g() && this.currentUvs[2] == sprite.func_94212_f() && this.currentUvs[3] == sprite.func_94210_h());
    boolean matchingColorMul = (this.currentColorMultipliers != null && this.currentColorMultipliers[0] == colorMultiplier);
    if (!matchingUvs || !matchingColorMul) {
      if (!matchingUvs)
        this.currentUvs = new float[] { sprite.func_94209_e(), sprite.func_94206_g(), sprite.func_94212_f(), sprite.func_94210_h() }; 
      if (!matchingColorMul)
        this.currentColorMultipliers = new int[] { colorMultiplier }; 
      setSpriteUnchecked(uSShift, vSShift, uEShift, vEShift);
    } 
  }
  
  public void setSprite(float[] uvs, int[] colorMultipliers, float uSShift, float vSShift, float uEShift, float vEShift) {
    boolean matchingUvs = Arrays.equals(uvs, this.currentUvs);
    boolean matchingColorMul = Arrays.equals(colorMultipliers, this.currentColorMultipliers);
    if (!matchingUvs || !matchingColorMul) {
      if (!matchingUvs)
        this.currentUvs = uvs; 
      if (!matchingColorMul)
        this.currentColorMultipliers = colorMultipliers; 
      setSpriteUnchecked(uSShift, vSShift, uEShift, vEShift);
    } 
  }
  
  private void setSpriteUnchecked(float uSShift, float vSShift, float uEShift, float vEShift) {
    if (this.mergedQuads.size() - this.retextureStart > this.textureStride * this.currentColorMultipliers.length)
      throw new IllegalStateException(String.format("mismatched size/stride/multipliers: retex-quads=%d, stride=%d, muls=%d", new Object[] { Integer.valueOf(this.mergedQuads.size() - this.retextureStart), 
              Integer.valueOf(this.textureStride), 
              Integer.valueOf(this.currentColorMultipliers.length) })); 
    if (this.currentUvs.length != this.currentColorMultipliers.length * 4)
      throw new IllegalStateException(String.format("mismatched uvs/multipliers: uvs=%d, muls=%d", new Object[] { Integer.valueOf(this.currentUvs.length), 
              Integer.valueOf(this.currentColorMultipliers.length) })); 
    int baseIdx;
    for (int texture = 0; baseIdx < this.mergedQuads.size(); texture++, baseIdx += this.textureStride) {
      float uS = this.currentUvs[texture * 4];
      float vS = this.currentUvs[texture * 4 + 1];
      float uE = this.currentUvs[texture * 4 + 2];
      float vE = this.currentUvs[texture * 4 + 3];
      float du = uE - uS;
      float dv = vE - vS;
      du /= uEShift - uSShift;
      uS -= du * uSShift;
      dv /= vEShift - vSShift;
      vS -= dv * (1.0F - vEShift);
      int colorMultiplier = mapColor(this.currentColorMultipliers[texture]);
      for (int i = 0; i < this.textureStride; i++) {
        int[] vertexData = ((BakedQuad)this.mergedQuads.get(baseIdx + i)).func_178209_a();
        for (int j = 0; j < 4; j++) {
          int offset = j * VdUtil.dataStride;
          vertexData[offset + 3] = colorMultiplier;
          float x = Float.intBitsToFloat(vertexData[offset + 0]);
          float y = Float.intBitsToFloat(vertexData[offset + 1]);
          vertexData[offset + 4] = Float.floatToRawIntBits(uS + x * du);
          vertexData[offset + 5] = Float.floatToRawIntBits(vS + y * dv);
        } 
      } 
    } 
  }
  
  private static int mapColor(int color) {
    int a = color >>> 24;
    if (a > 0)
      return color & 0xFF00FF00 | (color & 0xFF) << 16 | (color & 0xFF0000) >> 16; 
    return 0xFF000000 | color & 0xFF00 | (color & 0xFF) << 16 | (color & 0xFF0000) >> 16;
  }
  
  public List<BakedQuad> func_188616_a(IBlockState state, EnumFacing side, long rand) {
    if (side != null)
      return this.parent.func_188616_a(state, side, rand); 
    return this.mergedQuads;
  }
  
  public boolean func_177555_b() {
    return this.parent.func_177555_b();
  }
  
  public boolean func_177556_c() {
    return this.parent.func_177556_c();
  }
  
  public boolean func_188618_c() {
    return this.parent.func_188618_c();
  }
  
  public TextureAtlasSprite func_177554_e() {
    return this.parent.func_177554_e();
  }
  
  @Deprecated
  public ItemCameraTransforms func_177552_f() {
    return this.parent.func_177552_f();
  }
  
  public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
    return Pair.of(this, this.parent.handlePerspective(cameraTransformType).getRight());
  }
  
  public ItemOverrideList func_188617_f() {
    return this.parent.func_188617_f();
  }
}
