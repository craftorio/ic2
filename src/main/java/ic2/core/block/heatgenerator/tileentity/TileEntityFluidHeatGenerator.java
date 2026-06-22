package ic2.core.block.heatgenerator.tileentity;

import ic2.api.recipe.IFluidHeatManager;
import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.FluidHeatManager;
import ic2.core.IHasGui;
import ic2.core.block.comp.Fluids;
import ic2.core.block.heatgenerator.container.ContainerFluidHeatGenerator;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.tileentity.TileEntityHeatSourceInventory;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.init.IC2Config;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Fluids;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

@NotClassic
public class TileEntityFluidHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui
{
	public final InvSlotConsumableLiquid fluidSlot;
	public final InvSlotOutput outputSlot;
	@GuiSynced
	protected final Ic2FluidTank fluidTank;
	protected final Fluids fluids;
	protected int burnAmount = 0;
	protected int production = 0;
	boolean newActive = false;
	private short ticker = 0;

	public TileEntityFluidHeatGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.FLUID_HEAT_GENERATOR, pos, state);
		this.fluidSlot = new InvSlotConsumableLiquidByManager(this, "fluidSlot", 1, Recipes.fluidHeatGenerator);
		this.outputSlot = new InvSlotOutput(this, "output", 1);
		this.fluids = this.addComponent(new Fluids(this));
		this.fluidTank = this.fluids.addTankInsert("fluidTank", 10000, Fluids.fluidPredicate(Recipes.semiFluidGenerator));
	}

	public static void init()
	{
		Recipes.fluidHeatGenerator = new FluidHeatManager();
		if ((float) IC2Config.balance.energy.generator.semiFluidBiogas.get().floatValue() > 0.0F)
		{
			addFuel(Ic2Fluids.BIOGAS.still(), 10, Math.round(32.0F * (float) IC2Config.balance.energy.heatGenerator.semiFluidBiogas.get().floatValue()));
		}
	}

	public static void addFuel(Fluid fluid, int amount, int heat)
	{
		Recipes.fluidHeatGenerator.addFluid(fluid, amount, heat);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		if (this.needsFluid())
		{
			needsInvUpdate = this.gainFuel();
		}

		if (needsInvUpdate)
		{
			this.setChanged();
		}

		if (this.getActive() != this.newActive)
		{
			this.setActive(this.newActive);
		}
	}

	public boolean isConverting()
	{
		return this.getTankAmount() > 0 && this.HeatBuffer < this.getMaxHeatEmittedPerTick();
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.fluidTank.fromNbt(nbt.getCompound("fluidTank"));
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		CompoundTag fluidTankTag = new CompoundTag();
		this.fluidTank.toNbt(fluidTankTag);
		nbt.put("fluidTank", fluidTankTag);
	}

	@Override
	protected int fillHeatBuffer(int maxAmount)
	{
		if (this.isConverting())
		{
			if (this.ticker >= 19)
			{
				this.fluidTank.drainMbUnchecked(this.burnAmount, false);
				this.ticker = 0;
			} else
			{
				this.ticker++;
			}

			this.newActive = true;
			return this.production;
		} else
		{
			this.newActive = false;
			return 0;
		}
	}

	@Override
	public int getMaxHeatEmittedPerTick()
	{
		return this.calcHeatProduction();
	}

	@Override
	public ContainerBase<TileEntityFluidHeatGenerator> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerFluidHeatGenerator(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerFluidHeatGenerator(syncId, inventory, this);
	}

	protected int calcHeatProduction()
	{
		if (!this.fluidTank.isEmpty() && this.getFluidfromTank() != null)
		{
			IFluidHeatManager.BurnProperty property = Recipes.fluidHeatGenerator.getBurnProperty(this.getFluidfromTank());
			if (property != null)
			{
				return this.production = property.heat;
			}
		}

		return this.production = 0;
	}

	protected void calcBurnAmount()
	{
		if (this.getFluidfromTank() != null)
		{
			IFluidHeatManager.BurnProperty property = Recipes.fluidHeatGenerator.getBurnProperty(this.getFluidfromTank());
			if (property != null)
			{
				this.burnAmount = property.amount;
				return;
			}
		}

		this.burnAmount = 0;
	}

	public Ic2FluidTank getFluidTank()
	{
		return this.fluidTank;
	}

	public Ic2FluidStack getFluidStackfromTank()
	{
		return this.fluidTank.getFluidStack();
	}

	public Fluid getFluidfromTank()
	{
		return this.getFluidStackfromTank().getFluid();
	}

	public int getTankAmount()
	{
		return this.fluidTank.getFluidAmount();
	}

	public int gaugeLiquidScaled(int i)
	{
		return this.fluidTank.getFluidAmount() <= 0 ? 0 : this.fluidTank.getFluidAmount() * i / this.fluidTank.getCapacity();
	}

	public boolean needsFluid()
	{
		return this.fluidTank.getFluidAmount() <= this.fluidTank.getCapacity();
	}

	protected boolean gainFuel()
	{
		if (!this.fluidTank.isEmpty())
		{
			this.calcHeatProduction();
			this.calcBurnAmount();
		}

		return this.fluidSlot.processIntoTank(this.fluidTank, this.outputSlot);
	}
}
