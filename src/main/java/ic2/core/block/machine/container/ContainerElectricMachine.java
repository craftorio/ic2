package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public abstract class ContainerElectricMachine<T extends TileEntityElectricMachine> extends ContainerFullInv<T>
{
	public ContainerElectricMachine(
		MenuType<? extends ContainerElectricMachine<T>> type, int syncId, Inventory playerInventory, T base, int height, int dischargeX, int dischargeY
	)
	{
		super(type, syncId, playerInventory, base, height);
		this.addSlot(new SlotInvSlot(base.dischargeSlot, 0, dischargeX, dischargeY));
	}
}
