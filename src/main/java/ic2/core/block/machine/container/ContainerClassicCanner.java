package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityClassicCanner;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerClassicCanner extends ContainerElectricMachine<TileEntityClassicCanner>
{
	public ContainerClassicCanner(int syncId, Inventory playerInventory, TileEntityClassicCanner base)
	{
		super(Ic2ScreenHandlers.CLASSIC_CANNER, syncId, playerInventory, base, 166, 30, 45);
		this.m_38897_(new SlotInvSlot(base.resInputSlot, 0, 69, 17));
		this.m_38897_(new SlotInvSlot(base.outputSlot, 0, 119, 35));
		this.m_38897_(new SlotInvSlot(base.inputSlot, 0, 69, 53));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("progress");
		ret.add("mode");
		return ret;
	}
}
