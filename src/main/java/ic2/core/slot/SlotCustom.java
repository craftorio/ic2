// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.slot;

import net.minecraft.item.ItemStack;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.inventory.Slot;

public class SlotCustom extends Slot
{
    private final Item item;
    
    public SlotCustom(final IInventory iinventory, final Item item, final int i, final int j, final int k) {
        super(iinventory, i, j, k);
        this.item = item;
    }
    
    public boolean isItemValid(final ItemStack itemstack) {
        return itemstack != null && this.item != null && itemstack.getItem() == this.item;
    }
}
