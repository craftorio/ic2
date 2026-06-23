package ic2.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;

public class ParticleUtil
{
	public static void showFurnaceFlames(Level world, BlockPos pos, Direction facing)
	{
     RandomSource rng = RandomSource.create();
		if (rng.nextInt(8) == 0)
		{
			double width = 0.625;
			double height = 0.375;
			double depthOffset = 0.02;
			double x = pos.getX() + (facing.getStepX() * 1.04 + 1.0) / 2.0;
			double y = pos.getY() + rng.nextFloat() * 0.375;
			double z = pos.getZ() + (facing.getStepZ() * 1.04 + 1.0) / 2.0;
			if (facing.getAxis() == Axis.X)
			{
				z += rng.nextFloat() * 0.625 - 0.3125;
			} else
			{
				x += rng.nextFloat() * 0.625 - 0.3125;
			}

			world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
			world.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
		}
	}
}
