package ic2.core.block.machine.tileentity;

import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.tileentity.TileEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityElectricMachine extends TileEntityBase
{
	public final InvSlotDischarge dischargeSlot;
	protected final Energy energy;

	public TileEntityElectricMachine(BlockEntityType<? extends TileEntityElectricMachine> type, BlockPos pos, BlockState state, int maxEnergy, int tier)
	{
		this(type, pos, state, maxEnergy, tier, true);
	}

	public TileEntityElectricMachine(
		BlockEntityType<? extends TileEntityElectricMachine> type, BlockPos pos, BlockState state, int maxEnergy, int tier, boolean allowRedstone
	)
	{
		super(type, pos, state);
		this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, tier, allowRedstone, InvSlot.InvSide.ANY);
		this.energy = this.addComponent(Energy.asBasicSink(this, maxEnergy, tier).addManagedSlot(this.dischargeSlot));
	}

	public boolean hasEnergy()
	{
		return this.energy.getEnergy() > 0.0;
	}
}
