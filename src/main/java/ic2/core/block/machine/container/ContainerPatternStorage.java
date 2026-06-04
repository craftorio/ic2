package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityPatternStorage;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerPatternStorage extends ContainerFullInv<TileEntityPatternStorage>
{
	public ContainerPatternStorage(EntityPlayer player, TileEntityPatternStorage tileEntity1)
	{
		super(player, tileEntity1, 166);
		addSlotToContainer(new SlotInvSlot(tileEntity1.diskSlot, 0, 18, 20));
	}

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
