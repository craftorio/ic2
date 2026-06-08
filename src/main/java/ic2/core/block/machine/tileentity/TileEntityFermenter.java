package ic2.core.block.machine.tileentity;

import ic2.api.energy.tile.IHeatSource;
import ic2.api.recipe.IFermenterRecipeManager;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerFermenter;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.MainConfig;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.recipe.FermenterRecipeManager;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;
import ic2.core.util.ConfigUtil;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityFermenter extends TileEntityInventory implements IHasGui, IGuiValueProvider, IUpgradableBlock
{
	private final Ic2FluidTank inputTank;
	private final Ic2FluidTank outputTank;
	public final InvSlotConsumableLiquidByManager fluidInputCellInSlot;
	public final InvSlotConsumableLiquidByTank fluidOutputCellInSlot;
	public final InvSlotOutput fluidInputCellOutSlot;
	public final InvSlotOutput fluidOutputCellOutSlot;
	public final InvSlotOutput fertiliserSlot;
	public final InvSlotUpgrade upgradeSlot;
	protected final Fluids fluids;
	private int heatBuffer = 0;
	public int progress = 0;
	private final int maxProgress = ConfigUtil.getInt(MainConfig.get(), "balance/fermenter/biomass_per_fertilizier");
	private boolean newActive = false;

	public TileEntityFermenter(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.FERMENTER, pos, state);
		this.fluids = this.addComponent(new Fluids(this));
		this.outputTank = this.fluids.addTankExtract("output", 2000);
		this.inputTank = this.fluids.addTankInsert("input", 10000, Fluids.fluidPredicate(Recipes.fermenter));
		this.fluidInputCellOutSlot = new InvSlotOutput(this, "biomassOutput", 1);
		this.fluidOutputCellOutSlot = new InvSlotOutput(this, "biogassOutput", 1);
		this.fertiliserSlot = new InvSlotOutput(this, "output", 1);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 2);
		this.fluidOutputCellInSlot = new InvSlotConsumableLiquidByTank(
			this, "biogasInput", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, this.outputTank
		);
		this.fluidInputCellInSlot = new InvSlotConsumableLiquidByManager(
			this, "biomassInput", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, Recipes.fermenter
		);
	}

	public static void init()
	{
		Recipes.fermenter = new FermenterRecipeManager();
		Recipes.fermenter
			.addRecipe(
				Ic2Fluids.BIOMASS.still,
				ConfigUtil.getInt(MainConfig.get(), "balance/fermenter/need_amount_biomass_per_run"),
				ConfigUtil.getInt(MainConfig.get(), "balance/fermenter/hU_per_run"),
				Ic2Fluids.BIOGAS.still,
				ConfigUtil.getInt(MainConfig.get(), "balance/fermenter/output_amount_biogas_per_run")
			);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.inputTank.fromNbt(nbt.getCompound("inputTank"));
		this.outputTank.fromNbt(nbt.getCompound("outputTank"));
		this.progress = nbt.getInt("progress");
		this.heatBuffer = nbt.getInt("heatBuffer");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.put("inputTank", this.inputTank.toNbt(new CompoundTag()));
		nbt.put("outputTank", this.outputTank.toNbt(new CompoundTag()));
		nbt.putInt("progress", this.progress);
		nbt.putInt("heatBuffer", this.heatBuffer);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.fluidInputCellInSlot.processIntoTank(this.inputTank, this.fluidInputCellOutSlot);
		this.fluidOutputCellInSlot.processFromTank(this.outputTank, this.fluidOutputCellOutSlot);
		this.newActive = this.work();
		if (this.getActive() != this.newActive)
		{
			this.setActive(this.newActive);
		}

		this.upgradeSlot.tick();
	}

	private boolean work()
	{
		if (this.progress >= this.maxProgress)
		{
			this.fertiliserSlot.add(new ItemStack(Ic2Items.FERTILIZER));
			this.progress = 0;
		}

		Direction dir = this.getFacing();
		BlockEntity te = this.getLevel().getBlockEntity(this.worldPosition.relative(dir));
		if (te instanceof IHeatSource && !this.inputTank.isEmpty())
		{
			IFermenterRecipeManager.FermentationProperty fp = Recipes.fermenter.getFermentationInformation(this.inputTank.getFluidStack().getFluid());
			if (fp != null
				&& this.inputTank.getFluidAmount() >= fp.inputAmount
				&& fp.outputAmount <= this.outputTank.getCapacity() - this.outputTank.getFluidAmount())
			{
				this.heatBuffer = this.heatBuffer + ((IHeatSource) te).drawHeat(dir.getOpposite(), 100, false);
				if (this.heatBuffer >= fp.heat)
				{
					this.heatBuffer = this.heatBuffer - fp.heat;
					this.inputTank.drainMbUnchecked(fp.inputAmount, false);
					this.outputTank.fillMbUnchecked(fp.getOutput(), false);
					this.progress = this.progress + fp.inputAmount;
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public ContainerBase<TileEntityFermenter> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerFermenter(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerFermenter(syncId, inventory, this);
	}

	@Override
	public double getGuiValue(String name)
	{
		if ("heat".equals(name))
		{
			if (this.heatBuffer == 0)
			{
				return 0.0;
			}

			double maxHeatBuff = ConfigUtil.getInt(MainConfig.get(), "balance/fermenter/hU_per_run");
			if (!this.inputTank.isEmpty())
			{
				IFermenterRecipeManager.FermentationProperty fp = Recipes.fermenter.getFermentationInformation(this.inputTank.getFluidStack().getFluid());
				if (fp != null)
				{
					maxHeatBuff = fp.heat;
				}
			}

			return this.heatBuffer / maxHeatBuff;
		} else if ("progress".equals(name))
		{
			return this.progress == 0 ? 0.0 : (double) this.progress / this.maxProgress;
		} else
		{
			throw new IllegalArgumentException("Invalid GUI value: " + name);
		}
	}

	public int gaugeLiquidScaled(int i, int tank)
	{
		switch (tank)
		{
			case 0:
				if (this.inputTank.isEmpty())
				{
					return 0;
				}

				return this.inputTank.getFluidAmount() * i / this.inputTank.getCapacity();
			case 1:
				if (this.outputTank.isEmpty())
				{
					return 0;
				}

				return this.outputTank.getFluidAmount() * i / this.outputTank.getCapacity();
			default:
				return 0;
		}
	}

	public Ic2FluidTank getInputTank()
	{
		return this.inputTank;
	}

	public Ic2FluidTank getOutputTank()
	{
		return this.outputTank;
	}

	@Override
	public double getEnergy()
	{
		return 40.0;
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return true;
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(
			UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing
		);
	}
}
