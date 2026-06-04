// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.slot;

import ic2.api.item.ItemWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class SlotBoxable extends Slot
{
    public SlotBoxable(final IInventory iinventory, final int i, final int j, final int k) {
        super(iinventory, i, j, k);
    }
    
    public boolean isItemValid(final ItemStack itemstack) {
        return itemstack != null && ItemWrapper.canBeStoredInToolbox(itemstack);
    }
}
