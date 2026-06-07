package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityFluidRegulator;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerFluidRegulator extends ContainerElectricMachine<TileEntityFluidRegulator>
{
	public ContainerFluidRegulator(int syncId, Inventory playerInventory, TileEntityFluidRegulator be)
	{
		super(Ic2ScreenHandlers.FLUID_REGULATOR, syncId, playerInventory, be, 184, 8, 57);
		this.m_38897_(new SlotInvSlot(be.wasserinputSlot, 0, 58, 53));
		this.m_38897_(new SlotInvSlot(be.wasseroutputSlot, 0, 58, 71));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("fluidTank");
		ret.add("outputmb");
		ret.add("mode");
		return ret;
	}
}
