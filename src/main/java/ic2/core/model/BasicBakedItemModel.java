package ic2.core.model;

import java.util.Collections;
import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

public class BasicBakedItemModel extends AbstractBakedModel {
  private final List<BakedQuad> quads;
  
  private final TextureAtlasSprite particleTexture;
  
  public BasicBakedItemModel(List<BakedQuad> quads, TextureAtlasSprite particleTexture) {
    this.quads = quads;
    this.particleTexture = particleTexture;
  }
  
  public List<BakedQuad> func_188616_a(IBlockState state, EnumFacing side, long rand) {
    if (side != null)
      return Collections.emptyList(); 
    return this.quads;
  }
  
  public TextureAtlasSprite func_177554_e() {
    return this.particleTexture;
  }
  
  public ItemCameraTransforms func_177552_f() {
    return ItemCameraTransforms.field_178357_a;
  }
  
  public static BakedQuad createQuad(int[] vertexData, EnumFacing side) {
    return new BakedQuad(vertexData, -1, side, null, true, VdUtil.vertexFormat);
  }
}
