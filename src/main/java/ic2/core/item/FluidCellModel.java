// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidUtil;
import ic2.core.model.ModelUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.block.model.IBakedModel;
import java.util.List;
import java.util.Collections;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.util.ResourceLocation;
import ic2.core.model.MaskOverlayModel;

public class FluidCellModel extends MaskOverlayModel
{
    private static final ResourceLocation baseModelLoc;
    private static final ResourceLocation maskTextureLoc;
    private final ItemOverrideList overrideHandler;
    
    public FluidCellModel() {
        super(FluidCellModel.baseModelLoc, FluidCellModel.maskTextureLoc, false, -0.1f);
        this.overrideHandler = new ItemOverrideList((List)Collections.emptyList()) {
            public IBakedModel handleItemState(final IBakedModel originalModel, final ItemStack stack, final World world, final EntityLivingBase entity) {
                if (stack == null) {
                    return ModelUtil.getMissingModel();
                }
                final FluidStack fs = FluidUtil.getFluidContained(stack);
                final ResourceLocation spriteLoc;
                if (fs == null || (spriteLoc = fs.getFluid().getStill(fs)) == null) {
                    return MaskOverlayModel.this.get();
                }
                return MaskOverlayModel.this.get(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(spriteLoc.toString()), fs.getFluid().getColor(fs));
            }
        };
    }
    
    @Override
    public ItemOverrideList getOverrides() {
        return this.overrideHandler;
    }
    
    static {
        baseModelLoc = new ResourceLocation("ic2", "item/cell/fluid_cell_case");
        maskTextureLoc = new ResourceLocation("ic2", "textures/items/cell/fluid_cell_window.png");
    }
}
