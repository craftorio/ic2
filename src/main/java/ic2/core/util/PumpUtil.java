package ic2.core.util;

import ic2.core.fluid.FluidHandler;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PumpUtil
{
	private static int moveUp(Level world, MutableBlockPos pos)
	{
		pos.set(pos.getX(), pos.getY() + 1, pos.getZ());
		int newDecay = getFlowDecay(world, pos);
		if (newDecay >= 0)
		{
			return newDecay;
		}

		pos.set(pos.getX() + 1, pos.getY(), pos.getZ());
		newDecay = getFlowDecay(world, pos);
		if (newDecay >= 0)
		{
			return newDecay;
		}

		pos.set(pos.getX() - 2, pos.getY(), pos.getZ());
		newDecay = getFlowDecay(world, pos);
		if (newDecay >= 0)
		{
			return newDecay;
		}

		pos.set(pos.getX() + 1, pos.getY(), pos.getZ() + 1);
		newDecay = getFlowDecay(world, pos);
		if (newDecay >= 0)
		{
			return newDecay;
		}

		pos.set(pos.getX(), pos.getY(), pos.getZ() - 2);
		newDecay = getFlowDecay(world, pos);
		if (newDecay >= 0)
		{
			return newDecay;
		}

		pos.set(pos.getX(), pos.getY() - 1, pos.getZ() + 1);
		return -1;
	}

	private static int moveSideways(Level world, MutableBlockPos pos, int decay)
	{
		pos.set(pos.getX() - 1, pos.getY(), pos.getZ());
		int newDecay = getFlowDecay(world, pos);
		if (newDecay >= 0 && newDecay < decay)
		{
			return newDecay;
		}

		pos.set(pos.getX() + 1, pos.getY(), pos.getZ() + 1);
		newDecay = getFlowDecay(world, pos);
		if (newDecay >= 0 && newDecay < decay)
		{
			return newDecay;
		}

		pos.set(pos.getX(), pos.getY(), pos.getZ() - 2);
		newDecay = getFlowDecay(world, pos);
		if (newDecay >= 0 && newDecay < decay)
		{
			return newDecay;
		}

		pos.set(pos.getX() + 1, pos.getY(), pos.getZ() + 1);
		newDecay = getFlowDecay(world, pos);
		if (newDecay >= 0 && newDecay < decay)
		{
			return newDecay;
		}

		pos.set(pos.getX() - 1, pos.getY(), pos.getZ());
		return -1;
	}

	public static BlockPos searchFluidSource(Level world, BlockPos startPos)
	{
		MutableBlockPos pos = new MutableBlockPos();
		pos.set(startPos.getX(), startPos.getY(), startPos.getZ());
		int decay = getFlowDecay(world, pos);

		for (int i = 0; i < 64; i++)
		{
			int newDecay = moveUp(world, pos);
			if (newDecay < 0)
			{
				newDecay = moveSideways(world, pos, decay);
				if (newDecay < 0)
				{
					break;
				}
			}

			decay = newDecay;
		}

		Set<BlockPos> visited = new HashSet<>(64);
		int i = 0;

		while (i < 64)
		{
			label85:
			{
				label103:
				{
					visited.add(new BlockPos(pos));
					pos.set(pos.getX() - 1, pos.getY(), pos.getZ());
					if (!visited.contains(pos))
					{
						int newDecay = getFlowDecay(world, pos);
						if (newDecay >= 0)
						{
							if (newDecay == 0)
							{
								return pos;
							}
							break label103;
						}
					}

					pos.set(pos.getX() + 1, pos.getY(), pos.getZ() + 1);
					if (!visited.contains(pos))
					{
						int newDecay = getFlowDecay(world, pos);
						if (newDecay >= 0)
						{
							if (newDecay == 0)
							{
								return pos;
							}
							break label103;
						}
					}

					pos.set(pos.getX(), pos.getY(), pos.getZ() - 2);
					if (!visited.contains(pos))
					{
						int newDecay = getFlowDecay(world, pos);
						if (newDecay >= 0)
						{
							if (newDecay == 0)
							{
								return pos;
							}
							break label103;
						}
					}

					pos.set(pos.getX() + 1, pos.getY(), pos.getZ() + 1);
					if (visited.contains(pos))
					{
						break label85;
					}

					int newDecay = getFlowDecay(world, pos);
					if (newDecay < 0)
					{
						break label85;
					}

					if (newDecay == 0)
					{
						return pos;
					}
				}

				i++;
				continue;
			}

			pos.set(pos.getX() - 1, pos.getY(), pos.getZ());
			break;
		}

		MutableBlockPos cPos = new MutableBlockPos();

		for (int ix = -2; ix <= 2; ix++)
		{
			for (int iz = -2; iz <= 2; iz++)
			{
				cPos.set(pos.getX() + ix, pos.getY(), pos.getZ() + iz);
				BlockState state = world.getBlockState(cPos);
				decay = getFlowDecay(state, world, cPos);
				if (decay >= 0)
				{
					if (decay == 0)
					{
						return cPos;
					}

					if (decay >= 7 || !(state.getBlock() instanceof LiquidBlock))
					{
						world.removeBlock(cPos, false);
					}
				}
			}
		}

		return null;
	}

	protected static int getFlowDecay(Level world, BlockPos pos)
	{
		BlockState state = world.getBlockState(pos);
		return getFlowDecay(state, world, pos);
	}

	protected static int getFlowDecay(BlockState state, Level world, BlockPos pos)
	{
		Block block = state.getBlock();
		int level = FluidHandler.getWorldFluidLevel(state, world, pos);
		if (level >= 0)
		{
			return level;
		} else
		{
			return block instanceof LiquidBlock ? (Integer) state.getValue(LiquidBlock.LEVEL) : -1;
		}
	}

	protected static boolean isExistInArray(int x, int y, int z, int[][] xyz, int end_i)
	{
		for (int i = 0; i <= end_i; i++)
		{
			if (xyz[i][0] == x && xyz[i][1] == y && xyz[i][2] == z)
			{
				return true;
			}
		}

		return false;
	}
}
