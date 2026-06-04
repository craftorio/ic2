package ic2.core.block.invslot;

import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IFillFluidContainerRecipeManager;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.util.FluidContainerOutputMode;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class InvSlotProcessableCanner extends InvSlotProcessable<Object, Object, Object>
{
	public InvSlotProcessableCanner(TileEntityCanner base1, String name1, int count)
	{
		super(base1, name1, count, null);
	}

	public boolean accepts(ItemStack stack)
	{
		switch (((TileEntityCanner) this.base).getMode())
		{
			case BottleSolid:
			case EnrichLiquid:
				return super.accepts(stack);
			case BottleLiquid:
			case EmptyLiquid:
				return false;
		}
		assert false;
		return false;
	}

	public void consume(MachineRecipeResult<Object, Object, Object> result)
	{
		super.consume(result);
		ItemStack containerStack = ((TileEntityCanner) this.base).canInputSlot.get();
		if (StackUtil.isEmpty(containerStack))
			((TileEntityCanner) this.base).canInputSlot.clear();
		FluidStack fluid = ((TileEntityCanner) this.base).inputTank.getFluid();
		if (fluid != null && fluid.amount <= 0)
			((TileEntityCanner) this.base).inputTank.setFluid(null);
	}

	protected Object getInput(ItemStack fill)
	{
		ItemStack container = ((TileEntityCanner) this.base).canInputSlot.get();
		switch (((TileEntityCanner) this.base).getMode())
		{
			case BottleSolid:
				return new ICannerBottleRecipeManager.RawInput(container, fill);
			case BottleLiquid:
				return new IFillFluidContainerRecipeManager.Input(container, getTankFluid());
			case EmptyLiquid:
				return container;
			case EnrichLiquid:
				return new ICannerEnrichRecipeManager.RawInput(getTankFluid(), fill);
		}
		assert false;
		return null;
	}

	protected void setInput(Object rawInput)
	{
		ICannerBottleRecipeManager.RawInput rawInput1;
		IFillFluidContainerRecipeManager.Input input1;
		ICannerEnrichRecipeManager.RawInput input;
		InvSlotConsumableCanner canInputSlot = ((TileEntityCanner) this.base).canInputSlot;
		FluidTank tank = ((TileEntityCanner) this.base).inputTank;
		switch (((TileEntityCanner) this.base).getMode())
		{
			case BottleSolid:
				rawInput1 = (ICannerBottleRecipeManager.RawInput) rawInput;
				canInputSlot.put(rawInput1.container);
				put(rawInput1.fill);
				return;
			case BottleLiquid:
				input1 = (IFillFluidContainerRecipeManager.Input) rawInput;
				canInputSlot.put(input1.container);
				tank.drain((input1.fluid == null) ? tank.getFluidAmount() : (tank.getFluidAmount() - input1.fluid.amount), true);
				return;
			case EmptyLiquid:
				canInputSlot.put((ItemStack) rawInput);
				return;
			case EnrichLiquid:
				input = (ICannerEnrichRecipeManager.RawInput) rawInput;
				put(input.additive);
				tank.drain((input.fluid == null) ? tank.getFluidAmount() : (tank.getFluidAmount() - input.fluid.amount), true);
				return;
		}
		assert false;
	}

	protected boolean allowEmptyInput()
	{
		return true;
	}

	protected MachineRecipeResult<Object, Object, Object> getOutputFor(Object input, boolean forAccept)
	{
		return getOutput(input, forAccept);
	}

	protected MachineRecipeResult<Object, Object, Object> getOutput(Object input, boolean forAccept)
	{
		switch (((TileEntityCanner) this.base).getMode())
		{
			case BottleSolid:
				return Recipes.cannerBottle.apply(input, forAccept);
			case BottleLiquid:
				return Recipes.fillFluidContainer.apply((IFillFluidContainerRecipeManager.Input) input, FluidContainerOutputMode.EmptyFullToOutput, forAccept);
			case EmptyLiquid:
				return Recipes.emptyFluidContainer.apply((ItemStack) input, (getTankFluid() == null) ? null : getTankFluid().getFluid(), FluidContainerOutputMode.EmptyFullToOutput, forAccept);
			case EnrichLiquid:
				return Recipes.cannerEnrich.apply(input, forAccept);
		}
		assert false;
		return null;
	}

	private FluidStack getTankFluid()
	{
		return ((TileEntityCanner) this.base).inputTank.getFluid();
	}
}
