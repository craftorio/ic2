package ic2.core.slot;

import ic2.api.item.ItemWrapper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotBoxable extends Slot {
  public SlotBoxable(IInventory iinventory, int i, int j, int k) {
    super(iinventory, i, j, k);
  }
  
  public boolean isItemValid(ItemStack itemstack) {
    if (itemstack == null)
      return false; 
    return ItemWrapper.canBeStoredInToolbox(itemstack);
  }
}
