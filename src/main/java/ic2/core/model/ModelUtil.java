package ic2.core.model;

import java.util.Map;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.util.ResourceLocation;

public class ModelUtil {
  public static ModelResourceLocation getModelLocation(ResourceLocation loc, IBlockState state) {
    return new ModelResourceLocation(loc, getVariant(state));
  }
  
  public static ModelResourceLocation getTEBlockModelLocation(ResourceLocation loc, IBlockState state) {
    return new ModelResourceLocation(loc, skippingStateMapper.func_178131_a((Map)state.func_177228_b()));
  }
  
  public static String getVariant(IBlockState state) {
    return defaultStateMapper.func_178131_a((Map)state.func_177228_b());
  }
  
  private static final DefaultStateMapper defaultStateMapper = new DefaultStateMapper();
  
  private static final DefaultStateMapper skippingStateMapper = new DefaultStateMapper() {
      public String func_178131_a(Map<IProperty<?>, Comparable<?>> values) {
        StringBuilder propString = new StringBuilder();
        for (Map.Entry<IProperty<?>, Comparable<?>> entry : values.entrySet()) {
          IProperty<?> prop = entry.getKey();
          if (!(prop instanceof ic2.core.block.state.ISkippableProperty)) {
            if (propString.length() != 0)
              propString.append(','); 
            propString.append(prop.func_177701_a());
            propString.append('=');
            propString.append(getPropertyName(prop, entry.getValue()));
          } 
        } 
        if (propString.length() == 0)
          return "normal"; 
        return propString.toString();
      }
      
      private <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> value) {
        return property.func_177702_a(value);
      }
    };
  
  public static IBakedModel getMissingModel() {
    return getModelManager().func_174951_a();
  }
  
  public static IBakedModel getModel(ModelResourceLocation loc) {
    return getModelManager().func_174953_a(loc);
  }
  
  public static IBakedModel getBlockModel(IBlockState state) {
    return Minecraft.getMinecraft().func_175602_ab().func_175023_a().func_178125_b(state);
  }
  
  private static ModelManager getModelManager() {
    return Minecraft.getMinecraft().func_175599_af().func_175037_a().func_178083_a();
  }
}
