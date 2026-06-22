package ic2.api.energy.prefab;

import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.info.ILocatable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BasicSink extends BasicEnergyTile implements IEnergySink
{
	protected int tier;

	public BasicSink(BlockEntity parent, double capacity, int tier)
	{
		super(parent, capacity);
		if (tier < 0)
		{
			throw new IllegalArgumentException("invalid tier: " + tier);
		}

		this.tier = tier;
	}

	public BasicSink(ILocatable parent, double capacity, int tier)
	{
		super(parent, capacity);
		if (tier < 0)
		{
			throw new IllegalArgumentException("invalid tier: " + tier);
		}

		this.tier = tier;
	}

	public BasicSink(Level world, BlockPos pos, double capacity, int tier)
	{
		super(world, pos, capacity);
		if (tier < 0)
		{
			throw new IllegalArgumentException("invalid tier: " + tier);
		}

		this.tier = tier;
	}

	@Override
	public boolean acceptsEnergyFrom(IEnergyEmitter emitter, Direction direction)
	{
		return true;
	}

	@Override
	public double getDemandedEnergy()
	{
		return Math.max(0.0, this.getCapacity() - this.getEnergyStored());
	}

	@Override
	public double injectEnergy(Direction directionFrom, double amount, double voltage)
	{
		this.setEnergyStored(this.getEnergyStored() + amount);
		return 0.0;
	}

	@Override
	public int getSinkTier()
	{
		return this.tier;
	}

	public void setSinkTier(int tier)
	{
		if (tier < 0)
		{
			throw new IllegalArgumentException("invalid tier: " + tier);
		}

		this.tier = tier;
	}

	@Override
	protected String getNbtTagName()
	{
		return "IC2BasicSink";
	}
}
