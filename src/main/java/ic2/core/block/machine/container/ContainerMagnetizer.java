package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityMagnetizer;
import ic2.core.slot.SlotArmor;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;

public class ContainerMagnetizer extends ContainerElectricMachine<TileEntityMagnetizer>
{
	public final EntityPlayer player;

	public ContainerMagnetizer(EntityPlayer player, TileEntityMagnetizer base1)
	{
		super(player, base1, 166, 8, 44);
		this.player = player;

		for (int i = 0; i < 4; i++)
		{
			this.addSlotToContainer(new SlotInvSlot(base1.upgradeSlot, i, 152, 8 + i * 18));
		}

		this.addSlotToContainer(new SlotArmor(player.inventory, EntityEquipmentSlot.FEET, 45, 26));
	}
}
