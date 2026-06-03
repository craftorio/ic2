package ic2.core.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

public abstract class AbstractModel implements IReloadableModel, IBakedModel {
  public Collection<ResourceLocation> getDependencies() {
    return Collections.emptyList();
  }
  
  public Collection<ResourceLocation> getTextures() {
    return Collections.emptyList();
  }
  
  public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
    return this;
  }
  
  public IModelState getDefaultState() {
    return (IModelState)TRSRTransformation.identity();
  }
  
  public List<BakedQuad> func_188616_a(IBlockState state, EnumFacing side, long rand) {
    throw new UnsupportedOperationException();
  }
  
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
    return Minecraft.func_71410_x().func_147117_R().func_174944_f();
  }
  
  public ItemCameraTransforms func_177552_f() {
    return ItemCameraTransforms.field_178357_a;
  }
  
  public ItemOverrideList func_188617_f() {
    return ItemOverrideList.field_188022_a;
  }
}
