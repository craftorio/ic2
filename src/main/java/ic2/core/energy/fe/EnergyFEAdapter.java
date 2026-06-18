package ic2.core.energy.fe;

import ic2.api.energy.EnergyNet;
import ic2.core.block.comp.Energy;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnergyFEAdapter implements IEnergyStorage
{
	public static final double EU_FE_RATIO = 4.0;

	private final Energy energy;
	private final Direction side;

	public EnergyFEAdapter(Energy energy, Direction side)
	{
		this.energy = energy;
		this.side = side;
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate)
	{
		if (maxReceive <= 0 || !canReceive()) return 0;

		double eu = maxReceive / EU_FE_RATIO;
		double maxEu = Math.min(energy.getFreeEnergy(), EnergyNet.instance.getPowerFromTier(energy.getSinkTier()));
		eu = Math.min(eu, maxEu);

		if (eu <= 1e-9) return 0;

		if (!simulate) energy.addEnergy(eu);

		return (int) (eu * EU_FE_RATIO);
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate)
	{
		if (maxExtract <= 0 || !canExtract()) return 0;

		double eu = maxExtract / EU_FE_RATIO;
		double maxEu = Math.min(energy.getEnergy(), EnergyNet.instance.getPowerFromTier(energy.getSourceTier()));
		eu = Math.min(eu, maxEu);

		if (eu <= 1e-9) return 0;

		if (simulate)
		{
			eu = energy.useEnergy(eu, true);
		} else
		{
			energy.useEnergy(eu);
		}

		return (int) (eu * EU_FE_RATIO);
	}

	@Override
	public int getEnergyStored()
	{
		double fe = energy.getEnergy() * EU_FE_RATIO;
		return fe >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) fe;
	}

	@Override
	public int getMaxEnergyStored()
	{
		double fe = energy.getCapacity() * EU_FE_RATIO;
		return fe >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) fe;
	}

	@Override
	public boolean canReceive()
	{
		if (energy.getSinkDirs().isEmpty()) return false;
		if (side != null && !energy.getSinkDirs().contains(side)) return false;
		return energy.getEnergy() < energy.getCapacity();
	}

	@Override
	public boolean canExtract()
	{
		if (energy.getSourceDirs().isEmpty()) return false;
		if (side != null && !energy.getSourceDirs().contains(side)) return false;
		return energy.getEnergy() > 0.0;
	}

	public static ICapabilityProvider createProvider(Energy energy)
	{
		return new Provider(energy);
	}

	private static class Provider implements ICapabilityProvider
	{
		private final List<LazyOptional<IEnergyStorage>> sideCaps;
		private static final int capacity = 6;
		private final LazyOptional<IEnergyStorage> nullSideCap;
		
		Provider(Energy energy)
		{
			nullSideCap = LazyOptional.of(() -> new EnergyFEAdapter(energy, null));
			sideCaps = new ArrayList<>(Collections.nCopies(capacity, null));
			for (Direction dir : Direction.values())
			{
				sideCaps.set(dir.ordinal(), LazyOptional.of(() -> new EnergyFEAdapter(energy, dir)));
			}
		}

		@Override
		public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side)
		{
			if (cap == ForgeCapabilities.ENERGY)
			{
				return (LazyOptional<T>) (side != null ? sideCaps.get(side.ordinal()) : nullSideCap);
			}
			return LazyOptional.empty();
		}
	}
}
