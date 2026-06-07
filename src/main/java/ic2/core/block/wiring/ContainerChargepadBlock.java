package ic2.core.block.wiring;

import ic2.core.ContainerFullInv;
import ic2.core.block.wiring.tileentity.TileEntityChargepadBlock;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerChargepadBlock extends ContainerFullInv<TileEntityChargepadBlock>
{
	public ContainerChargepadBlock(int syncId, Inventory playerInventory, TileEntityChargepadBlock be)
	{
		super(Ic2ScreenHandlers.CHARGEPAD, syncId, playerInventory, be, 161);
		this.m_38897_(new SlotInvSlot(be.chargeSlot, 0, 56, 17));
		this.m_38897_(new SlotInvSlot(be.dischargeSlot, 0, 56, 53));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("redstoneMode");
		return ret;
	}
}
