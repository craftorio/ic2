package ic2.api.energy.tile;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public interface IColoredEnergyTile extends IEnergyTile
{
	DyeColor getColor(Direction var1);

	default boolean canInteractWith(IEnergyTile tile, Direction side)
	{
		if (!(tile instanceof IColoredEnergyTile other))
		{
			return true;
		} else
		{
			DyeColor thisColor = this.getColor(side);
			DyeColor otherColor = other.getColor(side.getOpposite());
			return thisColor == null || otherColor == null || thisColor == otherColor;
		}
	}
}
