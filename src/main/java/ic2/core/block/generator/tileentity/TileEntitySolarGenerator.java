package ic2.core.block.generator.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.block.generator.container.ContainerSolarGenerator;
import ic2.core.init.MainConfig;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.proxy.EnvProxy;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.BiomeUtil;
import ic2.core.util.ConfigUtil;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySolarGenerator extends TileEntityBaseGenerator
{
	public float skyLight = 0.0F;
	@GuiSynced
	public boolean sunlight = false;
	private int ticker = IC2.random.nextInt(128);
	private static final int tickRate = 128;
	private static final double energyMultiplier = ConfigUtil.getDouble(MainConfig.get(), "balance/energy/generator/solar");

	public TileEntitySolarGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.SOLAR_GENERATOR, pos, state, 1.0, 1, 2);
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
		this.skyLight = getSkyLight(this.getLevel(), this.worldPosition.m_7494_());
		this.sunlight = this.skyLight > 0.0F;
	}

	public static float getSkyLight(Level world, BlockPos pos)
	{
		if (!world.m_6042_().f_223549_())
		{
			return 0.0F;
		}

		float sunBrightness = Util.limit((float) Math.cos(world.m_46490_(1.0F)) * 2.0F + 0.2F, 0.0F, 1.0F);
		if (!IC2.envProxy.biomeHasType(BiomeUtil.getBiome(world, pos), EnvProxy.BiomeType.SANDY))
		{
			sunBrightness *= 1.0F - world.m_46722_(1.0F) * 5.0F / 16.0F;
			sunBrightness *= 1.0F - world.m_46661_(1.0F) * 5.0F / 16.0F;
			sunBrightness = Util.limit(sunBrightness, 0.0F, 1.0F);
		}

		return world.m_45517_(LightLayer.SKY, pos) / 15.0F * sunBrightness;
	}

	@Override
	public boolean needsFuel()
	{
		return false;
	}

	@Override
	public ContainerBase<TileEntitySolarGenerator> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerSolarGenerator(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerSolarGenerator(syncId, inventory, this);
	}

	@Override
	protected boolean delayActiveUpdate()
	{
		return true;
	}

	public boolean isSunlight()
	{
		return this.sunlight;
	}
}
