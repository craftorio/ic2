// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityClassicCropmatron;
import ic2.core.ContainerFullInv;

public class ContainerClassicCropmatron extends ContainerFullInv<TileEntityClassicCropmatron>
{
    public ContainerClassicCropmatron(final EntityPlayer player, final TileEntityClassicCropmatron base) {
        super(player, (IInventory)base, 166);
        for (int i = 0; i < base.fertilizerSlot.size(); ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(base.fertilizerSlot, i, 62, 20 + i * 18));
        }
        for (int i = 0; i < base.hydrationSlot.size(); ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(base.hydrationSlot, i, 98, 20 + i * 18));
        }
        for (int i = 0; i < base.weedExSlot.size(); ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(base.weedExSlot, i, 134, 20 + i * 18));
        }
    }
}
