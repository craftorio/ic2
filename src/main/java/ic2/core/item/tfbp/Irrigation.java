package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.RandomSource;

public class Irrigation extends TerraformerBase
{
	private static BlockState getLeaves(Level world, BlockPos pos)
	{
		for (Direction facing : Util.HORIZONTAL_DIRS)
		{
			BlockPos cPos = pos.relative(facing);
			BlockState state = world.getBlockState(cPos);
			if (state.is(BlockTags.LEAVES))
			{
				return state;
			}
		}

		return null;
	}

	private static void createLeaves(Level world, BlockPos pos, BlockState state)
	{
		BlockPos above = pos.above();
		if (world.isEmptyBlock(above))
		{
			world.setBlockAndUpdate(above, state);
		}

		for (Direction facing : Util.HORIZONTAL_DIRS)
		{
			BlockPos cPos = pos.relative(facing);
			if (world.isEmptyBlock(cPos))
			{
				world.setBlockAndUpdate(cPos, state);
			}
		}
	}

	private static boolean spreadGrass(Level world, BlockPos pos)
	{
     RandomSource rng = RandomSource.create();
		if (rng.nextBoolean())
		{
			return false;
		} else
		{
			pos = TileEntityTerra.getFirstBlockFrom(world, pos, 0);
			if (pos == null)
			{
				return false;
			} else
			{
				Block block = world.getBlockState(pos).getBlock();
				if (block == Blocks.DIRT)
				{
					world.setBlockAndUpdate(pos, Blocks.GRASS.defaultBlockState());
					return true;
				} else if (block == Blocks.GRASS)
				{
					world.setBlockAndUpdate(pos.above(), Blocks.TALL_GRASS.defaultBlockState());
					return true;
				} else
				{
					return false;
				}
			}
		}
	}

	@Override
	boolean terraform(Level world, BlockPos pos)
	{
     RandomSource rng = RandomSource.create();
		if (rng.nextInt(48000) == 0)
		{
			world.getLevelData().setRaining(true);
			return true;
		}

		pos = TileEntityTerra.getFirstBlockFrom(world, pos, 10);
		if (pos == null)
		{
			return false;
		}

		if (TileEntityTerra.switchGround(world, pos, Blocks.SAND, Blocks.DIRT.defaultBlockState(), true))
		{
			TileEntityTerra.switchGround(world, pos, Blocks.SAND, Blocks.DIRT.defaultBlockState(), true);
			return true;
		}

		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block instanceof BonemealableBlock && ((BonemealableBlock) block).isValidBonemealTarget(world, pos, state, false))
		{
			((BonemealableBlock) block).performBonemeal((ServerLevel) world, world.random, pos, state);
			return true;
		}

		if (block != Blocks.TALL_GRASS)
		{
			if (state.is(BlockTags.LOGS))
			{
				BlockPos above = pos.above();
				world.setBlockAndUpdate(above, state);
				BlockState leaves = getLeaves(world, pos);
				if (leaves != null)
				{
					createLeaves(world, above, leaves);
				}

				return true;
			} else if (block == Blocks.FIRE)
			{
				world.removeBlock(pos, false);
				return true;
			} else
			{
				return false;
			}
		} else
		{
			return spreadGrass(world, pos.north())
				|| spreadGrass(world, pos.east())
				|| spreadGrass(world, pos.south())
				|| spreadGrass(world, pos.west());
		}
	}
}
