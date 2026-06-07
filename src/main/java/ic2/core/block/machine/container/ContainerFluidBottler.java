package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityFluidBottler;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerFluidBottler extends ContainerStandardMachine<TileEntityFluidBottler>
{
	public ContainerFluidBottler(int syncId, Inventory playerInventory, TileEntityFluidBottler be)
	{
		super(Ic2ScreenHandlers.FLUID_BOTTLER, syncId, playerInventory, be, 184, 8, 53, 0, 0, 117, 53, 152, 26);
		this.m_38897_(new SlotInvSlot(be.drainInputSlot, 0, 44, 35));
		this.m_38897_(new SlotInvSlot(be.fillInputSlot, 0, 44, 72));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("fluidTank");
		return ret;
	}
}
