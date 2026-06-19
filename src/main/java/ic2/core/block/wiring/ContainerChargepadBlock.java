package ic2.core.block.wiring;

import ic2.core.ContainerFullInv;
import ic2.core.block.wiring.tileentity.TileEntityChargePadBlock;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerChargepadBlock extends ContainerFullInv<TileEntityChargePadBlock>
{
	public ContainerChargepadBlock(int syncId, Inventory playerInventory, TileEntityChargePadBlock be)
	{
		super(Ic2ScreenHandlers.CHARGEPAD, syncId, playerInventory, be, 161);
		this.addSlot(new SlotInvSlot(be.chargeSlot, 0, 56, 17));
		this.addSlot(new SlotInvSlot(be.dischargeSlot, 0, 56, 53));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("redstoneMode");
		return ret;
	}
}
