package ic2.api.crops;

import net.minecraft.world.level.block.Block;

public interface ICropType
{
	String getName();

	String getOwner();

	Block getCropBlock();

	int getMaxAge();
}
