package ic2.core.model;

import ic2.core.block.state.ISkippableProperty;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.util.ResourceLocation;

public class ModelUtil {
   private static final DefaultStateMapper defaultStateMapper = new DefaultStateMapper();
   private static final DefaultStateMapper skippingStateMapper = new DefaultStateMapper() {
      public String getPropertyString(Map<IProperty<?>, Comparable<?>> values) {
         StringBuilder propString = new StringBuilder();

         for (Entry<IProperty<?>, Comparable<?>> entry : values.entrySet()) {
            IProperty<?> prop = entry.getKey();
            if (!(prop instanceof ISkippableProperty)) {
               if (propString.length() != 0) {
                  propString.append(',');
               }

               propString.append(prop.getName());
               propString.append('=');
               propString.append(this.getPropertyName(prop, entry.getValue()));
            }
         }

         return propString.length() == 0 ? "normal" : propString.toString();
      }

      private <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> value) {
         return property.getName(value);
      }
   };

   public static ModelResourceLocation getModelLocation(ResourceLocation loc, IBlockState state) {
      return new ModelResourceLocation(loc, getVariant(state));
   }

   public static ModelResourceLocation getTEBlockModelLocation(ResourceLocation loc, IBlockState state) {
      return new ModelResourceLocation(loc, skippingStateMapper.getPropertyString(state.getProperties()));
   }

   public static String getVariant(IBlockState state) {
      return defaultStateMapper.getPropertyString(state.getProperties());
   }

   public static IBakedModel getMissingModel() {
      return getModelManager().getMissingModel();
   }

   public static IBakedModel getModel(ModelResourceLocation loc) {
      return getModelManager().getModel(loc);
   }

   public static IBakedModel getBlockModel(IBlockState state) {
      return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
   }

   private static ModelManager getModelManager() {
      return Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager();
   }
}
