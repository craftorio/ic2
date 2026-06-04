// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.kineticgenerator.container;

import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.kineticgenerator.tileentity.TileEntityElectricKineticGenerator;
import ic2.core.ContainerFullInv;

public class ContainerElectricKineticGenerator extends ContainerFullInv<TileEntityElectricKineticGenerator>
{
    public ContainerElectricKineticGenerator(final EntityPlayer player, final TileEntityElectricKineticGenerator tileEntity1) {
        super(player, (IInventory)tileEntity1, 166);
        for (int i = 0; i < 5; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.slotMotor, i, 44 + i * 18, 27));
        }
        for (int i = 5; i < 10; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.slotMotor, i, 44 + (i - 5) * 18, 45));
        }
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.dischargeSlot, 0, 8, 62));
    }
}
