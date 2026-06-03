package ic2.core.item;

import ic2.core.model.MaskOverlayModel;
import ic2.core.model.ModelUtil;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class FluidCellModel extends MaskOverlayModel {
  public FluidCellModel() {
    super(baseModelLoc, maskTextureLoc, false, -0.1F);
    this.overrideHandler = new ItemOverrideList(Collections.emptyList()) {
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
          if (stack == null)
            return ModelUtil.getMissingModel(); 
          FluidStack fs = FluidUtil.getFluidContained(stack);
          ResourceLocation spriteLoc;
          if (fs == null || (spriteLoc = fs.getFluid().getStill(fs)) == null)
            return FluidCellModel.this.get(); 
          return FluidCellModel.this.get(Minecraft.func_71410_x().func_147117_R().func_110572_b(spriteLoc.toString()), fs.getFluid().getColor(fs));
        }
      };
  }
  
  public ItemOverrideList func_188617_f() {
    return this.overrideHandler;
  }
  
  private static final ResourceLocation baseModelLoc = new ResourceLocation("ic2", "item/cell/fluid_cell_case");
  
  private static final ResourceLocation maskTextureLoc = new ResourceLocation("ic2", "textures/items/cell/fluid_cell_window.png");
  
  private final ItemOverrideList overrideHandler;
}
