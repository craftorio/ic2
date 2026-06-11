package ic2.core.block.kineticgenerator.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.Ic2Explosion;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.kineticgenerator.container.ContainerSteamKineticGenerator;
import ic2.core.block.machine.tileentity.TileEntityCondenser;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.init.MainConfig;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LiquidUtil;
import ic2.core.util.Util;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntitySteamKineticGenerator extends TileEntityAbstractKineticGenerator implements IHasGui, IUpgradableBlock
{
	protected final Ic2FluidTank steamTank;
	protected final Ic2FluidTank distilledWaterTank;
	public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 1);
	public final InvSlotConsumable turbineSlot = new InvSlotConsumableItemStack(this, "Turbineslot", 1, new ItemStack(Ic2Items.STEAM_TURBINE));
	private static final float outputModifier = ConfigUtil.getFloat(MainConfig.get(), "balance/energy/kineticgenerator/steam");
	private int kuOutput;
	private boolean ventingSteam;
	private boolean throttled;
	private boolean isTurbineFilledWithWater = false;
	private int condensationProgress = 0;
	protected final Fluids fluids;

	public TileEntitySteamKineticGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.STEAM_KINETIC_GENERATOR, pos, state);
		this.updateTicker = IC2.random.nextInt(this.getTickRate());
		this.turbineSlot.setStackSizeLimit(1);
		this.fluids = this.addComponent(new Fluids(this));
		this.steamTank = this.fluids.addTankInsert("steamTank", 21000, Fluids.fluidPredicate(Ic2Fluids.STEAM.still(), Ic2Fluids.SUPERHEATED_STEAM.still()));
		this.distilledWaterTank = this.fluids
			.addTank("distilledWaterTank", 1000, Fluids.fluidPredicate(Ic2Fluids.DISTILLED_WATER.still(), net.minecraft.world.level.material.Fluids.WATER));
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		if (this.distilledWaterTank.getCapacity() - this.distilledWaterTank.getFluidAmount() >= 1 && this.isTurbineFilledWithWater)
		{
			this.isTurbineFilledWithWater = false;
		}

		if (this.steamTank.getFluidAmount() > 0 && !this.isTurbineFilledWithWater && !this.turbineSlot.isEmpty())
		{
			if (!this.getActive())
			{
				this.setActive(true);
				needsInvUpdate = true;
			}

			boolean hotSteam = this.steamTank.hasExactFluid(Ic2Fluids.SUPERHEATED_STEAM.still());
			boolean turbineDoneWork = this.turbineDoWork(hotSteam);
			if (this.updateTicker++ >= this.getTickRate())
			{
				if (turbineDoneWork)
				{
					this.turbineSlot.damage(hotSteam ? 1 : 2, false);
				}

				this.updateTicker = 0;
			}
		} else if (this.getActive())
		{
			this.setActive(false);
			needsInvUpdate = true;
			this.kuOutput = 0;
		}

		needsInvUpdate |= this.upgradeSlot.tickNoMark();
		if (needsInvUpdate)
		{
			super.setChanged();
		}
	}

	private int handleSteam(boolean hotSteam)
	{
		int amount = this.steamTank.getFluidAmount();
		assert amount > 0;
		this.steamTank.drainMbUnchecked(amount, false);
		int KUWorkbuffer = amount * 2 * (hotSteam ? 2 : 1);
		if (hotSteam)
		{
			this.outputSteam(amount, true);
		} else
		{
			int condensation = amount / 10;
			this.condensationProgress += condensation;
			this.outputSteam(amount - condensation, false);
		}

		return KUWorkbuffer;
	}

	private boolean turbineDoWork(boolean hotSteam)
	{
		int rawOutput = this.handleSteam(hotSteam);
		int waterAmount = this.distilledWaterTank.getFluidAmount();
		float throttle;
		if (waterAmount == 0)
		{
			this.throttled = false;
			throttle = 1.0F;
		} else
		{
			this.throttled = true;
			throttle = 1.0F - (float) waterAmount / this.distilledWaterTank.getCapacity();
		}

		this.kuOutput = (int) (rawOutput * throttle * outputModifier);
		if (this.condensationProgress >= 100)
		{
			if (this.distilledWaterTank.fillMbUnchecked(Ic2FluidStack.create(Ic2Fluids.DISTILLED_WATER.still(), 1), true) == 1)
			{
				this.condensationProgress -= 100;
				this.distilledWaterTank.fillMbUnchecked(Ic2FluidStack.create(Ic2Fluids.DISTILLED_WATER.still(), 1), false);
			} else
			{
				this.isTurbineFilledWithWater = true;
			}
		}

		return this.kuOutput > 0;
	}

	private void outputSteam(int amount, boolean hotSteam)
	{
		Level world = this.getLevel();

		for (Direction dir : Util.ALL_DIRS)
		{
			BlockEntity te = world.getBlockEntity(this.worldPosition.relative(dir));
			if (te instanceof TileEntityCondenser || hotSteam && te instanceof TileEntitySteamKineticGenerator)
			{
				int transAmount = LiquidUtil.fillTile(te, dir.getOpposite(), Ic2FluidStack.create(Ic2Fluids.STEAM.still(), amount), false);
				if (transAmount > 0)
				{
					amount -= transAmount;
					if (amount <= 0)
					{
						break;
					}
				}
			}
		}

		if (amount > 0)
		{
			this.ventingSteam = true;
			if (world.random.nextInt(10) == 0)
			{
				new Ic2Explosion(world, null, this.worldPosition, 1, 1.0F, Ic2Explosion.Type.Heat).doExplosion();
			}
		} else
		{
			this.ventingSteam = false;
		}
	}

	public int getKUoutput()
	{
		return this.kuOutput;
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.condensationProgress = nbt.getInt("condensationprogress");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("condensationprogress", this.condensationProgress);
	}

	@Override
	public ContainerBase<TileEntitySteamKineticGenerator> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerSteamKineticGenerator(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerSteamKineticGenerator(syncId, inventory, this);
	}

	@Override
	public int getConnectionBandwidth(Direction side)
	{
		return side == this.getFacing() ? this.kuOutput : 0;
	}

	@Override
	public int drawKineticEnergy(Direction side, int request, boolean simulate)
	{
		return side == this.getFacing() ? this.kuOutput : 0;
	}

	public int gaugeLiquidScaled(int i, int tank)
	{
		return tank == 0 && this.distilledWaterTank.getFluidAmount() > 0
			? this.distilledWaterTank.getFluidAmount() * i / this.distilledWaterTank.getCapacity()
			: 0;
	}

	@Override
	public double getEnergy()
	{
		return 0.0;
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return false;
	}

	public int getDistilledWaterTankFill()
	{
		return this.distilledWaterTank.getFluidAmount();
	}

	public Ic2FluidTank getDistilledWaterTank()
	{
		return this.distilledWaterTank;
	}

	public boolean hasTurbine()
	{
		return !this.turbineSlot.isEmpty();
	}

	public boolean isVentingSteam()
	{
		return this.ventingSteam;
	}

	public boolean isThrottled()
	{
		return this.throttled;
	}

	public boolean isTurbineBlockedByWater()
	{
		return this.isTurbineFilledWithWater;
	}

	public int getTickRate()
	{
		return 20;
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
	}
}
