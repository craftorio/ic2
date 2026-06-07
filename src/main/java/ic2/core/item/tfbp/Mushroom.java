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

public class Mushroom extends TerraformerBase
{
	@Override
	boolean terraform(Level world, BlockPos pos)
	{
		pos = TileEntityTerra.getFirstSolidBlockFrom(world, pos, 20);
		return pos == null ? false : growBlockWithDependancy(world, pos, Blocks.f_50180_, Blocks.f_50072_);
	}

	private static boolean growBlockWithDependancy(Level world, BlockPos pos, Block target, Block dependancy)
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
							cPos.set(xm, ym, zm);
							BlockState state = world.getBlockState(cPos);
							Block block = state.getBlock();
							if (dependancy == Blocks.f_50195_)
							{
								if (block != dependancy && block != Blocks.f_50180_ && block != Blocks.f_50181_)
								{
									if (!state.isAir() && (block == Blocks.f_50493_ || block == Blocks.f_50034_))
									{
										BlockPos dstPos = new BlockPos(cPos);
										world.setBlockAndUpdate(dstPos, dependancy.defaultBlockState());
										BiomeUtil.setBiome(world, dstPos, BiomeUtil.getBiome(world, Biomes.f_48215_));
										return true;
									}
									break label116;
								}
							} else
							{
								if (dependancy != Blocks.f_50072_)
								{
									break label116;
								}

								if (block != Blocks.f_50072_ && block != Blocks.f_50073_)
								{
									if (!state.isAir() && growBlockWithDependancy(world, cPos, Blocks.f_50072_, Blocks.f_50195_))
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

		if (target == Blocks.f_50072_)
		{
			Block base = world.getBlockState(pos).getBlock();
			if (base != Blocks.f_50195_)
			{
				if (base != Blocks.f_50180_ && base != Blocks.f_50181_)
				{
					return false;
				}

				world.setBlockAndUpdate(pos, Blocks.f_50195_.defaultBlockState());
			}

			BlockPos above = pos.m_7494_();
			BlockState state = world.getBlockState(above);
			Block block = state.getBlock();
			if (!state.isAir() && block != Blocks.f_50359_)
			{
				return false;
			}

			Block shroom = world.random.m_188499_() ? Blocks.f_50072_ : Blocks.f_50073_;
			world.setBlockAndUpdate(above, shroom.defaultBlockState());
			return true;
		} else
		{
			if (target == Blocks.f_50180_)
			{
				BlockPos above = pos.m_7494_();
				BlockState state = world.getBlockState(above);
				Block base = state.getBlock();
				if (base != Blocks.f_50072_ && base != Blocks.f_50073_)
				{
					return false;
				}

				if (((MushroomBlock) base).m_221773_((ServerLevel) world, pos, state, world.random))
				{
					for (int xm = pos.getX() - 1; xm < pos.getX() + 1; xm++)
					{
						for (int zm = pos.getZ() - 1; zm < pos.getZ() + 1; zm++)
						{
							cPos.set(xm, above.getY(), zm);
							Block block = world.getBlockState(cPos).getBlock();
							if (block == Blocks.f_50072_ || block == Blocks.f_50073_)
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
}
