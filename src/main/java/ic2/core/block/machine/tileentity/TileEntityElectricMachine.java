package ic2.core.block.machine.tileentity;

import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotDischarge;

public abstract class TileEntityElectricMachine extends TileEntityInventory
{
	protected final Energy energy;
	public final InvSlotDischarge dischargeSlot;

	public TileEntityElectricMachine(int maxEnergy, int tier)
	{
		this(maxEnergy, tier, true);
	}

	public TileEntityElectricMachine(int maxEnergy, int tier, boolean allowRedstone)
	{
		this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, tier, allowRedstone, InvSlot.InvSide.ANY);
		this.energy = this.addComponent(Energy.asBasicSink(this, maxEnergy, tier).addManagedSlot(this.dischargeSlot));
	}
}
