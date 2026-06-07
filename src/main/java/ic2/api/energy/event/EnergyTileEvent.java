package ic2.api.energy.event;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraftforge.event.level.LevelEvent;

public class EnergyTileEvent extends LevelEvent
{
	public final IEnergyTile tile;

	public EnergyTileEvent(IEnergyTile tile)
	{
		super(EnergyNet.instance.getWorld(tile));
		if (this.getLevel() == null)
		{
			throw new NullPointerException("world is null");
		}

		this.tile = tile;
	}
}
