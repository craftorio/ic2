package ic2.core.block.wiring;

import ic2.core.ContainerFullInv;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class ContainerChargepadBlock extends ContainerFullInv<TileEntityChargepadBlock>
{
	public ContainerChargepadBlock(EntityPlayer player, TileEntityChargepadBlock tileEntity1)
	{
		super(player, tileEntity1, 161);
		this.addSlotToContainer(new SlotInvSlot(tileEntity1.chargeSlot, 0, 56, 17));
		this.addSlotToContainer(new SlotInvSlot(tileEntity1.dischargeSlot, 0, 56, 53));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("redstoneMode");
		return ret;
	}
}
