package ic2.core.block.generator.tileentity;

import ic2.core.IC2;
import ic2.core.init.MainConfig;
import ic2.core.network.GuiSynced;
import ic2.core.util.BiomeUtil;
import ic2.core.util.ConfigUtil;
import ic2.core.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;

public class TileEntitySolarGenerator extends TileEntityBaseGenerator
{
	@GuiSynced
	public float skyLight;
	private int ticker = IC2.random.nextInt(128);
	private static final int tickRate = 128;
	private static final double energyMultiplier = ConfigUtil.getDouble(MainConfig.get(), "balance/energy/generator/solar");

	public TileEntitySolarGenerator()
	{
		super(1.0, 1, 2);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.updateSunVisibility();
	}

	@Override
	public boolean gainEnergy()
	{
		if (++this.ticker % 128 == 0)
		{
			this.updateSunVisibility();
		}

		if (this.skyLight > 0.0F)
		{
			this.energy.addEnergy(energyMultiplier * this.skyLight);
			return true;
		} else
		{
			return false;
		}
	}

	@Override
	public boolean gainFuel()
	{
		return false;
	}

	public void updateSunVisibility()
	{
		this.skyLight = getSkyLight(this.getWorld(), this.pos.up());
	}

	public static float getSkyLight(World world, BlockPos pos)
	{
		if (world.provider.isNether())
		{
			return 0.0F;
		}

		float sunBrightness = Util.limit((float) Math.cos(world.getCelestialAngleRadians(1.0F)) * 2.0F + 0.2F, 0.0F, 1.0F);
		if (!BiomeDictionary.hasType(BiomeUtil.getBiome(world, pos), BiomeDictionary.Type.SANDY))
		{
			sunBrightness *= 1.0F - world.getRainStrength(1.0F) * 5.0F / 16.0F;
			sunBrightness *= 1.0F - world.getThunderStrength(1.0F) * 5.0F / 16.0F;
			sunBrightness = Util.limit(sunBrightness, 0.0F, 1.0F);
		}

		return world.getLightFor(EnumSkyBlock.SKY, pos) / 15.0F * sunBrightness;
	}

	@Override
	public boolean needsFuel()
	{
		return false;
	}

	@Override
	public boolean getGuiState(String name)
	{
		return "sunlight".equals(name) ? this.skyLight > 0.0F : super.getGuiState(name);
	}

	@Override
	protected boolean delayActiveUpdate()
	{
		return true;
	}
}
