package ic2.core.block.personal;

import ic2.core.ContainerFullInv;
import ic2.core.slot.SlotInvSlot;
import ic2.core.slot.SlotInvSlotReadOnly;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IContainerListener;

public class ContainerEnergyOMatClosed extends ContainerFullInv<TileEntityEnergyOMat>
{
	private int lastTier = -1;

	public ContainerEnergyOMatClosed(EntityPlayer player, TileEntityEnergyOMat tileEntity1)
	{
		super(player, tileEntity1, 166);
		this.addSlotToContainer(new SlotInvSlotReadOnly(tileEntity1.demandSlot, 0, 50, 17));
		this.addSlotToContainer(new SlotInvSlot(tileEntity1.inputSlot, 0, 143, 17));
		this.addSlotToContainer(new SlotInvSlot(tileEntity1.chargeSlot, 0, 143, 53));
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("paidFor");
		ret.add("euOffer");
		return ret;
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		for (IContainerListener listener : this.listeners)
		{
			if (this.base.chargeSlot.tier != this.lastTier)
			{
				listener.sendWindowProperty(this, 0, this.base.chargeSlot.tier);
			}
		}

		this.lastTier = this.base.chargeSlot.tier;
	}

	public void updateProgressBar(int index, int value)
	{
		super.updateProgressBar(index, value);
		switch (index)
		{
			case 0:
				this.base.chargeSlot.tier = value;
		}
	}
}
