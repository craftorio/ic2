package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntitySolarDestiller;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerSolarDestiller extends ContainerFullInv<TileEntitySolarDestiller>
{
	public ContainerSolarDestiller(EntityPlayer player, TileEntitySolarDestiller tileEntity)
	{
		super(player, tileEntity, 184);
		addSlotToContainer(new SlotInvSlot(tileEntity.waterinputSlot, 0, 17, 27));
		addSlotToContainer(new SlotInvSlot(tileEntity.destiwaterinputSlot, 0, 136, 64));
		addSlotToContainer(new SlotInvSlot(tileEntity.wateroutputSlot, 0, 17, 45));
		addSlotToContainer(new SlotInvSlot(tileEntity.destiwateroutputSlott, 0, 136, 82));
		for (int i = 0; i < 2; i++)
			addSlotToContainer(new SlotInvSlot(tileEntity.upgradeSlot, i, 152, 8 + i * 18));
	}

	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("skyLight");
		ret.add("inputTank");
		ret.add("outputTank");
		return ret;
	}
}
