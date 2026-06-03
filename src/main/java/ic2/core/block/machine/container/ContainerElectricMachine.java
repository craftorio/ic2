package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public abstract class ContainerElectricMachine<T extends TileEntityElectricMachine> extends ContainerFullInv<T> {
  public ContainerElectricMachine(EntityPlayer player, T base1, int height, int dischargeX, int dischargeY) {
    super(player, (IInventory)base1, height);
    func_75146_a((Slot)new SlotInvSlot((InvSlot)((TileEntityElectricMachine)base1).dischargeSlot, 0, dischargeX, dischargeY));
  }
}
