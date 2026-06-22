package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.sound.Sound;
import ic2.core.util.LiquidUtil;
import ic2.core.util.PumpUtil;
import ic2.core.util.Util;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityPump extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, IGuiValueProvider
{
	public final int defaultTier;
	public final int defaultEnergyStorage;
	public final int defaultEnergyConsume;
	public final int defaultOperationLength;
	public final InvSlotConsumableLiquid containerSlot;
	public final InvSlotOutput outputSlot;
	public final InvSlotUpgrade upgradeSlot;
	@GuiSynced
	protected final Ic2FluidTank fluidTank;
	protected final Fluids fluids;
	public int energyConsume;
	public int operationsPerTick;
	public short progress = 0;
	public int operationLength;
	@GuiSynced
	public float guiProgress;
	private Sound sound;
	private TileEntityMiner miner = null;

	public TileEntityPump(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.PUMP, pos, state, 20, 1);
		this.containerSlot = new InvSlotConsumableLiquid(this, "input", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Fill);
		this.outputSlot = new InvSlotOutput(this, "output", 1, InvSlot.InvSide.SIDE);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
		this.defaultEnergyConsume = this.energyConsume = 1;
		this.defaultOperationLength = this.operationLength = 20;
		this.defaultTier = 1;
		this.defaultEnergyStorage = this.operationLength;
		this.fluids = this.addComponent(new Fluids(this));
		this.fluidTank = this.fluids.addTankExtract("fluid", 8000);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!Objects.requireNonNull(this.getLevel()).isClientSide)
		{
			this.setUpgradestat();
		}
	}

	@Override
	protected void onUnloaded()
	{
		if (IC2.sideProxy.isRendering() && this.sound != null)
		{
			IC2.soundManager.removeSound(this, this.sound);
			this.sound = null;
		}

		this.miner = null;
		super.onUnloaded();
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.progress = nbt.getShort("progress");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putShort("progress", this.progress);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		if (this.canOperate() && this.energy.getEnergy() >= this.energyConsume * this.operationLength)
		{
			if (this.progress < this.operationLength)
			{
				this.progress++;
				this.energy.useEnergy(this.energyConsume);
			} else
			{
				this.progress = 0;
				this.operate(false);
			}

			this.activate(false);
		} else
		{
			this.shutdown(false);
		}

		needsInvUpdate |= this.containerSlot.processFromTank(this.fluidTank, this.outputSlot);
		needsInvUpdate |= this.upgradeSlot.tickNoMark();
		this.guiProgress = (float) this.progress / this.operationLength;
		if (needsInvUpdate)
		{
			super.setChanged();
		}
	}

	public boolean canOperate()
	{
		return this.operate(true);
	}

	public boolean operate(boolean sim)
	{
		if (this.miner == null || this.miner.isRemoved())
		{
			this.miner = null;
			Level world = this.getLevel();
			if (world == null)
			{
				return false;
			}

			for (Direction dir : Util.downSideFacings)
			{
				BlockEntity te = world.getBlockEntity(this.worldPosition.relative(dir));
				if (te instanceof TileEntityMiner)
				{
					this.miner = (TileEntityMiner) te;
					break;
				}
			}
		}

		Ic2FluidStack liquid = null;
		if (this.miner != null)
		{
			if (this.miner.canProvideLiquid)
			{
				liquid = this.pump(this.miner.liquidPos, sim, this.miner);
			}
		} else
		{
			Direction dir = this.getFacing();
			liquid = this.pump(this.worldPosition.relative(dir), sim, this.miner);
		}

		if (liquid != null && this.fluidTank.fillMbUnchecked(liquid, true) > 0)
		{
			if (!sim)
			{
				this.fluidTank.fillMbUnchecked(liquid, false);
			}

			return true;
		} else
		{
			return false;
		}
	}

	public Ic2FluidStack pump(BlockPos startPos, boolean sim, TileEntityMiner miner)
	{
		Level world = this.getLevel();
		if (world == null)
		{
			return null;
		}

		int freeSpace = this.fluidTank.getCapacity() - this.fluidTank.getFluidAmount();
		if (miner == null && freeSpace > 0)
		{
			BlockEntity te = world.getBlockEntity(startPos);
			BlockState state = world.getBlockState(startPos);
			Direction side = this.getFacing().getOpposite();
			if (LiquidUtil.isFluidTile(state, te, side))
			{
				if (freeSpace > 1000)
				{
					freeSpace = 1000;
				}

				return LiquidUtil.drainTile(state, world, startPos, side, freeSpace, sim);
			}
		}

		if (freeSpace >= 1000)
		{
			BlockPos cPos;
			if (miner != null && miner.canProvideLiquid)
			{
				assert miner.liquidPos != null;
				cPos = miner.liquidPos;
			} else
			{
				cPos = PumpUtil.searchFluidSource(world, startPos);
			}

			if (cPos != null)
			{
				return LiquidUtil.drainWorldFluidBlock(world, cPos, sim);
			}
		}

		return null;
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		if (IC2.sideProxy.isSimulating())
		{
			this.setUpgradestat();
		}
	}

	public void setUpgradestat()
	{
		double previousProgress = (double) this.progress / this.operationLength;
		this.operationsPerTick = this.upgradeSlot.getOperationsPerTick(this.defaultOperationLength);
		this.operationLength = this.upgradeSlot.getOperationLength(this.defaultOperationLength);
		this.energyConsume = this.upgradeSlot.getEnergyDemand(this.defaultEnergyConsume);
		this.energy.setSinkTier(this.upgradeSlot.getTier(this.defaultTier));
		this.dischargeSlot.setTier(this.energy.getSinkTier());
		this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(this.defaultEnergyStorage, this.defaultOperationLength, this.defaultEnergyConsume));
		this.progress = (short) Math.floor(previousProgress * this.operationLength + 0.1);
	}

	@Override
	public double getGuiValue(String name)
	{
		if (name.equals("progress"))
		{
			return this.guiProgress;
		} else
		{
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " Cannot get value for " + name);
		}
	}

	@Override
	public double getEnergy()
	{
		return this.energy.getEnergy();
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return this.energy.useEnergy(amount);
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return DynamicContainer.create(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return DynamicContainer.create(syncId, inventory, this);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(
			UpgradableProperty.Processing,
			UpgradableProperty.Transformer,
			UpgradableProperty.EnergyStorage,
			UpgradableProperty.ItemConsuming,
			UpgradableProperty.ItemProducing,
			UpgradableProperty.FluidProducing
		);
	}
}
