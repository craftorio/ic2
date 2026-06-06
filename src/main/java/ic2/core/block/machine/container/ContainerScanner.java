package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityScanner;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerScanner extends ContainerElectricMachine<TileEntityScanner>
{
	public ContainerScanner(EntityPlayer player, TileEntityScanner tileEntity1)
	{
		super(player, tileEntity1, 166, 8, 43);
		this.addSlotToContainer(new SlotInvSlot(tileEntity1.inputSlot, 0, 55, 35));
		this.addSlotToContainer(new SlotInvSlot(tileEntity1.diskSlot, 0, 152, 65));
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
