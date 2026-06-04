package ic2.core.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SlotCustom extends Slot {
  private final Item item;
  
  public SlotCustom(IInventory iinventory, Item item, int i, int j, int k) {
    super(iinventory, i, j, k);
    this.item = item;
  }
  
  public boolean isItemValid(ItemStack itemstack) {
    if (itemstack == null)
      return false; 
    return (this.item != null && itemstack.getItem() == this.item);
  }
}
