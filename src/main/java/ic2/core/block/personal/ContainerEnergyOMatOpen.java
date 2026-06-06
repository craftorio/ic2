package ic2.core.block.personal;

import ic2.core.ContainerFullInv;
import ic2.core.slot.SlotInvSlot;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IContainerListener;

public class ContainerEnergyOMatOpen extends ContainerFullInv<TileEntityEnergyOMat>
{
	private int lastTier = -1;

	public ContainerEnergyOMatOpen(EntityPlayer player, TileEntityEnergyOMat tileEntity1)
	{
		super(player, tileEntity1, 166);
		this.addSlotToContainer(new SlotInvSlot(tileEntity1.demandSlot, 0, 24, 17));
		this.addSlotToContainer(new SlotInvSlot(tileEntity1.upgradeSlot, 0, 24, 53));
		this.addSlotToContainer(new SlotInvSlot(tileEntity1.inputSlot, 0, 60, 17));
		this.addSlotToContainer(new SlotInvSlot(tileEntity1.chargeSlot, 0, 60, 53));
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
