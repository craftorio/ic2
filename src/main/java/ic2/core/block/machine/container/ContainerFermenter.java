package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityFermenter;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerFermenter extends ContainerFullInv<TileEntityFermenter>
{
	public ContainerFermenter(EntityPlayer player, TileEntityFermenter te)
	{
		super(player, te, 184);
		addSlotToContainer(new SlotInvSlot(te.fluidInputCellInSlot, 0, 14, 46));
		addSlotToContainer(new SlotInvSlot(te.fluidInputCellOutSlot, 0, 14, 64));
		addSlotToContainer(new SlotInvSlot(te.fluidOutputCellInSlot, 0, 148, 43));
		addSlotToContainer(new SlotInvSlot(te.fluidOutputCellOutSlot, 0, 148, 61));
		addSlotToContainer(new SlotInvSlot(te.fertiliserSlot, 0, 86, 83));
		for (int i = 0; i < 2; i++)
			addSlotToContainer(new SlotInvSlot(te.upgradeSlot, i, 125 + i * 18, 83));
	}

	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("inputTank");
		ret.add("outputTank");
		ret.add("progress");
		ret.add("heatBuffer");
		return ret;
	}
}
