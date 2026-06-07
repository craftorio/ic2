package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityMagnetizer;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotArmor;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerMagnetizer extends ContainerElectricMachine<TileEntityMagnetizer>
{
	public ContainerMagnetizer(int syncId, Inventory playerInventory, TileEntityMagnetizer be)
	{
		super(Ic2ScreenHandlers.MAGNETIZER, syncId, playerInventory, be, 166, 8, 44);

		for (int i = 0; i < 4; i++)
		{
			this.m_38897_(new SlotInvSlot(be.upgradeSlot, i, 152, 8 + i * 18));
		}

		this.m_38897_(new SlotArmor(playerInventory, EquipmentSlot.FEET, 45, 26));
	}
}
