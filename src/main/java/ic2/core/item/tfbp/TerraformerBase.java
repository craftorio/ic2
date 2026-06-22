package ic2.core.item.tfbp;

import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public abstract class TerraformerBase
{
	protected static boolean isVanilla(Block block)
	{
		ResourceLocation id = Util.getName(block);
		return id != null && id.getNamespace().equals("minecraft");
	}

	abstract boolean terraform(Level var1, BlockPos var2);

	void init()
	{
	}
}
