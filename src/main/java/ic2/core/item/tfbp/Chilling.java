package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Chilling extends TerraformerBase
{
	@Override
	boolean terraform(Level world, BlockPos pos)
	{
		pos = TileEntityTerra.getFirstBlockFrom(world, pos, 10);
		if (pos == null)
		{
			return false;
		}

		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block == Blocks.f_49990_)
		{
			world.setBlockAndUpdate(pos, Blocks.f_50126_.defaultBlockState());
			return true;
		}

		if (block == Blocks.f_50126_)
		{
			BlockPos below = pos.m_7495_();
			Block blockBelow = world.getBlockState(below).getBlock();
			if (blockBelow == Blocks.f_49990_)
			{
				world.setBlockAndUpdate(below, Blocks.f_50126_.defaultBlockState());
				return true;
			}
		} else if (block == Blocks.f_50125_)
		{
			if (isSurroundedBySnow(world, pos))
			{
				world.setBlockAndUpdate(pos, Blocks.f_50127_.defaultBlockState());
				return true;
			}

			int size = (Integer) state.getValue(SnowLayerBlock.f_56581_);
			if (SnowLayerBlock.f_56581_.m_6908_().contains(size + 1))
			{
				world.setBlockAndUpdate(pos, (BlockState) state.setValue(SnowLayerBlock.f_56581_, size + 1));
				return true;
			}
		}

		pos = pos.m_7494_();
		if (!Blocks.f_50125_.defaultBlockState().m_60710_(world, pos) && block != Blocks.f_50126_)
		{
			return false;
		}

		world.setBlockAndUpdate(pos, Blocks.f_50125_.defaultBlockState());
		return true;
	}

	private static boolean isSurroundedBySnow(Level world, BlockPos pos)
	{
		for (Direction dir : Util.HORIZONTAL_DIRS)
		{
			if (!isSnowHere(world, pos.relative(dir)))
			{
				return false;
			}
		}

		return true;
	}

	private static boolean isSnowHere(Level world, BlockPos pos)
	{
		int prevY = pos.getY();
		pos = TileEntityTerra.getFirstBlockFrom(world, pos, 16);
		if (pos != null && prevY <= pos.getY())
		{
			Block block = world.getBlockState(pos).getBlock();
			if (block != Blocks.f_50127_ && block != Blocks.f_50125_)
			{
				pos = pos.m_7494_();
				if (Blocks.f_50125_.defaultBlockState().m_60710_(world, pos) || block == Blocks.f_50126_)
				{
					world.setBlockAndUpdate(pos, Blocks.f_50125_.defaultBlockState());
				}

				return false;
			} else
			{
				return true;
			}
		} else
		{
			return false;
		}
	}
}
