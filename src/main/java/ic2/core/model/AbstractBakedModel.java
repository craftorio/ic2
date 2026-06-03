package ic2.core.model;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public abstract class AbstractBakedModel implements IBakedModel {
  public boolean func_177555_b() {
    return true;
  }
  
  public boolean func_177556_c() {
    return false;
  }
  
  public boolean func_188618_c() {
    return false;
  }
  
  public TextureAtlasSprite func_177554_e() {
    return null;
  }
  
  public ItemCameraTransforms func_177552_f() {
    return null;
  }
  
  public ItemOverrideList func_188617_f() {
    return null;
  }
}
