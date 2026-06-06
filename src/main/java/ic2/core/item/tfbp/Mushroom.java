package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.util.BiomeUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

class Mushroom extends TerraformerBase
{
	@Override
	boolean terraform(World world, BlockPos pos)
	{
		pos = TileEntityTerra.getFirstSolidBlockFrom(world, pos, 20);
		return pos == null ? false : growBlockWithDependancy(world, pos, Blocks.BROWN_MUSHROOM_BLOCK, Blocks.BROWN_MUSHROOM);
	}

	private static boolean growBlockWithDependancy(World world, BlockPos pos, Block target, Block dependancy)
	{
		MutableBlockPos cPos = new MutableBlockPos();

		for (int xm = pos.getX() - 1; dependancy != null && xm < pos.getX() + 1; xm++)
		{
			int zm = pos.getZ() - 1;

			while (zm < pos.getZ() + 1)
			{
				int ym = pos.getY() + 5;

				while (true)
				{
					label116:
					{
						if (ym > pos.getY() - 2)
						{
							cPos.setPos(xm, ym, zm);
							IBlockState state = world.getBlockState(cPos);
							Block block = state.getBlock();
							if (dependancy == Blocks.MYCELIUM)
							{
								if (block != dependancy && block != Blocks.BROWN_MUSHROOM_BLOCK && block != Blocks.RED_MUSHROOM_BLOCK)
								{
									if (!block.isAir(state, world, cPos) && (block == Blocks.DIRT || block == Blocks.GRASS))
									{
										BlockPos dstPos = new BlockPos(cPos);
										world.setBlockState(dstPos, dependancy.getDefaultState());
										BiomeUtil.setBiome(world, dstPos, Biomes.MUSHROOM_ISLAND);
										return true;
									}
									break label116;
								}
							} else
							{
								if (dependancy != Blocks.BROWN_MUSHROOM)
								{
									break label116;
								}

								if (block != Blocks.BROWN_MUSHROOM && block != Blocks.RED_MUSHROOM)
								{
									if (!block.isAir(state, world, cPos) && growBlockWithDependancy(world, cPos, Blocks.BROWN_MUSHROOM, Blocks.MYCELIUM))
									{
										return true;
									}
									break label116;
								}
							}
						}

						zm++;
						break;
					}

					ym--;
				}
			}
		}

		if (target == Blocks.BROWN_MUSHROOM)
		{
			Block base = world.getBlockState(pos).getBlock();
			if (base != Blocks.MYCELIUM)
			{
				if (base != Blocks.BROWN_MUSHROOM_BLOCK && base != Blocks.RED_MUSHROOM_BLOCK)
				{
					return false;
				}

				world.setBlockState(pos, Blocks.MYCELIUM.getDefaultState());
			}

			BlockPos above = pos.up();
			IBlockState state = world.getBlockState(above);
			Block block = state.getBlock();
			if (!block.isAir(state, world, above) && block != Blocks.TALLGRASS)
			{
				return false;
			}

			Block shroom = world.rand.nextBoolean() ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM;
			world.setBlockState(above, shroom.getDefaultState());
			return true;
		} else
		{
			if (target == Blocks.BROWN_MUSHROOM_BLOCK)
			{
				BlockPos above = pos.up();
				IBlockState state = world.getBlockState(above);
				Block base = state.getBlock();
				if (base != Blocks.BROWN_MUSHROOM && base != Blocks.RED_MUSHROOM)
				{
					return false;
				}

				if (((BlockMushroom) base).generateBigMushroom(world, above, state, world.rand))
				{
					for (int xm = pos.getX() - 1; xm < pos.getX() + 1; xm++)
					{
						for (int zm = pos.getZ() - 1; zm < pos.getZ() + 1; zm++)
						{
							cPos.setPos(xm, above.getY(), zm);
							Block block = world.getBlockState(cPos).getBlock();
							if (block == Blocks.BROWN_MUSHROOM || block == Blocks.RED_MUSHROOM)
							{
								world.setBlockToAir(new BlockPos(cPos));
							}
						}
					}

					return true;
				}
			}

			return false;
		}
	}
}
