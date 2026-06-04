package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityClassicCanner;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerClassicCanner extends ContainerElectricMachine<TileEntityClassicCanner>
{
	public ContainerClassicCanner(EntityPlayer player, TileEntityClassicCanner base)
	{
		super(player, base, 166, 30, 45);
		addSlotToContainer(new SlotInvSlot(base.resInputSlot, 0, 69, 17));
		addSlotToContainer(new SlotInvSlot(base.outputSlot, 0, 119, 35));
		addSlotToContainer(new SlotInvSlot(base.inputSlot, 0, 69, 53));
	}

	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("progress");
		ret.add("mode");
		return ret;
	}
}
