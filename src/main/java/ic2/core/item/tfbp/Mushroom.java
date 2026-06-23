package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.util.BiomeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.RandomSource;

public class Mushroom extends TerraformerBase
{
	private static boolean growBlockWithDependancy(Level world, BlockPos pos, Block target, Block dependancy)
	{
     RandomSource rng = RandomSource.create();
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
							cPos.set(xm, ym, zm);
							BlockState state = world.getBlockState(cPos);
							Block block = state.getBlock();
							if (dependancy == Blocks.MYCELIUM)
							{
								if (block != dependancy && block != Blocks.BROWN_MUSHROOM_BLOCK && block != Blocks.RED_MUSHROOM_BLOCK)
								{
									if (!state.isAir() && (block == Blocks.DIRT || block == Blocks.GRASS))
									{
										BlockPos dstPos = new BlockPos(cPos);
										world.setBlockAndUpdate(dstPos, dependancy.defaultBlockState());
										BiomeUtil.setBiome(world, dstPos, BiomeUtil.getBiome(world, Biomes.MUSHROOM_FIELDS));
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
									if (!state.isAir() && growBlockWithDependancy(world, cPos, Blocks.BROWN_MUSHROOM, Blocks.MYCELIUM))
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

				world.setBlockAndUpdate(pos, Blocks.MYCELIUM.defaultBlockState());
			}

			BlockPos above = pos.above();
			BlockState state = world.getBlockState(above);
			Block block = state.getBlock();
			if (!state.isAir() && block != Blocks.TALL_GRASS)
			{
				return false;
			}

			Block shroom = rng.nextBoolean() ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM;
			world.setBlockAndUpdate(above, shroom.defaultBlockState());
			return true;
		} else
		{
			if (target == Blocks.BROWN_MUSHROOM_BLOCK)
			{
				BlockPos above = pos.above();
				BlockState state = world.getBlockState(above);
				Block base = state.getBlock();
				if (base != Blocks.BROWN_MUSHROOM && base != Blocks.RED_MUSHROOM)
				{
					return false;
				}

				if (((MushroomBlock) base).growMushroom((ServerLevel) world, pos, state, world.random))
				{
					for (int xm = pos.getX() - 1; xm < pos.getX() + 1; xm++)
					{
						for (int zm = pos.getZ() - 1; zm < pos.getZ() + 1; zm++)
						{
							cPos.set(xm, above.getY(), zm);
							Block block = world.getBlockState(cPos).getBlock();
							if (block == Blocks.BROWN_MUSHROOM || block == Blocks.RED_MUSHROOM)
							{
								world.removeBlock(new BlockPos(cPos), false);
							}
						}
					}

					return true;
				}
			}

			return false;
		}
	}

	@Override
	boolean terraform(Level world, BlockPos pos)
	{
		pos = TileEntityTerra.getFirstSolidBlockFrom(world, pos, 20);
		return pos == null ? false : growBlockWithDependancy(world, pos, Blocks.BROWN_MUSHROOM_BLOCK, Blocks.BROWN_MUSHROOM);
	}
}
