package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityChunkloader;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerChunkLoader extends ContainerFullInv<TileEntityChunkloader>
{
	public ContainerChunkLoader(int syncId, Inventory playerInventory, TileEntityChunkloader be)
	{
		super(Ic2ScreenHandlers.CHUNK_LOADER, syncId, playerInventory, be, 250);
		this.addSlot(new SlotInvSlot(be.dischargeSlot, 0, 8, 143));

		for (int i = 0; i < be.upgradeSlot.size(); i++)
		{
			this.addSlot(new SlotInvSlot(be.upgradeSlot, i, 8, 44 + 18 * i));
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("loadedChunks");
		return ret;
	}
}
