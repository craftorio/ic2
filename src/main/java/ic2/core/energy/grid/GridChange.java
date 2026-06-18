package ic2.core.energy.grid;

import ic2.api.energy.tile.IEnergyTile;

import java.util.List;

import net.minecraft.core.BlockPos;

class GridChange
{
	final GridChange.Type type;
	final BlockPos pos;
	final IEnergyTile ioTile;
	List<IEnergyTile> subTiles;

	GridChange(GridChange.Type type, BlockPos pos, IEnergyTile ioTile)
	{
		this.type = type;
		this.pos = pos;
		this.ioTile = ioTile;
	}

	enum Type
	{
		ADDITION,
		REMOVAL;
	}
}
