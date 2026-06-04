package ic2.core.slot;

import ic2.api.info.Info;
import ic2.api.item.ElectricItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotDischarge extends Slot {
  public int tier = Integer.MAX_VALUE;
  
  public SlotDischarge(IInventory par1iInventory, int tier1, int par2, int par3, int par4) {
    super(par1iInventory, par2, par3, par4);
    this.tier = tier1;
  }
  
  public SlotDischarge(IInventory par1iInventory, int par2, int par3, int par4) {
    super(par1iInventory, par2, par3, par4);
  }
  
  public boolean isItemValid(ItemStack stack) {
    if (stack == null)
      return false; 
    if (Info.itemInfo.getEnergyValue(stack) > 0.0D)
      return true; 
    return (ElectricItem.manager.discharge(stack, Double.POSITIVE_INFINITY, this.tier, true, true, true) > 0.0D);
  }
}
