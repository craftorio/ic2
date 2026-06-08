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
		if (block == Blocks.WATER)
		{
			world.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
			return true;
		}

		if (block == Blocks.ICE)
		{
			BlockPos below = pos.below();
			Block blockBelow = world.getBlockState(below).getBlock();
			if (blockBelow == Blocks.WATER)
			{
				world.setBlockAndUpdate(below, Blocks.ICE.defaultBlockState());
				return true;
			}
		} else if (block == Blocks.SNOW)
		{
			if (isSurroundedBySnow(world, pos))
			{
				world.setBlockAndUpdate(pos, Blocks.SNOW_BLOCK.defaultBlockState());
				return true;
			}

			int size = (Integer) state.getValue(SnowLayerBlock.LAYERS);
			if (SnowLayerBlock.LAYERS.getPossibleValues().contains(size + 1))
			{
				world.setBlockAndUpdate(pos, (BlockState) state.setValue(SnowLayerBlock.LAYERS, size + 1));
				return true;
			}
		}

		pos = pos.above();
		if (!Blocks.SNOW.defaultBlockState().canSurvive(world, pos) && block != Blocks.ICE)
		{
			return false;
		}

		world.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState());
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
			if (block != Blocks.SNOW_BLOCK && block != Blocks.SNOW)
			{
				pos = pos.above();
				if (Blocks.SNOW.defaultBlockState().canSurvive(world, pos) || block == Blocks.ICE)
				{
					world.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState());
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
