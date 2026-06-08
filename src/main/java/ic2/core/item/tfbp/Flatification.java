package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.ref.Ic2Blocks;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class Flatification extends TerraformerBase
{
	static Set<Block> removable = Collections.newSetFromMap(new IdentityHashMap<>());

	@Override
	void init()
	{
		removable.add(Blocks.SNOW);
		removable.add(Blocks.ICE);
		removable.add(Blocks.GRASS);
		removable.add(Blocks.STONE);
		removable.add(Blocks.GRAVEL);
		removable.add(Blocks.SAND);
		removable.add(Blocks.DIRT);
		removable.add(Blocks.OAK_LEAVES);
		removable.add(Blocks.SPRUCE_LEAVES);
		removable.add(Blocks.BIRCH_LEAVES);
		removable.add(Blocks.JUNGLE_LEAVES);
		removable.add(Blocks.ACACIA_LEAVES);
		removable.add(Blocks.DARK_OAK_LEAVES);
		removable.add(Blocks.TALL_GRASS);
		removable.add(Blocks.POPPY);
		removable.add(Blocks.DANDELION);
		removable.add(Blocks.WHEAT);
		removable.add(Blocks.RED_MUSHROOM);
		removable.add(Blocks.BROWN_MUSHROOM);
		removable.add(Blocks.PUMPKIN);
		removable.add(Blocks.MELON);
		removable.add(Ic2Blocks.RUBBER_LEAVES);
		removable.add(Ic2Blocks.RUBBER_SAPLING);
		removable.add(Ic2Blocks.RUBBER_LOG);
	}

	@Override
	boolean terraform(Level world, BlockPos pos)
	{
		BlockPos workPos = TileEntityTerra.getFirstBlockFrom(world, pos, 20);
		if (workPos == null)
		{
			return false;
		}

		if (world.getBlockState(workPos).getBlock() == Blocks.SNOW)
		{
			workPos = workPos.below();
		}

		if (pos.getY() == workPos.getY())
		{
			return false;
		} else if (workPos.getY() < pos.getY())
		{
			world.setBlockAndUpdate(workPos.above(), Blocks.DIRT.defaultBlockState());
			return true;
		} else if (canRemove(world.getBlockState(workPos).getBlock()))
		{
			world.removeBlock(workPos, false);
			return true;
		} else
		{
			return false;
		}
	}

	private static boolean canRemove(Block block)
	{
		return removable.contains(block) || block.builtInRegistryHolder().is(BlockTags.SAPLINGS) || block.builtInRegistryHolder().is(BlockTags.LOGS);
	}
}
