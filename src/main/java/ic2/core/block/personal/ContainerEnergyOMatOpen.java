package ic2.core.block.personal;

import ic2.core.ContainerFullInv;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.DataSlot;

public class ContainerEnergyOMatOpen extends ContainerFullInv<TileEntityEnergyOMat>
{
	public ContainerEnergyOMatOpen(int syncId, Inventory playerInventory, TileEntityEnergyOMat be)
	{
		super(Ic2ScreenHandlers.ENERGY_O_MAT_OPEN, syncId, playerInventory, be, 166);
		this.m_38897_(new SlotInvSlot(be.demandSlot, 0, 24, 17));
		this.m_38897_(new SlotInvSlot(be.upgradeSlot, 0, 24, 53));
		this.m_38897_(new SlotInvSlot(be.inputSlot, 0, 60, 17));
		this.m_38897_(new SlotInvSlot(be.chargeSlot, 0, 60, 53));
		this.m_38895_(new DataSlot()
		{
			public int m_6501_()
			{
				return ContainerEnergyOMatOpen.this.base.chargeSlot.tier;
			}

			public void m_6422_(int value)
			{
				ContainerEnergyOMatOpen.this.base.chargeSlot.tier = value;
			}
		});
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("paidFor");
		ret.add("euBuffer");
		ret.add("euOffer");
		return ret;
	}
}
