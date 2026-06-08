package ic2.core.block.personal;

import ic2.core.ContainerFullInv;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;
import ic2.core.slot.SlotInvSlotReadOnly;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerTradeOMatClosed extends ContainerFullInv<TileEntityTradeOMat>
{
	public ContainerTradeOMatClosed(int syncId, Inventory playerInventory, TileEntityTradeOMat be)
	{
		super(Ic2ScreenHandlers.TRADE_O_MAT_CLOSED, syncId, playerInventory, be, 166);
		this.addSlot(new SlotInvSlotReadOnly(be.demandSlot, 0, 50, 19));
		this.addSlot(new SlotInvSlotReadOnly(be.offerSlot, 0, 50, 38));
		this.addSlot(new SlotInvSlot(be.inputSlot, 0, 143, 17));
		this.addSlot(new SlotInvSlot(be.outputSlot, 0, 143, 53));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("stock");
		return ret;
	}
}
