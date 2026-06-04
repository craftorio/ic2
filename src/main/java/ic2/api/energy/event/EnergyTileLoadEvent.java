package ic2.api.energy.event;

import ic2.api.energy.tile.IEnergyTile;

public class EnergyTileLoadEvent extends EnergyTileEvent
{
	public EnergyTileLoadEvent(IEnergyTile energyTile1)
	{
		super(energyTile1);
	}
}
