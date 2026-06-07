package ic2.core.util;

import ic2.core.IC2;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class RotationUtil
{
	public static Direction rotateByRay(BlockHitResult ray)
	{
		assert ray.m_6662_() == Type.BLOCK;
		Vec3 hit = ray.m_82450_();
		BlockPos pos = ray.m_82425_();
		return rotateByHit(ray.m_82434_(), (float) hit.f_82479_ - pos.getX(), (float) hit.f_82480_ - pos.getY(), (float) hit.f_82481_ - pos.getZ());
	}

	public static Direction rotateByHit(Direction facingHit, float hitX, float hitY, float hitZ)
	{
		switch (facingHit)
		{
			case DOWN:
				if (hitX <= 0.25F)
				{
					if (hitZ > 0.25F && hitZ < 0.75F)
					{
						return Direction.WEST;
					}

					return Direction.UP;
				}

				if (hitX > 0.25F && hitX < 0.75F)
				{
					if (hitZ <= 0.25F)
					{
						return Direction.NORTH;
					}

					if (hitZ >= 0.75F)
					{
						return Direction.SOUTH;
					}

					return Direction.DOWN;
				}

				if (hitX >= 0.75F)
				{
					if (hitZ > 0.25F && hitZ < 0.75F)
					{
						return Direction.EAST;
					}

					return Direction.UP;
				}
				break;
			case UP:
				if (hitX <= 0.25F)
				{
					if (hitZ > 0.25F && hitZ < 0.75F)
					{
						return Direction.WEST;
					}

					return Direction.DOWN;
				}

				if (hitX > 0.25F && hitX < 0.75F)
				{
					if (hitZ <= 0.25F)
					{
						return Direction.NORTH;
					}

					if (hitZ >= 0.75F)
					{
						return Direction.SOUTH;
					}

					return Direction.UP;
				}

				if (hitX >= 0.75F)
				{
					if (hitZ > 0.25F && hitZ < 0.75F)
					{
						return Direction.EAST;
					}

					return Direction.DOWN;
				}
				break;
			case NORTH:
				if (hitX <= 0.25F)
				{
					if (hitY > 0.25F && hitY < 0.75F)
					{
						return Direction.WEST;
					}

					return Direction.SOUTH;
				}

				if (hitX > 0.25F && hitX < 0.75F)
				{
					if (hitY <= 0.25F)
					{
						return Direction.DOWN;
					}

					if (hitY >= 0.75F)
					{
						return Direction.UP;
					}

					return Direction.NORTH;
				}

				if (hitX >= 0.75F)
				{
					if (hitY > 0.25F && hitY < 0.75F)
					{
						return Direction.EAST;
					}

					return Direction.SOUTH;
				}
				break;
			case SOUTH:
				if (hitX <= 0.25F)
				{
					if (hitY > 0.25F && hitY < 0.75F)
					{
						return Direction.WEST;
					}

					return Direction.NORTH;
				}

				if (hitX > 0.25F && hitX < 0.75F)
				{
					if (hitY <= 0.25F)
					{
						return Direction.DOWN;
					}

					if (hitY >= 0.75F)
					{
						return Direction.UP;
					}

					return Direction.SOUTH;
				}

				if (hitX >= 0.75F)
				{
					if (hitY > 0.25F && hitY < 0.75F)
					{
						return Direction.EAST;
					}

					return Direction.NORTH;
				}
				break;
			case WEST:
				if (hitZ <= 0.25F)
				{
					if (hitY > 0.25F && hitY < 0.75F)
					{
						return Direction.NORTH;
					}

					return Direction.EAST;
				}

				if (hitZ > 0.25F && hitZ < 0.75F)
				{
					if (hitY <= 0.25F)
					{
						return Direction.DOWN;
					}

					if (hitY >= 0.75F)
					{
						return Direction.UP;
					}

					return Direction.WEST;
				}

				if (hitZ >= 0.75F)
				{
					if (hitY > 0.25F && hitY < 0.75F)
					{
						return Direction.SOUTH;
					}

					return Direction.EAST;
				}
				break;
			case EAST:
				if (hitZ <= 0.25F)
				{
					if (hitY > 0.25F && hitY < 0.75F)
					{
						return Direction.NORTH;
					}

					return Direction.WEST;
				}

				if (hitZ > 0.25F && hitZ < 0.75F)
				{
					if (hitY <= 0.25F)
					{
						return Direction.DOWN;
					}

					if (hitY >= 0.75F)
					{
						return Direction.UP;
					}

					return Direction.EAST;
				}

				if (hitZ >= 0.75F)
				{
					if (hitY > 0.25F && hitY < 0.75F)
					{
						return Direction.SOUTH;
					}

					return Direction.WEST;
				}
		}

		return facingHit;
	}

	public static int[] shuffledFacings()
	{
		int[] ordinals = new int[] { 0, 1, 2, 3, 4, 5 };

		for (int i = ordinals.length - 1; i > 0; i--)
		{
			int index = IC2.random.nextInt(i + 1);
			if (index != i)
			{
				ordinals[index] ^= ordinals[i];
				ordinals[i] ^= ordinals[index];
				ordinals[index] ^= ordinals[i];
			}
		}

		return ordinals;
	}
}
