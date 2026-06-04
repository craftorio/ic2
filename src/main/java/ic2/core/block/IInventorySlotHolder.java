// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import ic2.core.block.invslot.InvSlot;
import net.minecraft.inventory.IInventory;

public interface IInventorySlotHolder<P extends TileEntityBlock & IInventory>
{
    P getParent();
    
    InvSlot getInventorySlot(final String p0);
    
    void addInventorySlot(final InvSlot p0);
    
    int getBaseIndex(final InvSlot p0);
}
