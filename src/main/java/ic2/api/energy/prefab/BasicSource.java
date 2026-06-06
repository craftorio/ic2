package ic2.api.energy.prefab;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.info.ILocatable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BasicSource extends BasicEnergyTile implements IEnergySource
{
	protected int tier;

	public BasicSource(TileEntity parent, double capacity, int tier)
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

	public BasicSource(World world, BlockPos pos, double capacity, int tier)
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
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing direction)
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

	@Override
	protected String getNbtTagName()
	{
		return "IC2BasicSource";
	}
}
