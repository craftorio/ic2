package ic2.core.block.machine.tileentity;

import ic2.api.recipe.ILiquidHeatExchangerManager;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.LiquidHeatExchangerManager;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerLiquidHeatExchanger;
import ic2.core.block.tileentity.TileEntityHeatSourceInventory;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.init.MainConfig;
import ic2.core.network.GrowingBuffer;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;
import ic2.core.util.ConfigUtil;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public class TileEntityLiquidHeatExchanger extends TileEntityHeatSourceInventory implements IHasGui, IUpgradableBlock
{
	private boolean newActive;
	public final Ic2FluidTank inputTank;
	public final Ic2FluidTank outputTank;
	public final InvSlotConsumable heatexchangerslots;
	public final InvSlotOutput hotoutputSlot;
	public final InvSlotOutput cooloutputSlot;
	public final InvSlotConsumableLiquid hotfluidinputSlot;
	public final InvSlotConsumableLiquid coolfluidinputSlot;
	public final InvSlotUpgrade upgradeSlot;
	protected final Fluids fluids = this.addComponent(new Fluids(this));

	public TileEntityLiquidHeatExchanger(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.LIQUID_HEAT_EXCHANGER, pos, state);
		this.inputTank = this.fluids.addTankInsert("inputTank", 2000, Fluids.fluidPredicate(Recipes.liquidCooldownManager));
		this.outputTank = this.fluids.addTankExtract("outputTank", 2000);
		this.heatexchangerslots = new InvSlotConsumableItemStack(this, "heatExchanger", 10, new ItemStack(Ic2Items.HEAT_CONDUCTOR));
		this.heatexchangerslots.setStackSizeLimit(1);
		this.hotoutputSlot = new InvSlotOutput(this, "hotOutputSlot", 1);
		this.cooloutputSlot = new InvSlotOutput(this, "outputSlot", 1);
		this.hotfluidinputSlot = new InvSlotConsumableLiquidByManager(
			this, "hotFluidInput", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, Recipes.liquidCooldownManager
		);
		this.coolfluidinputSlot = new InvSlotConsumableLiquidByTank(
			this, "coolFluidOutput", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, this.outputTank
		);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 3);
		this.newActive = false;
	}

	public static void init()
	{
		Recipes.liquidCooldownManager = new LiquidHeatExchangerManager(false);
		Recipes.liquidHeatupManager = new LiquidHeatExchangerManager(true);
		IC2.envProxy
			.runAfterRegistryInit(
				() ->
				{
					addCooldownRecipe(
						net.minecraft.world.level.material.Fluids.f_76195_,
						Ic2Fluids.PAHOEHOE_LAVA.still,
						Math.round(20.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/fluidconversion/heatExchangerLava"))
					);
					addBiDiRecipe(
						Ic2Fluids.HOT_COOLANT.still,
						Ic2Fluids.COOLANT.still,
						Math.round(20.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/fluidconversion/heatExchangerHotCoolant"))
					);
					addHeatupRecipe(
						Ic2Fluids.HOT_WATER.still,
						net.minecraft.world.level.material.Fluids.f_76193_,
						Math.round(1.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/fluidconversion/heatExchangerWater"))
					);
				}
			);
	}

	public static void addBiDiRecipe(Fluid hotFluid, Fluid coldFluid, int huPerMB)
	{
		addHeatupRecipe(hotFluid, coldFluid, huPerMB);
		addCooldownRecipe(hotFluid, coldFluid, huPerMB);
	}

	public static void addHeatupRecipe(Fluid hotFluid, Fluid coldFluid, int huPerMB)
	{
		Recipes.liquidHeatupManager.addFluid(coldFluid, hotFluid, huPerMB);
	}

	public static void addCooldownRecipe(Fluid hotFluid, Fluid coldFluid, int huPerMB)
	{
		Recipes.liquidCooldownManager.addFluid(hotFluid, coldFluid, huPerMB);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.hotfluidinputSlot.processIntoTank(this.inputTank, this.hotoutputSlot);
		this.coolfluidinputSlot.processFromTank(this.outputTank, this.cooloutputSlot);
		this.newActive = this.HeatBuffer > 0;
		if (this.getActive() != this.newActive)
		{
			this.setActive(this.newActive);
		}

		this.upgradeSlot.tick();
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerLiquidHeatExchanger(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerLiquidHeatExchanger(syncId, inventory, this);
	}

	public int gaugeLiquidScaled(int i, int tank)
	{
		switch (tank)
		{
			case 0:
				if (this.inputTank.getFluidAmount() <= 0)
				{
					return 0;
				}

				return this.inputTank.getFluidAmount() * i / this.inputTank.getCapacity();
			case 1:
				if (this.outputTank.getFluidAmount() <= 0)
				{
					return 0;
				}

				return this.outputTank.getFluidAmount() * i / this.outputTank.getCapacity();
			default:
				return 0;
		}
	}

	@Override
	public int getMaxHeatEmittedPerTick()
	{
		int count = 0;

		for (int i = 0; i < this.heatexchangerslots.size(); i++)
		{
			if (!this.heatexchangerslots.isEmpty(i))
			{
				count += 10;
			}
		}

		return count;
	}

	@Override
	protected int fillHeatBuffer(int bufferspace)
	{
		if (bufferspace > 0)
		{
			int AmountHotCoolant = this.inputTank.getFluidAmount();
			int OutputTankFreeCap = this.outputTank.getCapacity() - this.outputTank.getFluidAmount();
			Ic2FluidStack draincoolant = null;
			if (OutputTankFreeCap == 0 || AmountHotCoolant == 0)
			{
				return 0;
			}

			Fluid fluidInputTank = this.inputTank.getFluidStack().getFluid();
			Fluid fluidOutput = null;
			int hUper1mb = 0;
			if (Recipes.liquidCooldownManager.acceptsFluid(fluidInputTank))
			{
				ILiquidHeatExchangerManager.HeatExchangeProperty hep = Recipes.liquidCooldownManager.getHeatExchangeProperty(fluidInputTank);
				fluidOutput = hep.outputFluid;
				hUper1mb = hep.huPerMB;
			}

			if (fluidOutput == null)
			{
				return 0;
			}

			if (this.outputTank.getFluidAmount() > 0 && !this.outputTank.getFluidStack().hasExactFluid(fluidOutput))
			{
				return 0;
			}

			int mbtofillheatbuffer = bufferspace / hUper1mb;
			if (OutputTankFreeCap >= AmountHotCoolant)
			{
				if (mbtofillheatbuffer <= AmountHotCoolant)
				{
					draincoolant = this.inputTank.drainMbUnchecked(mbtofillheatbuffer, true);
				} else
				{
					draincoolant = this.inputTank.drainMbUnchecked(AmountHotCoolant, true);
				}
			} else if (mbtofillheatbuffer <= OutputTankFreeCap)
			{
				draincoolant = this.inputTank.drainMbUnchecked(mbtofillheatbuffer, true);
			} else
			{
				draincoolant = this.inputTank.drainMbUnchecked(OutputTankFreeCap * 20, true);
			}

			if (draincoolant != null)
			{
				this.inputTank.drainMbUnchecked(draincoolant.getAmountMb(), false);
				this.outputTank.fillMbUnchecked(Ic2FluidStack.create(fluidOutput, draincoolant.getAmountMb()), false);
				return draincoolant.getAmountMb() * hUper1mb;
			}
		}

		return 0;
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
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(
			UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing
		);
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
}
