package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityItemBuffer;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerItemBuffer extends ContainerFullInv<TileEntityItemBuffer>
{
	public ContainerItemBuffer(EntityPlayer player, TileEntityItemBuffer tileEntity)
	{
		super(player, tileEntity, 232);
		int y;
		for (y = 0; y < tileEntity.leftcontentSlot.size() / 4; y++)
		{
			for (int x = 0; x < 4; x++)
				addSlotToContainer(new SlotInvSlot(tileEntity.leftcontentSlot, x + y * 4, 8 + x * 18, 18 + y * 18));
		}
		for (y = 0; y < tileEntity.rightcontentSlot.size() / 4; y++)
		{
			for (int x = 0; x < 4; x++)
				addSlotToContainer(new SlotInvSlot(tileEntity.rightcontentSlot, x + y * 4, 98 + x * 18, 18 + y * 18));
		}
		for (int i = 0; i < 2; i++)
			addSlotToContainer(new SlotInvSlot(tileEntity.upgradeSlot, i, 35 + i * 90, 128));
	}
}
