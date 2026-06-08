package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityWeightedItemDistributor;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.world.entity.player.Inventory;

public class ContainerWeightedItemDistributor extends ContainerFullInv<TileEntityWeightedItemDistributor>
{
	public static final short HEIGHT = 211;

	public ContainerWeightedItemDistributor(int syncId, Inventory playerInventory, TileEntityWeightedItemDistributor te)
	{
		super(Ic2ScreenHandlers.WEIGHTED_ITEM_DISTRIBUTOR, syncId, playerInventory, te, 211);

		for (int i = 0; i < te.buffer.size(); i++)
		{
			this.addSlot(new SlotInvSlot(te.buffer, i, 8 + i * 18, 108));
		}
	}
}
