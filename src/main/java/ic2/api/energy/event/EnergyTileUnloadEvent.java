package ic2.api.energy.event;

import ic2.api.energy.tile.IEnergyTile;

public class EnergyTileUnloadEvent extends EnergyTileEvent
{
	public EnergyTileUnloadEvent(IEnergyTile energyTile1)
	{
		super(energyTile1);
	}
}
