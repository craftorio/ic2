package ic2.api.crops;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum CropSoilType
{
	FARMLAND(Blocks.FARMLAND),
	MYCELIUM(Blocks.MYCELIUM),
	SAND(Blocks.SAND),
	SOULSAND(Blocks.SOUL_SAND);

	private final Block block;

	CropSoilType(Block block)
	{
		if (block == null)
		{
			throw new NullPointerException("null block");
		}

		this.block = block;
	}

	public static boolean contains(Block block)
	{
		for (CropSoilType aux : values())
		{
			if (aux.getBlock() == block)
			{
				return true;
			}
		}

		return false;
	}

	public Block getBlock()
	{
		return this.block;
	}
}
