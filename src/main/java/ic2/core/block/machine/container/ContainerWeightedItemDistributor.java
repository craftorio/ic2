package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityWeightedItemDistributor;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerWeightedItemDistributor extends ContainerFullInv<TileEntityWeightedItemDistributor>
{
	public static final short HEIGHT = 211;

	public ContainerWeightedItemDistributor(EntityPlayer player, TileEntityWeightedItemDistributor te)
	{
		super(player, te, 211);

		for (int i = 0; i < te.buffer.size(); i++)
		{
			this.addSlotToContainer(new SlotInvSlot(te.buffer, i, 8 + i * 18, 108));
		}
	}
}
