// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.model;

import java.util.Iterator;
import ic2.core.block.state.ISkippableProperty;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import java.util.Map;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;

public class ModelUtil
{
    private static final DefaultStateMapper defaultStateMapper;
    private static final DefaultStateMapper skippingStateMapper;
    
    public static ModelResourceLocation getModelLocation(final ResourceLocation loc, final IBlockState state) {
        return new ModelResourceLocation(loc, getVariant(state));
    }
    
    public static ModelResourceLocation getTEBlockModelLocation(final ResourceLocation loc, final IBlockState state) {
        return new ModelResourceLocation(loc, ModelUtil.skippingStateMapper.getPropertyString((Map)state.getProperties()));
    }
    
    public static String getVariant(final IBlockState state) {
        return ModelUtil.defaultStateMapper.getPropertyString((Map)state.getProperties());
    }
    
    public static IBakedModel getMissingModel() {
        return getModelManager().getMissingModel();
    }
    
    public static IBakedModel getModel(final ModelResourceLocation loc) {
        return getModelManager().getModel(loc);
    }
    
    public static IBakedModel getBlockModel(final IBlockState state) {
        return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
    }
    
    private static ModelManager getModelManager() {
        return Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager();
    }
    
    static {
        defaultStateMapper = new DefaultStateMapper();
        skippingStateMapper = new DefaultStateMapper() {
            public String getPropertyString(final Map<IProperty<?>, Comparable<?>> values) {
                final StringBuilder propString = new StringBuilder();
                for (final Map.Entry<IProperty<?>, Comparable<?>> entry : values.entrySet()) {
                    final IProperty<?> prop = entry.getKey();
                    if (!(prop instanceof ISkippableProperty)) {
                        if (propString.length() != 0) {
                            propString.append(',');
                        }
                        propString.append(prop.getName());
                        propString.append('=');
                        propString.append(this.getPropertyName(prop, entry.getValue()));
                    }
                }
                if (propString.length() == 0) {
                    return "normal";
                }
                return propString.toString();
            }
            
            private <T extends Comparable<T>> String getPropertyName(final IProperty<T> property, final Comparable<?> value) {
                return property.getName((Comparable)value);
            }
        };
    }
}
