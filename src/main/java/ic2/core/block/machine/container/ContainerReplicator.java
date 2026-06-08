package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityReplicator;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerReplicator extends ContainerElectricMachine<TileEntityReplicator>
{
	public ContainerReplicator(int syncId, Inventory playerInventory, TileEntityReplicator be)
	{
		super(Ic2ScreenHandlers.REPLICATOR, syncId, playerInventory, be, 184, 152, 83);
		this.addSlot(new SlotInvSlot(be.outputSlot, 0, 90, 59));
		this.addSlot(new SlotInvSlot(be.fluidSlot, 0, 8, 27));
		this.addSlot(new SlotInvSlot(be.cellSlot, 0, 8, 72));

		for (int i = 0; i < 4; i++)
		{
			this.addSlot(new SlotInvSlot(be.upgradeSlot, i, 152, 8 + i * 18));
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("fluidTank");
		ret.add("uuProcessed");
		ret.add("pattern");
		ret.add("mode");
		ret.add("index");
		ret.add("maxIndex");
		ret.add("patternUu");
		ret.add("patternEu");
		return ret;
	}
}
