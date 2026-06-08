package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityFluidDistributor;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerFluidDistributor extends ContainerFullInv<TileEntityFluidDistributor>
{
	public ContainerFluidDistributor(int syncId, Inventory playerInventory, TileEntityFluidDistributor be)
	{
		super(Ic2ScreenHandlers.FLUID_DISTRIBUTOR, syncId, playerInventory, be, 184);
		this.addSlot(new SlotInvSlot(be.inputSlot, 0, 9, 54));
		this.addSlot(new SlotInvSlot(be.OutputSlot, 0, 9, 72));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("fluidTank");
		return ret;
	}
}
