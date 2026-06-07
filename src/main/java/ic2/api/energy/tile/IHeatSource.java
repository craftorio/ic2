package ic2.api.energy.tile;

import net.minecraft.core.Direction;

public interface IHeatSource
{
	@Deprecated
	int maxrequestHeatTick(Direction var1);

	default int getConnectionBandwidth(Direction side)
	{
		return this.maxrequestHeatTick(side);
	}

	@Deprecated
	int requestHeat(Direction var1, int var2);

	default int drawHeat(Direction side, int request, boolean simulate)
	{
		return !simulate ? this.requestHeat(side, request) : this.maxrequestHeatTick(side);
	}
}
