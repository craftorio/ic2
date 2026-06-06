package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityMatter;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerMatter extends ContainerFullInv<TileEntityMatter>
{
	public ContainerMatter(EntityPlayer player, TileEntityMatter tileEntity1)
	{
		super(player, tileEntity1, 166);
		this.addSlotToContainer(new SlotInvSlot(tileEntity1.amplifierSlot, 0, 72, 40));
		this.addSlotToContainer(new SlotInvSlot(tileEntity1.outputSlot, 0, 125, 59));
		this.addSlotToContainer(new SlotInvSlot(tileEntity1.containerslot, 0, 125, 23));

		for (int i = 0; i < 4; i++)
		{
			this.addSlotToContainer(new SlotInvSlot(tileEntity1.upgradeSlot, i, 152, 8 + i * 18));
		}
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("energy");
		ret.add("scrap");
		ret.add("fluidTank");
		return ret;
	}
}
