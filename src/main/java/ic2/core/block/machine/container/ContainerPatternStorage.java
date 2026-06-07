package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityPatternStorage;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerPatternStorage extends ContainerFullInv<TileEntityPatternStorage>
{
	public ContainerPatternStorage(int syncId, Inventory playerInventory, TileEntityPatternStorage be)
	{
		super(Ic2ScreenHandlers.PATTERN_STORAGE, syncId, playerInventory, be, 166);
		this.m_38897_(new SlotInvSlot(be.diskSlot, 0, 18, 20));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("index");
		ret.add("maxIndex");
		ret.add("pattern");
		ret.add("patternUu");
		ret.add("patternEu");
		return ret;
	}
}
