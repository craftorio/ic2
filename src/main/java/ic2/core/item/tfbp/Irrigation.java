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

public class Irrigation extends TerraformerBase
{
	@Override
	boolean terraform(Level world, BlockPos pos)
	{
		if (world.random.nextInt(48000) == 0)
		{
			world.m_6106_().m_5565_(true);
			return true;
		}

		pos = TileEntityTerra.getFirstBlockFrom(world, pos, 10);
		if (pos == null)
		{
			return false;
		}

		if (TileEntityTerra.switchGround(world, pos, Blocks.f_49992_, Blocks.f_50493_.defaultBlockState(), true))
		{
			TileEntityTerra.switchGround(world, pos, Blocks.f_49992_, Blocks.f_50493_.defaultBlockState(), true);
			return true;
		}

		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block instanceof BonemealableBlock && ((BonemealableBlock) block).m_7370_(world, pos, state, false))
		{
			((BonemealableBlock) block).m_214148_((ServerLevel) world, world.random, pos, state);
			return true;
		}

		if (block != Blocks.f_50359_)
		{
			if (state.m_204336_(BlockTags.f_13106_))
			{
				BlockPos above = pos.m_7494_();
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
			return spreadGrass(world, pos.m_122012_())
				|| spreadGrass(world, pos.m_122029_())
				|| spreadGrass(world, pos.m_122019_())
				|| spreadGrass(world, pos.m_122024_());
		}
	}

	private static BlockState getLeaves(Level world, BlockPos pos)
	{
		for (Direction facing : Util.HORIZONTAL_DIRS)
		{
			BlockPos cPos = pos.relative(facing);
			BlockState state = world.getBlockState(cPos);
			if (state.m_204336_(BlockTags.f_13035_))
			{
				return state;
			}
		}

		return null;
	}

	private static void createLeaves(Level world, BlockPos pos, BlockState state)
	{
		BlockPos above = pos.m_7494_();
		if (world.m_46859_(above))
		{
			world.setBlockAndUpdate(above, state);
		}

		for (Direction facing : Util.HORIZONTAL_DIRS)
		{
			BlockPos cPos = pos.relative(facing);
			if (world.m_46859_(cPos))
			{
				world.setBlockAndUpdate(cPos, state);
			}
		}
	}

	private static boolean spreadGrass(Level world, BlockPos pos)
	{
		if (world.random.m_188499_())
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
				if (block == Blocks.f_50493_)
				{
					world.setBlockAndUpdate(pos, Blocks.f_50034_.defaultBlockState());
					return true;
				} else if (block == Blocks.f_50034_)
				{
					world.setBlockAndUpdate(pos.m_7494_(), Blocks.f_50359_.defaultBlockState());
					return true;
				} else
				{
					return false;
				}
			}
		}
	}
}
