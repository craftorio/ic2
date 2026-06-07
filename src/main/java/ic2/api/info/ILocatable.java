package ic2.api.info;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ILocatable
{
	BlockPos getPosition();

	Level getWorldObj();
}
