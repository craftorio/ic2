package ic2.core.item.tool;

import ic2.core.model.MaskOverlayModel;
import ic2.core.model.ModelUtil;
import ic2.core.util.StackUtil;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class RenderObscurator extends MaskOverlayModel {
  public RenderObscurator() {
    super(baseModelLoc, maskTextureLoc, true, 0.001F);
    this.overrideHandler = new ItemOverrideList(Collections.emptyList()) {
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
          if (stack == null)
            return ModelUtil.getMissingModel(); 
          NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
          IBlockState state = ItemObscurator.getState(nbt);
          EnumFacing side = ItemObscurator.getSide(nbt);
          int[] colorMultipliers = ItemObscurator.getColorMultipliers(nbt);
          ItemObscurator.ObscuredRenderInfo renderInfo;
          if (state == null || side == null || (
            
            renderInfo = ItemObscurator.getRenderInfo(state, side)) == null || colorMultipliers == null || colorMultipliers.length * 4 != renderInfo.uvs.length)
            return RenderObscurator.this.get(); 
          return RenderObscurator.this.get(renderInfo.uvs, colorMultipliers);
        }
      };
  }
  
  public ItemOverrideList getOverrides() {
    return this.overrideHandler;
  }
  
  private static final ResourceLocation baseModelLoc = new ResourceLocation("ic2", "item/tool/electric/obscurator_raw");
  
  private static final ResourceLocation maskTextureLoc = new ResourceLocation("ic2", "textures/items/tool/electric/obscurator_mask.png");
  
  private final ItemOverrideList overrideHandler;
}
