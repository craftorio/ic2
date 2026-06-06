package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityItemBuffer;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerItemBuffer extends ContainerFullInv<TileEntityItemBuffer>
{
	public ContainerItemBuffer(EntityPlayer player, TileEntityItemBuffer tileEntite)
	{
		super(player, tileEntite, 232);

		for (int y = 0; y < tileEntite.leftcontentSlot.size() / 4; y++)
		{
			for (int x = 0; x < 4; x++)
			{
				this.addSlotToContainer(new SlotInvSlot(tileEntite.leftcontentSlot, x + y * 4, 8 + x * 18, 18 + y * 18));
			}
		}

		for (int y = 0; y < tileEntite.rightcontentSlot.size() / 4; y++)
		{
			for (int x = 0; x < 4; x++)
			{
				this.addSlotToContainer(new SlotInvSlot(tileEntite.rightcontentSlot, x + y * 4, 98 + x * 18, 18 + y * 18));
			}
		}

		for (int i = 0; i < 2; i++)
		{
			this.addSlotToContainer(new SlotInvSlot(tileEntite.upgradeSlot, i, 35 + i * 90, 128));
		}
	}
}
