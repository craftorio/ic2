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

		for (int i = 0; i < base.fertilizerSlot.size(); i++)
		{
			this.addSlotToContainer(new SlotInvSlot(base.fertilizerSlot, i, 8 + i * 18, 80));
		}

		this.addSlotToContainer(new SlotInvSlot(base.exInputSlot, 0, 49, 27));
		this.addSlotToContainer(new SlotInvSlot(base.exOutputSlot, 0, 67, 27));
		this.addSlotToContainer(new SlotInvSlot(base.wasserinputSlot, 0, 57, 56));
		this.addSlotToContainer(new SlotInvSlot(base.wasseroutputSlot, 0, 75, 56));

		for (int i = 0; i < 4; i++)
		{
			this.addSlotToContainer(new SlotInvSlot(base.upgradeSlot, i, 152, 26 + i * 18));
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("waterTank");
		ret.add("exTank");
		return ret;
	}
}
