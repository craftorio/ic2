package ic2.api.energy.prefab;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.info.ILocatable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BasicSource extends BasicEnergyTile implements IEnergySource
{
	protected int tier;

	public BasicSource(BlockEntity parent, double capacity, int tier)
	{
		super(parent, capacity);
		if (tier < 0)
		{
			throw new IllegalArgumentException("invalid tier: " + tier);
		}

		this.tier = tier;
		double power = EnergyNet.instance.getPowerFromTier(tier);
		if (this.getCapacity() < power)
		{
			this.setCapacity(power);
		}
	}

	public BasicSource(ILocatable parent, double capacity, int tier)
	{
		super(parent, capacity);
		if (tier < 0)
		{
			throw new IllegalArgumentException("invalid tier: " + tier);
		}

		this.tier = tier;
		double power = EnergyNet.instance.getPowerFromTier(tier);
		if (this.getCapacity() < power)
		{
			this.setCapacity(power);
		}
	}

	public BasicSource(Level world, BlockPos pos, double capacity, int tier)
	{
		super(world, pos, capacity);
		if (tier < 0)
		{
			throw new IllegalArgumentException("invalid tier: " + tier);
		}

		this.tier = tier;
		double power = EnergyNet.instance.getPowerFromTier(tier);
		if (this.getCapacity() < power)
		{
			this.setCapacity(power);
		}
	}

	@Override
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, Direction direction)
	{
		return true;
	}

	@Override
	public double getOfferedEnergy()
	{
		return this.getEnergyStored();
	}

	@Override
	public void drawEnergy(double amount)
	{
		this.setEnergyStored(this.getEnergyStored() - amount);
	}

	@Override
	public int getSourceTier()
	{
		return this.tier;
	}

	public void setSourceTier(int tier)
	{
		if (tier < 0)
		{
			throw new IllegalArgumentException("invalid tier: " + tier);
		}

		double power = EnergyNet.instance.getPowerFromTier(tier);
		if (this.getCapacity() < power)
		{
			this.setCapacity(power);
		}

		this.tier = tier;
	}

	@Override
	protected String getNbtTagName()
	{
		return "IC2BasicSource";
	}
}
