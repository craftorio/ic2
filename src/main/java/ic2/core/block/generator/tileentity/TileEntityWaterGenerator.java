package ic2.core.block.generator.tileentity;

import ic2.core.IC2;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.IC2Config;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class TileEntityWaterGenerator extends TileEntityBaseRotorGenerator implements IGuiValueProvider
{
	private static final int tickRate = 128;
	public final InvSlotConsumableLiquid fuelSlot;
	@GuiSynced
	public int water = 0;
	public int microStorage = 0;
	public int maxWater = 2000;
	private int ticker = IC2.random.nextInt(128);

	public TileEntityWaterGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.WATER_GENERATOR, pos, state, 2.0, 1, 4, 2);
		this.production = 2.0;
		this.fuelSlot = new InvSlotConsumableLiquidByList(
			this, "fuel", IC2Config.balance.watermillAutomation.get() ? InvSlot.Access.IO : InvSlot.Access.NONE, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, Fluids.WATER
		);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.updateWaterCount();
	}

	@Override
	public boolean gainFuel()
	{
		if (this.fuel + 500 > this.maxWater)
		{
			return false;
		}

		if (!this.fuelSlot.isEmpty())
		{
			ItemStack liquid = this.fuelSlot.consume(1);
			if (liquid == null)
			{
				return false;
			}

			this.fuel += 500;
			if (IC2.envProxy.hasRecipeRemainder(liquid))
			{
				this.production = 1.0;
			} else
			{
				this.production = 2.0;
			}

			return true;
		} else if (this.fuel <= 0)
		{
			this.flowPower();
			this.production = this.microStorage / 100;
			this.microStorage = (int) (this.microStorage - this.production * 100.0);
			if (this.production > 0.0)
			{
				this.fuel++;
				return true;
			} else
			{
				return false;
			}
		} else
		{
			return false;
		}
	}

	@Override
	public boolean isConverting()
	{
		return this.fuel > 0;
	}

	@Override
	public boolean needsFuel()
	{
		return this.fuel <= this.maxWater;
	}

	public void flowPower()
	{
		if (++this.ticker % 128 == 0)
		{
			this.updateWaterCount();
		}

		this.water = (int) Math.round(this.water * IC2Config.balance.energy.generator.water.get());
		if (this.water > 0)
		{
			this.microStorage = this.microStorage + this.water;
		}
	}

	public void updateWaterCount()
	{
		Level world = this.getLevel();
		int count = 0;

		for (int x = -1; x < 2; x++)
		{
			for (int y = -1; y < 2; y++)
			{
				for (int z = -1; z < 2; z++)
				{
					if (world.getBlockState(this.worldPosition.offset(x, y, z)).getFluidState().is(Fluids.WATER))
					{
						count++;
					}
				}
			}
		}

		this.water = count;
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.GENERATOR_WATER_LOOP;
	}

	@Override
	protected boolean delayActiveUpdate()
	{
		return true;
	}

	@Override
	protected boolean shouldRotorRotate()
	{
		return this.water > 0 || this.fuel > 0;
	}

	@Override
	protected float rotorSpeedFactor()
	{
		return this.fuel > 0 ? 1.0F : this.water / 25.0F;
	}

	@Override
	public double getGuiValue(String name)
	{
		if ("water".equals(name))
		{
			assert this.maxWater > 0;
			return (double) this.fuel / this.maxWater;
		} else
		{
			throw new IllegalArgumentException("Unexpected value requested: " + name);
		}
	}
}
