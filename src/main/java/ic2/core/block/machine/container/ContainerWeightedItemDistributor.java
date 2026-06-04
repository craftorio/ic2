// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import net.minecraft.inventory.Slot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityWeightedItemDistributor;
import ic2.core.ContainerFullInv;

public class ContainerWeightedItemDistributor extends ContainerFullInv<TileEntityWeightedItemDistributor>
{
    public static final short HEIGHT = 211;
    
    public ContainerWeightedItemDistributor(final EntityPlayer player, final TileEntityWeightedItemDistributor te) {
        super(player, (IInventory)te, 211);
        for (int i = 0; i < te.buffer.size(); ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(te.buffer, i, 8 + i * 18, 108));
        }
    }
}
