package ic2.core.util;

import ic2.api.info.IInfoProvider;
import ic2.core.init.MainConfig;
import ic2.core.item.type.CraftingItemType;
import ic2.core.item.type.DustResourceType;
import ic2.core.ref.ItemName;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class ItemInfo implements IInfoProvider {
  public double getEnergyValue(ItemStack stack) {
    if (StackUtil.isEmpty(stack))
      return 0.0D; 
    if (StackUtil.checkItemEquality(stack, ItemName.single_use_battery.getItemStack()))
      return 1200.0D; 
    if (StackUtil.checkItemEquality(stack, Items.field_151137_ax))
      return 800.0D; 
    if (StackUtil.checkItemEquality(stack, ItemName.dust.getItemStack((Enum)DustResourceType.energium)))
      return 16000.0D; 
    return 0.0D;
  }
  
  public int getFuelValue(ItemStack stack, boolean allowLava) {
    if (StackUtil.isEmpty(stack))
      return 0; 
    if ((StackUtil.checkItemEquality(stack, ItemName.crafting.getItemStack((Enum)CraftingItemType.scrap)) || 
      StackUtil.checkItemEquality(stack, ItemName.crafting.getItemStack((Enum)CraftingItemType.scrap_box))) && 
      !ConfigUtil.getBool(MainConfig.get(), "misc/allowBurningScrap"))
      return 0; 
    FluidStack liquid = FluidUtil.getFluidContained(stack);
    boolean isLava = (liquid != null && liquid.amount > 0 && liquid.getFluid() == FluidRegistry.LAVA);
    if (isLava && !allowLava)
      return 0; 
    int ret = TileEntityFurnace.func_145952_a(stack);
    return isLava ? (ret / 10) : ret;
  }
}
