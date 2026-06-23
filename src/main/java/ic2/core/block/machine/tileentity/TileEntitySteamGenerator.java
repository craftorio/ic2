package ic2.core.block.machine.tileentity;

import ic2.api.energy.tile.IHeatSource;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.Ic2Explosion;
import ic2.core.block.comp.Fluids;
import ic2.core.block.machine.container.ContainerSteamGenerator;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Fluids;
import ic2.core.util.BiomeUtil;
import ic2.core.util.LiquidUtil;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.util.RandomSource;

@NotClassic
public class TileEntitySteamGenerator extends TileEntityInventory implements IHasGui, IGuiValueProvider, INetworkClientTileEntityEventListener
{
	private static final float maxHeat = 500.0F;
	private static final float heatPerHu = 5.0E-4F;
	private static final float coolingPerMb = 0.1F;
	private static final float maxCooling = 2.0F;
	private static final int maxHuInput = 1200;
	private static final int maxCalcification = 100000;
	private static final int steamExpansion = 100;
	private static final float epsilon = 1.0E-4F;
	public final Ic2FluidTank waterTank;
	protected final Fluids fluids = this.addComponent(new Fluids(this));
	private int heatInput = 0;
	private int inputMB = 0;
	private int calcification = 0;
	private int outputMB = 0;
	private TileEntitySteamGenerator.outputType outputFluid = TileEntitySteamGenerator.outputType.NONE;
	private float systemHeat;
	private int pressure = 0;
	private boolean newActive = false;

	public TileEntitySteamGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.STEAM_GENERATOR, pos, state);
		this.waterTank = this.fluids
			.addTankInsert("waterTank", 10000, Fluids.fluidPredicate(net.minecraft.world.level.material.Fluids.WATER, Ic2Fluids.DISTILLED_WATER.still()));
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.inputMB = nbt.getInt("inputmb");
		this.pressure = nbt.getInt("pressurevalve");
		this.systemHeat = nbt.getFloat("systemheat");
		this.calcification = nbt.getInt("calcification");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("inputmb", this.inputMB);
		nbt.putInt("pressurevalve", this.pressure);
		nbt.putFloat("systemheat", this.systemHeat);
		nbt.putInt("calcification", this.calcification);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.systemHeat = Math.max(this.systemHeat, BiomeUtil.getBiomeTemperature(this.getLevel(), this.worldPosition));
		if (this.isCalcified())
		{
			if (this.getActive())
			{
				this.setActive(false);
			}
		} else
		{
			this.newActive = this.work();
			if (this.getActive() != this.newActive)
			{
				this.setActive(this.newActive);
			}
		}

		if (!this.getActive())
		{
			this.cooldown(0.01F);
		}
	}

	private boolean work()
	{
     RandomSource rng = RandomSource.create();
		this.heatInput = this.requestHeat(1200);
		if (this.heatInput <= 0)
		{
			return false;
		}

		assert this.heatInput <= 1200;
		this.outputMB = 0;
		this.outputFluid = TileEntitySteamGenerator.outputType.NONE;
		if (!this.waterTank.isEmpty() && this.inputMB > 0)
		{
			Fluid inputFluid = this.waterTank.getFluidStack().getFluid();
			boolean hasDistilledWater = inputFluid == Ic2Fluids.DISTILLED_WATER.still();
			int maxAmount = Math.min(this.inputMB, this.waterTank.getFluidAmount());
			float hUneeded = 100.0F + this.pressure / 220.0F * 100.0F;
			float targetTemp = 100.0F + this.pressure / 220.0F * 100.0F * 2.74F;
			float reqHeat = targetTemp - this.systemHeat;
			float remainingHuInput = this.heatInput;
			if (reqHeat > 1.0E-4F)
			{
				int heatReq = (int) Math.ceil(reqHeat / 5.0E-4F);
				if (this.heatInput <= heatReq)
				{
					this.heatup(this.heatInput);
					if (this.pressure == 0 && this.systemHeat < 99.9999F)
					{
						this.outputMB = maxAmount;
						this.outputFluid = hasDistilledWater ? TileEntitySteamGenerator.outputType.DISTILLEDWATER : TileEntitySteamGenerator.outputType.WATER;
						int transferred = LiquidUtil.distribute(this, Ic2FluidStack.create(inputFluid, maxAmount), false);
						if (transferred > 0)
						{
							this.waterTank.drainMbUnchecked(transferred, false);
						}
					}

					return true;
				}

				this.heatup(heatReq);
				remainingHuInput -= heatReq;
				reqHeat = targetTemp - this.systemHeat;
			}

			assert this.systemHeat >= targetTemp - 1.0E-4F;
			assert this.systemHeat >= 99.9999F;
			float availableSystemHu = Math.min(-reqHeat / 5.0E-4F, 1200 - this.heatInput);
			int activeAmount = Math.min(maxAmount, (int) ((remainingHuInput + availableSystemHu) / hUneeded));
			int totalAmount = activeAmount;
			remainingHuInput -= activeAmount * hUneeded;
			if (remainingHuInput < 0.0F)
			{
				this.cooldown(-remainingHuInput * 5.0E-4F);
				reqHeat = targetTemp - this.systemHeat;
			}

			if (reqHeat <= -0.1001F)
			{
				int coolingAmount = Math.min(maxAmount, (int) (-reqHeat / 0.1F));
				coolingAmount = Math.min(coolingAmount, (int) Math.ceil(20.0));
				assert coolingAmount >= 0;
				this.cooldown(coolingAmount * 0.1F);
				totalAmount = Math.max(activeAmount, coolingAmount);
			}

			if (remainingHuInput > 0.0F)
			{
				this.heatup(remainingHuInput);
			}

			if (totalAmount <= 0)
			{
				return true;
			}

			if (!hasDistilledWater)
			{
				this.calcification += totalAmount;
			}

			this.waterTank.drainMbUnchecked(totalAmount, false);
			if (activeAmount <= 0)
			{
				return true;
			}

			this.outputMB = activeAmount * 100;
			Fluid output;
			if (this.systemHeat >= 373.9999F)
			{
				output = Ic2Fluids.SUPERHEATED_STEAM.still();
				this.outputFluid = TileEntitySteamGenerator.outputType.SUPERHEATEDSTEAM;
			} else
			{
				output = Ic2Fluids.STEAM.still();
				this.outputFluid = TileEntitySteamGenerator.outputType.STEAM;
			}

			int transferred = LiquidUtil.distribute(this, Ic2FluidStack.create(output, this.outputMB), false);
			int remaining = this.outputMB - transferred;
			if (remaining > 0)
			{
				Level world = this.getLevel();
				if (rng.nextInt(10) == 0)
				{
					new Ic2Explosion(world, null, this.worldPosition, 1, 1.0F, Ic2Explosion.Type.Heat).doExplosion();
				} else if (remaining >= 100)
				{
					this.waterTank.fillMbUnchecked(Ic2FluidStack.create(inputFluid, remaining / 100), false);
				}
			}

			return true;
		} else
		{
			this.heatup(this.heatInput);
			return true;
		}
	}

	private void heatup(float heatinput)
	{
		assert heatinput >= -1.0E-4F;
		this.systemHeat += heatinput * 5.0E-4F;
		if (this.systemHeat > 500.0F)
		{
			Level world = this.getLevel();
			world.removeBlock(this.worldPosition, false);
			new Ic2Explosion(world, null, this.worldPosition, 10, 0.01F, Ic2Explosion.Type.Heat).doExplosion();
		}
	}

	private void cooldown(float cool)
	{
		assert cool >= -1.0E-4F;
		this.systemHeat = Math.max(this.systemHeat - cool, BiomeUtil.getBiomeTemperature(this.getLevel(), this.worldPosition));
	}

	private int requestHeat(int requestHeat)
	{
		Level world = this.getLevel();
		int targetHeat = requestHeat;

		for (Direction dir : Util.ALL_DIRS)
		{
			if (world.getBlockEntity(this.worldPosition.relative(dir)) instanceof IHeatSource hs)
			{
				int request = hs.drawHeat(dir.getOpposite(), targetHeat, true);
				if (request > 0)
				{
					targetHeat -= hs.drawHeat(dir.getOpposite(), request, false);
					if (targetHeat == 0)
					{
						return requestHeat;
					}
				}
			}
		}

		return requestHeat - targetHeat;
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		if (event <= 2000 && event >= -2000)
		{
			this.inputMB = Math.max(Math.min(this.inputMB + event, 1000), 0);
		} else
		{
			if (event > 2000)
			{
				this.pressure = Math.min(this.pressure + (event - 2000), 300);
			}

			if (event < -2000)
			{
				this.pressure = Math.max(this.pressure + event + 2000, 0);
			}
		}
	}

	public int gaugeLiquidScaled(int i, int tank)
	{
		if (tank == 0)
		{
			return this.waterTank.getFluidAmount() <= 0 ? 0 : this.waterTank.getFluidAmount() * i / this.waterTank.getCapacity();
		} else
		{
			return 0;
		}
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerSteamGenerator(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerSteamGenerator(syncId, inventory, this);
	}

	@Override
	public double getGuiValue(String name)
	{
		if ("heat".equals(name))
		{
			return this.systemHeat == 0.0F ? 0.0 : this.systemHeat / 500.0;
		} else if ("calcification".equals(name))
		{
			return this.calcification == 0 ? 0.0 : this.calcification / 100000.0;
		} else
		{
			throw new IllegalArgumentException();
		}
	}

	public int getOutputMB()
	{
		return this.outputMB;
	}

	public int getInputMB()
	{
		return this.inputMB;
	}

	public int getHeatInput()
	{
		return this.heatInput;
	}

	public int getPressure()
	{
		return this.pressure;
	}

	public float getSystemHeat()
	{
		return Math.round(this.systemHeat * 10.0F) / 10.0F;
	}

	public float getCalcification()
	{
		return Math.round(this.calcification / 100000.0F * 100.0F * 100.0F) / 100.0F;
	}

	public boolean isCalcified()
	{
		return this.calcification >= 100000;
	}

	public String getOutputFluidName()
	{
		return this.outputFluid.getName();
	}

	private enum outputType
	{
		NONE(""),
		WATER("ic2.SteamGenerator.output.water"),
		DISTILLEDWATER("ic2.SteamGenerator.output.destiwater"),
		STEAM("ic2.SteamGenerator.output.steam"),
		SUPERHEATEDSTEAM("ic2.SteamGenerator.output.hotsteam");

		private final String name;

		outputType(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return this.name;
		}
	}
}
