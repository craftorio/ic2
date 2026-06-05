package ic2.core.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotRadioactive extends Slot {
   public SlotRadioactive(IInventory inventory, int index, int x, int y) {
      super(inventory, index, x, y);
   }

   public boolean isItemValid(ItemStack stack) {
      return this.inventory.isItemValidForSlot(this.slotNumber, stack);
   }
}
