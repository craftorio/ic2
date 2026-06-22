package ic2.core;

import net.minecraft.world.level.Level;

public class WindSim
{
	private final Level world;
	private final double[] windHeightCoefficients;
	public int windTicker;
	private int windStrength = 5 + IC2.random.nextInt(20);
	private int windDirection = IC2.random.nextInt(360);

	public WindSim(Level world)
	{
		this.world = world;
		this.windHeightCoefficients = calculateCoefficients(IC2.getWorldMaxHeight(world), IC2.getSeaLevel(world));
	}

	private static double[] calculateCoefficients(int height, int seaLevel)
	{
		height = Math.max(1, height);
		seaLevel = Math.max(0, seaLevel);
		double baseHeight;
		if (seaLevel < height)
		{
			baseHeight = seaLevel;
		} else
		{
			baseHeight = height * 0.5;
		}

		double sh = baseHeight + (height - baseHeight) / 2.0;
		double fh = height * 1.125;
		double[] matrixA = new double[] { sh, sh * sh, sh * sh * sh, fh, fh * fh, fh * fh * fh, 1.0, 2.0 * sh, 3.0 * sh * sh };
		double[] matrixB = new double[] { 1.0, 0.0, 0.0 };
		if (!solve(matrixA, matrixB))
		{
			throw new RuntimeException("matrix inversion failed (height=" + height + ", sealevel=" + seaLevel + ")");
		} else
		{
			return matrixB;
		}
	}

	private static boolean solve(double[] ma, double[] mb)
	{
		double invDet = 1.0
			/ (ma[0] * ma[4] * ma[8] + ma[1] * ma[5] * ma[6] + ma[2] * ma[3] * ma[7] - ma[2] * ma[4] * ma[6] - ma[0] * ma[5] * ma[7] - ma[1] * ma[3] * ma[8]);
		if (Double.isInfinite(invDet))
		{
			return false;
		}

		double a11 = (ma[4] * ma[8] - ma[5] * ma[7]) * invDet;
		double a12 = (ma[2] * ma[7] - ma[1] * ma[8]) * invDet;
		double a13 = (ma[1] * ma[5] - ma[2] * ma[4]) * invDet;
		double a21 = (ma[5] * ma[6] - ma[3] * ma[8]) * invDet;
		double a22 = (ma[0] * ma[8] - ma[2] * ma[6]) * invDet;
		double a23 = (ma[2] * ma[3] - ma[0] * ma[5]) * invDet;
		double a31 = (ma[3] * ma[7] - ma[4] * ma[6]) * invDet;
		double a32 = (ma[1] * ma[6] - ma[0] * ma[7]) * invDet;
		double a33 = (ma[0] * ma[4] - ma[1] * ma[3]) * invDet;
		double b1 = a11 * mb[0] + a12 * mb[1] + a13 * mb[2];
		double b2 = a21 * mb[0] + a22 * mb[1] + a23 * mb[2];
		double b3 = a31 * mb[0] + a32 * mb[1] + a33 * mb[2];
		mb[0] = b1;
		mb[1] = b2;
		mb[2] = b3;
		return true;
	}

	public void updateWind()
	{
		if ((this.windTicker++ & 127) == 0)
		{
			int upChance = 10;
			int downChance = 10;
			if (this.windStrength > 20)
			{
				upChance -= this.windStrength - 20;
			} else if (this.windStrength < 10)
			{
				downChance -= 10 - this.windStrength;
			}

			if (IC2.random.nextInt(100) < upChance)
			{
				this.windStrength++;
			} else if (IC2.random.nextInt(100) < downChance)
			{
				this.windStrength--;
			}

			switch (IC2.random.nextInt(3))
			{
				case 0:
					this.windDirection = this.chanceWindDirection(-18);
					break;
				case 2:
					this.windDirection = this.chanceWindDirection(18);
				case 1:
				default:
					break;
			}
		}
	}

	public double getWindAt(double height)
	{
		double ret = this.windStrength;
		double heightMultiplier = ((this.windHeightCoefficients[2] * height + this.windHeightCoefficients[1]) * height + this.windHeightCoefficients[0]) * height;
		heightMultiplier = Math.max(0.0, heightMultiplier);
		ret *= heightMultiplier * 2.4;
		if (this.world.isThundering())
		{
			ret *= 1.5;
		} else if (this.world.isRaining())
		{
			ret *= 1.25;
		}

		return ret;
	}

	public double getMaxWind()
	{
		return 108.0;
	}

	// TODO
	private int chanceWindDirection(int amount)
	{
		this.windDirection += amount;
		if (this.windDirection < 0)
		{
			return 360 + this.windDirection;
		} else
		{
			return this.windDirection > 359 ? (this.windDirection - 359) : this.windDirection;
		}
	}
}
