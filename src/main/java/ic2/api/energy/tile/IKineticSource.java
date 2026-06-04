package ic2.api.energy.tile;

import net.minecraft.util.EnumFacing;

public interface IKineticSource
{
	@Deprecated
	int maxrequestKineticEnergyTick(EnumFacing paramEnumFacing);

	default int getConnectionBandwidth(EnumFacing side)
	{
		return maxrequestKineticEnergyTick(side);
	}

	@Deprecated
	int requestKineticEnergy(EnumFacing paramEnumFacing, int paramInt);

	default int drawKineticEnergy(EnumFacing side, int request, boolean simulate)
	{
		return !simulate ? requestKineticEnergy(side, request) : maxrequestKineticEnergyTick(side);
	}
}
