package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityItemBuffer;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerItemBuffer extends ContainerFullInv<TileEntityItemBuffer>
{
	public ContainerItemBuffer(int syncId, Inventory playerInventory, TileEntityItemBuffer te)
	{
		super(Ic2ScreenHandlers.ITEM_BUFFER, syncId, playerInventory, te, 232);

		for (int y = 0; y < te.leftcontentSlot.size() / 4; y++)
		{
			for (int x = 0; x < 4; x++)
			{
				this.m_38897_(new SlotInvSlot(te.leftcontentSlot, x + y * 4, 8 + x * 18, 18 + y * 18));
			}
		}

		for (int y = 0; y < te.rightcontentSlot.size() / 4; y++)
		{
			for (int x = 0; x < 4; x++)
			{
				this.m_38897_(new SlotInvSlot(te.rightcontentSlot, x + y * 4, 98 + x * 18, 18 + y * 18));
			}
		}

		for (int i = 0; i < 2; i++)
		{
			this.m_38897_(new SlotInvSlot(te.upgradeSlot, i, 35 + i * 90, 128));
		}
	}
}
