package ic2.core.block.wiring;

import ic2.core.ContainerFullInv;
import ic2.core.block.wiring.tileentity.TileEntityElectricBlock;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.ArmorSlot;
import ic2.core.slot.SlotArmor;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;

public class ContainerElectricBlock extends ContainerFullInv<TileEntityElectricBlock>
{
	public ContainerElectricBlock(int syncId, Inventory playerInventory, TileEntityElectricBlock tileEntity1)
	{
		super(Ic2ScreenHandlers.ENERGY_STORAGE, syncId, playerInventory, tileEntity1, 196);

		for (int col = 0; col < ArmorSlot.getCount(); col++)
		{
			this.addSlot(new SlotArmor(playerInventory, ArmorSlot.get(col), 8 + col * 18, 84));
		}

		this.addSlot(new SlotInvSlot(tileEntity1.chargeSlot, 0, 56, 17));
		this.addSlot(new SlotInvSlot(tileEntity1.dischargeSlot, 0, 56, 53));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("redstoneMode");
		return ret;
	}
}
