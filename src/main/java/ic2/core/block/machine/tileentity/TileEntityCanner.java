package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.recipe.IEmptyFluidContainerRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.api.util.FluidContainerOutputMode;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlotConsumableCanner;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotProcessableCanner;
import ic2.core.block.machine.CannerBottleRecipeManager;
import ic2.core.block.machine.CannerEnrichRecipeManager;
import ic2.core.block.machine.container.ContainerCanner;
import ic2.core.block.machine.gui.GuiCanner;
import ic2.core.item.type.CraftingItemType;
import ic2.core.item.type.DustResourceType;
import ic2.core.item.type.NuclearResourceType;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@TeBlock.Delegated(current = TileEntityCanner.class, old = TileEntityClassicCanner.class)
public class TileEntityCanner extends TileEntityStandardMachine<Object, Object, Object> implements INetworkClientTileEntityEventListener
{
	private TileEntityCanner.Mode mode = TileEntityCanner.Mode.BottleSolid;
	public static final int eventSetModeBase = 0;
	public static final int eventSwapTanks = 0 + TileEntityCanner.Mode.values.length + 1;
	public final FluidTank inputTank;
	public final FluidTank outputTank;
	public final InvSlotConsumableCanner canInputSlot;
	protected final Fluids fluids;

	public static Class<? extends TileEntityElectricMachine> delegate()
	{
		return IC2.version.isClassic() ? TileEntityClassicCanner.class : TileEntityCanner.class;
	}

	public TileEntityCanner()
	{
		super(4, 200, 1);
		this.inputSlot = new InvSlotProcessableCanner(this, "input", 1);
		this.canInputSlot = new InvSlotConsumableCanner(this, "canInput", 1);
		this.fluids = this.addComponent(new Fluids(this));
		this.inputTank = this.fluids.addTankInsert("inputTank", 8000);
		this.outputTank = this.fluids.addTankExtract("outputTank", 8000);
	}

	public static void init()
	{
		Recipes.cannerBottle = new CannerBottleRecipeManager();
		Recipes.cannerEnrich = new CannerEnrichRecipeManager();
		ItemStack fuelRod = ItemName.crafting.getItemStack(CraftingItemType.fuel_rod);
		addBottleRecipe(fuelRod, ItemName.nuclear.getItemStack(NuclearResourceType.uranium), ItemName.uranium_fuel_rod.getItemStack());
		addBottleRecipe(fuelRod, ItemName.nuclear.getItemStack(NuclearResourceType.mox), ItemName.mox_fuel_rod.getItemStack());
		ItemStack tinCan = ItemName.crafting.getItemStack(CraftingItemType.tin_can);
		ItemStack filledTinCan = ItemName.filled_tin_can.getItemStack();
		addBottleRecipe(tinCan, new ItemStack(Items.POTATO), filledTinCan);
		addBottleRecipe(tinCan, 2, new ItemStack(Items.COOKIE), StackUtil.copyWithSize(filledTinCan, 2));
		addBottleRecipe(tinCan, 2, new ItemStack(Items.MELON), StackUtil.copyWithSize(filledTinCan, 2));
		addBottleRecipe(tinCan, 2, new ItemStack(Items.FISH), StackUtil.copyWithSize(filledTinCan, 2));
		addBottleRecipe(tinCan, 2, new ItemStack(Items.CHICKEN), StackUtil.copyWithSize(filledTinCan, 2));
		addBottleRecipe(tinCan, 3, new ItemStack(Items.PORKCHOP), StackUtil.copyWithSize(filledTinCan, 3));
		addBottleRecipe(tinCan, 3, new ItemStack(Items.BEEF), StackUtil.copyWithSize(filledTinCan, 3));
		addBottleRecipe(tinCan, 4, new ItemStack(Items.APPLE), StackUtil.copyWithSize(filledTinCan, 4));
		addBottleRecipe(tinCan, 4, new ItemStack(Items.CARROT), StackUtil.copyWithSize(filledTinCan, 4));
		addBottleRecipe(tinCan, 5, new ItemStack(Items.BREAD), StackUtil.copyWithSize(filledTinCan, 5));
		addBottleRecipe(tinCan, 5, new ItemStack(Items.COOKED_FISH), StackUtil.copyWithSize(filledTinCan, 5));
		addBottleRecipe(tinCan, 6, new ItemStack(Items.COOKED_CHICKEN), StackUtil.copyWithSize(filledTinCan, 6));
		addBottleRecipe(tinCan, 6, new ItemStack(Items.BAKED_POTATO), StackUtil.copyWithSize(filledTinCan, 6));
		addBottleRecipe(tinCan, 6, new ItemStack(Items.MUSHROOM_STEW), StackUtil.copyWithSize(filledTinCan, 6));
		addBottleRecipe(tinCan, 6, new ItemStack(Items.PUMPKIN_PIE), StackUtil.copyWithSize(filledTinCan, 6));
		addBottleRecipe(tinCan, 8, new ItemStack(Items.COOKED_PORKCHOP), StackUtil.copyWithSize(filledTinCan, 8));
		addBottleRecipe(tinCan, 8, new ItemStack(Items.COOKED_BEEF), StackUtil.copyWithSize(filledTinCan, 8));
		addBottleRecipe(tinCan, 12, new ItemStack(Items.CAKE), StackUtil.copyWithSize(filledTinCan, 12));
		addBottleRecipe(tinCan, new ItemStack(Items.POISONOUS_POTATO), 2, filledTinCan);
		addBottleRecipe(tinCan, new ItemStack(Items.ROTTEN_FLESH), 2, filledTinCan);
		addEnrichRecipe(FluidRegistry.WATER, ItemName.dust.getItemStack(DustResourceType.milk), FluidName.milk.getInstance());
		addEnrichRecipe(FluidRegistry.WATER, ItemName.crafting.getItemStack(CraftingItemType.cf_powder), FluidName.construction_foam.getInstance());
		addEnrichRecipe(FluidRegistry.WATER, Recipes.inputFactory.forOreDict("dustLapis", 8), FluidName.coolant.getInstance());
		addEnrichRecipe(FluidName.distilled_water.getInstance(), Recipes.inputFactory.forOreDict("dustLapis", 1), FluidName.coolant.getInstance());
		addEnrichRecipe(FluidRegistry.WATER, ItemName.crafting.getItemStack(CraftingItemType.bio_chaff), FluidName.biomass.getInstance());
		addEnrichRecipe(
			new FluidStack(FluidRegistry.WATER, 6000),
			Recipes.inputFactory.forStack(new ItemStack(Items.STICK)),
			new FluidStack(FluidName.hot_water.getInstance(), 1000)
		);
	}

	public static void addBottleRecipe(ItemStack container, int conamount, ItemStack fill, int fillamount, ItemStack output)
	{
		addBottleRecipe(Recipes.inputFactory.forStack(container, conamount), Recipes.inputFactory.forStack(fill, fillamount), output);
	}

	public static void addBottleRecipe(ItemStack container, ItemStack fill, int fillamount, ItemStack output)
	{
		addBottleRecipe(Recipes.inputFactory.forStack(container, 1), Recipes.inputFactory.forStack(fill, fillamount), output);
	}

	public static void addBottleRecipe(ItemStack container, int conamount, ItemStack fill, ItemStack output)
	{
		addBottleRecipe(Recipes.inputFactory.forStack(container, conamount), Recipes.inputFactory.forStack(fill, 1), output);
	}

	public static void addBottleRecipe(ItemStack container, ItemStack fill, ItemStack output)
	{
		addBottleRecipe(Recipes.inputFactory.forStack(container, 1), Recipes.inputFactory.forStack(fill, 1), output);
	}

	public static void addBottleRecipe(IRecipeInput container, IRecipeInput fill, ItemStack output)
	{
		Recipes.cannerBottle.addRecipe(container, fill, output);
	}

	public static void addEnrichRecipe(Fluid input, ItemStack additive, Fluid output)
	{
		addEnrichRecipe(new FluidStack(input, 1000), Recipes.inputFactory.forStack(additive, 1), new FluidStack(output, 1000));
	}

	public static void addEnrichRecipe(Fluid input, IRecipeInput additive, Fluid output)
	{
		addEnrichRecipe(new FluidStack(input, 1000), additive, new FluidStack(output, 1000));
	}

	public static void addEnrichRecipe(FluidStack input, IRecipeInput additive, FluidStack output)
	{
		Recipes.cannerEnrich.addRecipe(input, additive, output);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.setMode(TileEntityCanner.Mode.values[nbt.getInteger("mode")]);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("mode", this.mode.ordinal());
		return nbt;
	}

	@Override
	public void operateOnce(MachineRecipeResult<Object, Object, Object> result, Collection<ItemStack> processResult)
	{
		super.operateOnce(result, processResult);
		if (this.mode == TileEntityCanner.Mode.EmptyLiquid)
		{
			IEmptyFluidContainerRecipeManager.Output output = (IEmptyFluidContainerRecipeManager.Output) result.getOutput();
			this.getOutputTank().fill(output.fluid, true);
		} else if (this.mode == TileEntityCanner.Mode.EnrichLiquid)
		{
			FluidStack output = ((FluidStack) result.getOutput()).copy();
			LiquidUtil.FluidOperationResult outcome;
			if (!this.canInputSlot.isEmpty())
			{
				do
				{
					outcome = LiquidUtil.fillContainer(this.canInputSlot.get(), output, FluidContainerOutputMode.EmptyFullToOutput);
					if (outcome != null)
					{
						if (outcome.extraOutput != null && !this.outputSlot.canAdd(outcome.extraOutput))
						{
							outcome = null;
						} else
						{
							this.canInputSlot.put(outcome.inPlaceOutput);
							if (outcome.extraOutput != null)
							{
								this.outputSlot.add(outcome.extraOutput);
							}

							output.amount = output.amount - outcome.fluidChange.amount;
						}
					}
				} while (outcome != null && output.amount > 0);
			}

			this.getOutputTank().fill(output, true);
		}
	}

	@Override
	protected Collection<ItemStack> getOutput(Object output)
	{
		if (output instanceof ItemStack)
		{
			return Collections.singletonList((ItemStack) output);
		} else if (output instanceof FluidStack)
		{
			return Collections.emptyList();
		} else
		{
			return output instanceof IEmptyFluidContainerRecipeManager.Output
				? ((IEmptyFluidContainerRecipeManager.Output) output).container
				: super.getOutput(output);
		}
	}

	@Override
	public MachineRecipeResult<Object, Object, Object> getOutput()
	{
		if (this.mode != TileEntityCanner.Mode.EmptyLiquid && this.mode != TileEntityCanner.Mode.BottleLiquid)
		{
			if (this.inputSlot.isEmpty())
			{
				return null;
			}
		} else if (this.canInputSlot.isEmpty())
		{
			return null;
		}

		MachineRecipeResult<Object, Object, Object> result = this.inputSlot.process();
		if (result == null)
		{
			return null;
		}

		if (!this.outputSlot.canAdd(this.getOutput(result.getOutput())))
		{
			return null;
		}

		if (this.mode == TileEntityCanner.Mode.EmptyLiquid)
		{
			IEmptyFluidContainerRecipeManager.Output output = (IEmptyFluidContainerRecipeManager.Output) result.getOutput();
			if (this.getOutputTank().fill(output.fluid, false) != output.fluid.amount)
			{
				return null;
			}
		} else if (this.mode == TileEntityCanner.Mode.EnrichLiquid)
		{
			FluidStack output = ((FluidStack) result.getOutput()).copy();
			LiquidUtil.FluidOperationResult outcome;
			if (!this.canInputSlot.isEmpty())
			{
				do
				{
					outcome = LiquidUtil.fillContainer(this.canInputSlot.get(), output, FluidContainerOutputMode.EmptyFullToOutput);
					if (outcome != null)
					{
						if (outcome.extraOutput != null && !this.outputSlot.canAdd(outcome.extraOutput))
						{
							outcome = null;
						} else
						{
							output.amount = output.amount - outcome.fluidChange.amount;
						}
					}
				} while (outcome != null && output.amount > 0);
			}

			if (this.getOutputTank().fill(output, false) != output.amount)
			{
				return null;
			}
		}

		return result;
	}

	public FluidTank getInputTank()
	{
		return this.inputTank;
	}

	public FluidTank getOutputTank()
	{
		return this.outputTank;
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = new ArrayList<>();
		ret.add("canInputSlot");
		ret.addAll(super.getNetworkedFields());
		return ret;
	}

	@Override
	public String getStartSoundFile()
	{
		switch (this.mode)
		{
			case BottleSolid:
			case BottleLiquid:
			case EmptyLiquid:
			case EnrichLiquid:
			default:
				return null;
		}
	}

	@Override
	public String getInterruptSoundFile()
	{
		switch (this.mode)
		{
			case BottleSolid:
			case BottleLiquid:
			case EmptyLiquid:
			case EnrichLiquid:
			default:
				return null;
		}
	}

	@Override
	public ContainerBase<TileEntityCanner> getGuiContainer(EntityPlayer player)
	{
		return new ContainerCanner(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiCanner(new ContainerCanner(player, this));
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		super.onNetworkUpdate(field);
		if (field.equals("mode"))
		{
			this.setMode(this.mode);
		}
	}

	@Override
	public void onNetworkEvent(EntityPlayer player, int event)
	{
		if (event >= 0 && event < 0 + TileEntityCanner.Mode.values.length)
		{
			this.setMode(TileEntityCanner.Mode.values[event - 0]);
		} else if (event == eventSwapTanks)
		{
			this.switchTanks();
		}
	}

	public TileEntityCanner.Mode getMode()
	{
		return this.mode;
	}

	public void setMode(TileEntityCanner.Mode mode)
	{
		this.mode = mode;
		switch (mode)
		{
			case BottleSolid:
				this.canInputSlot.setOpType(InvSlotConsumableLiquid.OpType.None);
				break;
			case BottleLiquid:
				this.canInputSlot.setOpType(InvSlotConsumableLiquid.OpType.Fill);
				break;
			case EmptyLiquid:
				this.canInputSlot.setOpType(InvSlotConsumableLiquid.OpType.Drain);
				break;
			case EnrichLiquid:
				this.canInputSlot.setOpType(InvSlotConsumableLiquid.OpType.Both);
		}

		if (IC2.platform.isRendering())
		{
			if (this.audioSource != null)
			{
				this.audioSource.stop();
			}

			if (this.getStartSoundFile() != null)
			{
				this.audioSource = IC2.audioManager.createSource(this, this.getStartSoundFile());
			}
		}
	}

	private boolean switchTanks()
	{
		if (this.progress != 0)
		{
			return false;
		}

		FluidStack inputStack = this.inputTank.getFluid();
		FluidStack outputStack = this.outputTank.getFluid();
		this.inputTank.setFluid(outputStack);
		this.outputTank.setFluid(inputStack);
		return true;
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
			UpgradableProperty.FluidConsuming,
			UpgradableProperty.FluidProducing
		);
	}

	public enum Mode
	{
		BottleSolid,
		EmptyLiquid,
		BottleLiquid,
		EnrichLiquid;

		public static final TileEntityCanner.Mode[] values = values();
	}
}
