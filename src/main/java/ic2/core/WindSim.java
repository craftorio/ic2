package ic2.core;

import ic2.shades.org.ejml.simple.SimpleMatrix;
import net.minecraft.world.World;

public class WindSim
{
	private int windStrength = 5 + IC2.random.nextInt(20);
	private int windDirection = IC2.random.nextInt(360);
	public int windTicker;
	private final World world;
	private final SimpleMatrix windHeightCoefficients;

	public WindSim(World world)
	{
		this.world = world;
		this.windHeightCoefficients = calculateCoefficients(IC2.getWorldHeight(world), IC2.getSeaLevel(world));
	}

	private static SimpleMatrix calculateCoefficients(int height, int seaLevel)
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
		SimpleMatrix a = new SimpleMatrix(3, 3);
		SimpleMatrix b = new SimpleMatrix(3, 1);
		a.setRow(0, 0, sh, sh * sh, sh * sh * sh);
		b.set(0, 1.0);
		a.setRow(1, 0, fh, fh * fh, fh * fh * fh);
		b.set(1, 0.0);
		a.setRow(2, 0, 1.0, 2.0 * sh, 3.0 * sh * sh);
		b.set(2, 0.0);
		return a.solve(b);
	}

	public void updateWind()
	{
		if (this.windTicker++ % 128 == 0)
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
					this.windDirection = this.chancewindDirection(-18);
				case 1:
				default:
					break;
				case 2:
					this.windDirection = this.chancewindDirection(18);
			}
		}
	}

	public double getWindAt(double height)
	{
		double ret = this.windStrength;
		SimpleMatrix x = new SimpleMatrix(1, 3);
		x.setRow(0, 0, height, height * height, height * height * height);
		double heightMultiplier = Math.max(0.0, x.mult(this.windHeightCoefficients).get(0));
		ret *= heightMultiplier;
		if (this.world.isThundering())
		{
			ret *= 1.5;
		} else if (this.world.isRaining())
		{
			ret *= 1.25;
		}

		return ret * 2.4;
	}

	public double getMaxWind()
	{
		return 108.0;
	}

	private int chancewindDirection(int amount)
	{
		this.windDirection += amount;
		if (this.windDirection < 0)
		{
			return 359 - this.windDirection;
		} else
		{
			return this.windDirection > 359 ? 0 + (this.windDirection - 359) : this.windDirection;
		}
	}
}
