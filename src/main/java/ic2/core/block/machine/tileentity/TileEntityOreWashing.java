package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.recipe.BasicMachineRecipeManager;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityOreWashing extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	public final InvSlotConsumableLiquid fluidSlot;
	public final InvSlotOutput cellSlot;
	@GuiSynced
	protected final FluidTank fluidTank;
	protected final Fluids fluids;

	public TileEntityOreWashing()
	{
		super(16, 500, 3);
		this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.oreWashing);
		this.fluidSlot = new InvSlotConsumableLiquidByList(this, "fluid", 1, FluidRegistry.WATER);
		this.cellSlot = new InvSlotOutput(this, "cell", 1);
		this.fluids = this.addComponent(new Fluids(this));
		this.fluidTank = this.fluids.addTankInsert("fluid", 8000, Fluids.fluidPredicate(FluidRegistry.WATER));
	}

	public static void init()
	{
		Recipes.oreWashing = new BasicMachineRecipeManager();
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.fluidTank.getFluidAmount() < this.fluidTank.getCapacity())
		{
			this.gainFluid();
		}
	}

	@Override
	public void operateOnce(MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> output, Collection<ItemStack> processResult)
	{
		super.operateOnce(output, processResult);
		this.fluidTank.drainInternal(output.getRecipe().getMetaData().getInteger("amount"), true);
	}

	@Override
	public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getOutput()
	{
		MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> ret = super.getOutput();
		if (ret != null)
		{
			if (ret.getRecipe().getMetaData() == null)
			{
				return null;
			}

			if (ret.getRecipe().getMetaData().getInteger("amount") > this.fluidTank.getFluidAmount())
			{
				return null;
			}
		}

		return ret;
	}

	public boolean gainFluid()
	{
		return this.fluidSlot.processIntoTank(this.fluidTank, this.cellSlot);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return DynamicGui.<TileEntityOreWashing>create(this, player, GuiParser.parse(this.teBlock));
	}

	@Override
	public ContainerBase<TileEntityOreWashing> getGuiContainer(EntityPlayer player)
	{
		return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
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
			UpgradableProperty.FluidConsuming
		);
	}
}
