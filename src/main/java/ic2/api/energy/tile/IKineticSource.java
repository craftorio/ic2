package ic2.api.energy.tile;

import net.minecraft.core.Direction;

public interface IKineticSource
{
	@Deprecated
	int maxrequestkineticenergyTick(Direction var1);

	default int getConnectionBandwidth(Direction side)
	{
		return this.maxrequestkineticenergyTick(side);
	}

	@Deprecated
	int requestkineticenergy(Direction var1, int var2);

	default int drawKineticEnergy(Direction side, int request, boolean simulate)
	{
		return !simulate ? this.requestkineticenergy(side, request) : this.maxrequestkineticenergyTick(side);
	}
	
}
