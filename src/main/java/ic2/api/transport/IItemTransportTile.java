// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.transport;

import net.minecraft.util.EnumFacing;
import net.minecraft.item.ItemStack;

public interface IItemTransportTile extends IPipe
{
    int putItems(final ItemStack p0, final EnumFacing p1, final boolean p2);
    
    ItemStack getContents();
    
    void setContents(final ItemStack p0);
    
    int getMaxStackSizeAllowed();
    
    int getTransferRate();
}
