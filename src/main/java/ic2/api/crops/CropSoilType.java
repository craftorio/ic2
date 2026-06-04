package ic2.api.crops;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public enum CropSoilType
{
	FARMLAND(Blocks.FARMLAND),
	MYCELIUM(Blocks.MYCELIUM),
	SAND(Blocks.SAND),
	SOULSAND(Blocks.SOUL_SAND),
	;

	private final Block block;

	CropSoilType(Block block)
	{
		this.block = block;
	}

	public Block getBlock()
	{
		return this.block;
	}

	public static boolean contains(Block block)
	{
		for (CropSoilType aux : values())
		{
			if (aux.getBlock() == block)
				return true;
		}
		return false;
	}
}
