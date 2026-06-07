package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityScanner;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerScanner extends ContainerElectricMachine<TileEntityScanner>
{
	public ContainerScanner(int syncId, Inventory playerInventory, TileEntityScanner be)
	{
		super(Ic2ScreenHandlers.UU_SCANNER, syncId, playerInventory, be, 166, 8, 43);
		this.m_38897_(new SlotInvSlot(be.inputSlot, 0, 55, 35));
		this.m_38897_(new SlotInvSlot(be.diskSlot, 0, 152, 65));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("state");
		ret.add("progress");
		ret.add("patternEu");
		ret.add("patternUu");
		return ret;
	}
}
