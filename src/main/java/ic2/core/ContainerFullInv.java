// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public abstract class ContainerFullInv<T extends IInventory> extends ContainerBase<T>
{
    public ContainerFullInv(final EntityPlayer player, final T base, final int height) {
        super(base);
        this.addPlayerInventorySlots(player, height);
    }
    
    public ContainerFullInv(final EntityPlayer player, final T base, final int width, final int height) {
        super(base);
        this.addPlayerInventorySlots(player, width, height);
    }
}
