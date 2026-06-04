// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;

public abstract class ContainerElectricMachine<T extends TileEntityElectricMachine> extends ContainerFullInv<T>
{
    public ContainerElectricMachine(final EntityPlayer player, final T base1, final int height, final int dischargeX, final int dischargeY) {
        super(player, (IInventory)base1, height);
        this.addSlotToContainer((Slot)new SlotInvSlot(base1.dischargeSlot, 0, dischargeX, dischargeY));
    }
}
