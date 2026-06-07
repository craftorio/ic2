package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerCanner extends ContainerStandardMachine<TileEntityCanner>
{
	public ContainerCanner(int syncId, Inventory playerInventory, TileEntityCanner canner)
	{
		super(Ic2ScreenHandlers.CANNER, syncId, playerInventory, canner, 184, 8, 80, 80, 44, 119, 17, 152, 26);
		this.m_38897_(new SlotInvSlot(canner.canInputSlot, 0, 41, 17));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("mode");
		ret.add("inputTank");
		ret.add("outputTank");
		return ret;
	}
}
