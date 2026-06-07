package ic2.api.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

public interface StainableBlock
{
	DyeColor getColor(Level var1, BlockPos var2, Direction var3);

	boolean setColor(Level var1, BlockPos var2, Direction var3, DyeColor var4);
}
