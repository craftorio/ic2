package ic2.core.block.machine.container;

import ic2.core.block.machine.tileentity.TileEntityCropmatron;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerCropmatron extends ContainerElectricMachine<TileEntityCropmatron>
{
	public ContainerCropmatron(EntityPlayer player, TileEntityCropmatron base)
	{
		super(player, base, 192, 134, 80);
		int i;
		for (i = 0; i < base.fertilizerSlot.size(); i++)
			addSlotToContainer(new SlotInvSlot(base.fertilizerSlot, i, 8 + i * 18, 80));
		addSlotToContainer(new SlotInvSlot(base.exInputSlot, 0, 49, 27));
		addSlotToContainer(new SlotInvSlot(base.exoutputSlot, 0, 67, 27));
		addSlotToContainer(new SlotInvSlot(base.wasserinputSlot, 0, 57, 56));
		addSlotToContainer(new SlotInvSlot(base.wasseroutputSlot, 0, 75, 56));
		for (i = 0; i < 4; i++)
			addSlotToContainer(new SlotInvSlot(base.upgradeSlot, i, 152, 26 + i * 18));
	}

	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("waterTank");
		ret.add("exTank");
		return ret;
	}
}
