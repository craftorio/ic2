package ic2.core.item.tfbp;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

abstract class TerraformerBase
{
	abstract boolean terraform(World var1, BlockPos var2);

	void init()
	{
	}
}
