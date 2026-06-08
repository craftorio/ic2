package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityFermenter;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerFermenter extends ContainerFullInv<TileEntityFermenter>
{
	public ContainerFermenter(int syncId, Inventory playerInventory, TileEntityFermenter te)
	{
		super(Ic2ScreenHandlers.FERMENTER, syncId, playerInventory, te, 184);
		this.addSlot(new SlotInvSlot(te.fluidInputCellInSlot, 0, 14, 46));
		this.addSlot(new SlotInvSlot(te.fluidInputCellOutSlot, 0, 14, 64));
		this.addSlot(new SlotInvSlot(te.fluidOutputCellInSlot, 0, 148, 43));
		this.addSlot(new SlotInvSlot(te.fluidOutputCellOutSlot, 0, 148, 61));
		this.addSlot(new SlotInvSlot(te.fertiliserSlot, 0, 86, 83));

		for (int i = 0; i < 2; i++)
		{
			this.addSlot(new SlotInvSlot(te.upgradeSlot, i, 125 + i * 18, 83));
		}
	}

	@Override
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
