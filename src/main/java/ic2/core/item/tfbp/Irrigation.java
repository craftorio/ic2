package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.IGrowable;
import net.minecraft.block.BlockTallGrass.EnumType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class Irrigation extends TerraformerBase
{
	@Override
	boolean terraform(World world, BlockPos pos)
	{
		if (world.rand.nextInt(48000) == 0)
		{
			world.getWorldInfo().setRaining(true);
			return true;
		}

		pos = TileEntityTerra.getFirstBlockFrom(world, pos, 10);
		if (pos == null)
		{
			return false;
		}

		if (TileEntityTerra.switchGround(world, pos, Blocks.SAND, Blocks.DIRT.getDefaultState(), true))
		{
			TileEntityTerra.switchGround(world, pos, Blocks.SAND, Blocks.DIRT.getDefaultState(), true);
			return true;
		}

		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block instanceof IGrowable && ((IGrowable) block).canGrow(world, pos, state, false))
		{
			((IGrowable) block).grow(world, world.rand, pos, state);
			return true;
		}

		if (block != Blocks.TALLGRASS)
		{
			if (block == Blocks.LOG || block == Blocks.LOG2)
			{
				BlockPos above = pos.up();
				world.setBlockState(above, state);
				IBlockState leaves = getLeaves(world, pos);
				if (leaves != null)
				{
					createLeaves(world, above, leaves);
				}

				return true;
			} else if (block == Blocks.FIRE)
			{
				world.setBlockToAir(pos);
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

	private static IBlockState getLeaves(World world, BlockPos pos)
	{
		for (EnumFacing facing : EnumFacing.HORIZONTALS)
		{
			BlockPos cPos = pos.offset(facing);
			IBlockState state = world.getBlockState(cPos);
			if (state.getBlock().isLeaves(state, world, cPos))
			{
				return state;
			}
		}

		return null;
	}

	private static void createLeaves(World world, BlockPos pos, IBlockState state)
	{
		BlockPos above = pos.up();
		if (world.isAirBlock(above))
		{
			world.setBlockState(above, state);
		}

		for (EnumFacing facing : EnumFacing.HORIZONTALS)
		{
			BlockPos cPos = pos.offset(facing);
			if (world.isAirBlock(cPos))
			{
				world.setBlockState(cPos, state);
			}
		}
	}

	private static boolean spreadGrass(World world, BlockPos pos)
	{
		if (world.rand.nextBoolean())
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
					world.setBlockState(pos, Blocks.GRASS.getDefaultState());
					return true;
				} else if (block == Blocks.GRASS)
				{
					world.setBlockState(pos.up(), Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, EnumType.GRASS));
					return true;
				} else
				{
					return false;
				}
			}
		}
	}
}
