// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.slot;

import net.minecraft.item.ItemStack;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class SlotRadioactive extends Slot
{
    public SlotRadioactive(final IInventory inventory, final int index, final int x, final int y) {
        super(inventory, index, x, y);
    }
    
    public boolean isItemValid(final ItemStack stack) {
        return this.inventory.isItemValidForSlot(this.slotNumber, stack);
    }
}
