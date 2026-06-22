package ic2.core.block.generator.tileentity;

import ic2.core.IC2;
import ic2.core.WindSim;
import ic2.core.event.WorldData;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.IC2Config;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityWindGenerator extends TileEntityBaseRotorGenerator implements IGuiValueProvider.IActiveGuiValueProvider
{
	private static final double safeWindRatio = 0.5;
	private static final int tickRate = 128;
	private int ticker = IC2.random.nextInt(128);
	private int obstructedBlockCount;
	@GuiSynced
	private double overheatRatio;

	public TileEntityWindGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.WIND_GENERATOR, pos, state, 4.0, 1, 32, 2);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.updateObscuratedBlockCount();
	}

	@Override
	public boolean gainEnergy()
	{
		if (++this.ticker % 128 == 0)
		{
			if (this.ticker % 1024 == 0)
			{
				this.updateObscuratedBlockCount();
			}

			this.production = 0.0;
			this.overheatRatio = 0.0;
			if (0.1 * IC2Config.balance.energy.generator.wind.get() <= 0.0)
			{
				return false;
			}

			Level world = this.getLevel();
			WindSim windSim = WorldData.get(world).windSim;
			double wind = windSim.getWindAt(this.worldPosition.getY()) * (1.0 - this.obstructedBlockCount / 567.0);
			if (wind <= 0.0)
			{
				return false;
			}

			double windRatio = wind / windSim.getMaxWind();
			this.overheatRatio = Math.max(0.0, (windRatio - 0.5) / 0.5);
			if (wind > windSim.getMaxWind() * 0.5 && world.random.nextInt(5000) <= this.production - 5.0)
			{
				if (Util.harvestBlock(world, this.worldPosition))
				{
					for (int i = world.random.nextInt(5); i > 0; i--)
					{
						StackUtil.dropAsEntity(world, this.worldPosition, new ItemStack(Items.IRON_INGOT));
					}
				}

				return false;
			}

			this.production = wind * 0.1 * IC2Config.balance.energy.generator.wind.get();
		}

		return super.gainEnergy();
	}

	@Override
	public boolean gainFuel()
	{
		return false;
	}

	public void updateObscuratedBlockCount()
	{
		Level world = this.getLevel();
		int count = -1;

		for (int x = -4; x < 5; x++)
		{
			for (int y = -2; y < 5; y++)
			{
				for (int z = -4; z < 5; z++)
				{
					if (!world.isEmptyBlock(this.worldPosition.offset(x, y, z)))
					{
						count++;
					}
				}
			}
		}

		this.obstructedBlockCount = count;
	}

	public int getObstructions()
	{
		return this.obstructedBlockCount;
	}

	@Override
	public boolean needsFuel()
	{
		return false;
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.GENERATOR_WIND_LOOP;
	}

	@Override
	protected boolean delayActiveUpdate()
	{
		return true;
	}

	@Override
	protected boolean shouldRotorRotate()
	{
		return this.production > 0.0;
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = super.getNetworkedFields();
		ret.add("production");
		return ret;
	}

	@Override
	public boolean isGuiValueActive(String name)
	{
		if ("wind".equals(name))
		{
			return this.production > 0.0;
		} else
		{
			throw new IllegalArgumentException("Unexpected value requested: " + name);
		}
	}

	@Override
	public double getGuiValue(String name)
	{
		if ("wind".equals(name))
		{
			return Math.max(this.overheatRatio, 0.0);
		} else
		{
			throw new IllegalArgumentException("Unexpected value requested: " + name);
		}
	}
}
