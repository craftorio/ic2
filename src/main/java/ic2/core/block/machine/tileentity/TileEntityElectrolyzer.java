package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IElectrolyzerRecipeManager;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerElectrolyzer;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.gui.CustomGauge;
import ic2.core.network.GrowingBuffer;
import ic2.core.recipe.ElectrolyzerRecipeManager;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.LiquidUtil;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityElectrolyzer extends TileEntityElectricMachine implements IUpgradableBlock, IHasGui, CustomGauge.IGaugeRatioProvider
{
	public final InvSlotUpgrade upgradeSlot;
	protected final Fluids fluids = this.addComponent(new Fluids(this));
	protected int progress = 0;
	protected IElectrolyzerRecipeManager.ElectrolyzerRecipe recipe = null;
	protected Ic2FluidTank input;

	public TileEntityElectrolyzer(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.ELECTROLYZER, pos, state, 32000, 2);
		this.input = this.fluids.addTankInsert("input", 8000, Fluids.fluidPredicate(Recipes.electrolyzer));
		this.upgradeSlot = new InvSlotUpgrade(this, "upgradeSlot", 4);
	}

	public static void init()
	{
		Recipes.electrolyzer = new ElectrolyzerRecipeManager();
		Recipes.electrolyzer
			.addRecipe(
				net.minecraft.world.level.material.Fluids.WATER,
				40,
				32,
				new IElectrolyzerRecipeManager.ElectrolyzerOutput(Ic2Fluids.HYDROGEN.still(), 26, Direction.DOWN),
				new IElectrolyzerRecipeManager.ElectrolyzerOutput(Ic2Fluids.OXYGEN.still(), 13, Direction.UP)
			);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.progress = nbt.getInt("progress");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("progress", this.progress);
	}

	@Override
	public void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		if (this.canOperate())
		{
			assert this.recipe != null;
			this.setActive(true);
			this.energy.useEnergy(this.recipe.EUaTick());
			this.progress++;
			if (this.progress >= this.recipe.ticksNeeded())
			{
				this.operate();
				this.progress = 0;
				needsInvUpdate = true;
			}
		} else
		{
			this.setActive(false);
			this.progress = 0;
		}

		needsInvUpdate |= this.upgradeSlot.tickNoMark();
		if (needsInvUpdate)
		{
			super.setChanged();
		}
	}

	protected boolean canOperate()
	{
		if (this.input.isEmpty())
		{
			return false;
		}

		this.recipe = Recipes.electrolyzer.getElectrolysisInformation(this.input.getFluidStack().getFluid());
		if (this.recipe != null && !(this.energy.getEnergy() < this.recipe.EUaTick()) && this.input.getFluidAmount() >= this.recipe.inputAmount())
		{
			for (IElectrolyzerRecipeManager.ElectrolyzerOutput output : this.recipe.outputs())
			{
				if (!this.canFillTank(output.tankDirection(), output.getOutput()))
				{
					return false;
				}
			}

			return true;
		} else
		{
			return false;
		}
	}

	protected void operate()
	{
		assert this.recipe != null;
		this.input.drainMbUnchecked(this.recipe.inputAmount(), false);

		for (IElectrolyzerRecipeManager.ElectrolyzerOutput output : this.recipe.outputs())
		{
			this.fillTank(output.tankDirection(), output.getOutput());
		}
	}

	protected boolean canFillTank(Direction facing, Ic2FluidStack fluid)
	{
		BlockEntity te = this.getLevel().getBlockEntity(this.worldPosition.relative(facing));
		return te instanceof TileEntityTank ? LiquidUtil.fillTile(te, facing, fluid, true) == fluid.getAmountMb() : false;
	}

	protected void fillTank(Direction facing, Ic2FluidStack fluid)
	{
		BlockEntity te = this.getLevel().getBlockEntity(this.worldPosition.relative(facing));
		if (te instanceof TileEntityTank)
		{
			LiquidUtil.fillTile(te, facing, fluid, false);
		}
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.FluidConsuming);
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
		return new ContainerElectrolyzer(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerElectrolyzer(syncId, inventory, this);
	}

	public Ic2FluidTank getInput()
	{
		return this.input;
	}

	public boolean hasRecipe()
	{
		return this.getCurrentRecipe() != null;
	}

	public IElectrolyzerRecipeManager.ElectrolyzerRecipe getCurrentRecipe()
	{
		return this.recipe;
	}

	@Override
	public double getRatio()
	{
		return this.recipe == null ? 0.0 : (double) this.progress / this.recipe.ticksNeeded();
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_ELECTROLYZER_LOOP;
	}
}
