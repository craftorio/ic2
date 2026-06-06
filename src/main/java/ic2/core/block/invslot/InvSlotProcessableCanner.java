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

	@Override
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
			default:
				assert false;
				return false;
		}
	}

	@Override
	public void consume(MachineRecipeResult<Object, Object, Object> result)
	{
		super.consume(result);
		ItemStack containerStack = ((TileEntityCanner) this.base).canInputSlot.get();
		if (StackUtil.isEmpty(containerStack))
		{
			((TileEntityCanner) this.base).canInputSlot.clear();
		}

		FluidStack fluid = ((TileEntityCanner) this.base).inputTank.getFluid();
		if (fluid != null && fluid.amount <= 0)
		{
			((TileEntityCanner) this.base).inputTank.setFluid(null);
		}
	}

	@Override
	protected Object getInput(ItemStack fill)
	{
		ItemStack container = ((TileEntityCanner) this.base).canInputSlot.get();
		switch (((TileEntityCanner) this.base).getMode())
		{
			case BottleSolid:
				return new ICannerBottleRecipeManager.RawInput(container, fill);
			case EnrichLiquid:
				return new ICannerEnrichRecipeManager.RawInput(this.getTankFluid(), fill);
			case BottleLiquid:
				return new IFillFluidContainerRecipeManager.Input(container, this.getTankFluid());
			case EmptyLiquid:
				return container;
			default:
				assert false;
				return null;
		}
	}

	@Override
	protected void setInput(Object rawInput)
	{
		InvSlotConsumableCanner canInputSlot = ((TileEntityCanner) this.base).canInputSlot;
		FluidTank tank = ((TileEntityCanner) this.base).inputTank;
		switch (((TileEntityCanner) this.base).getMode())
		{
			case BottleSolid:
			{
				ICannerBottleRecipeManager.RawInput input = (ICannerBottleRecipeManager.RawInput) rawInput;
				canInputSlot.put(input.container);
				this.put(input.fill);
				break;
			}
			case EnrichLiquid:
			{
				ICannerEnrichRecipeManager.RawInput input = (ICannerEnrichRecipeManager.RawInput) rawInput;
				this.put(input.additive);
				tank.drain(input.fluid == null ? tank.getFluidAmount() : tank.getFluidAmount() - input.fluid.amount, true);
				break;
			}
			case BottleLiquid:
			{
				IFillFluidContainerRecipeManager.Input input = (IFillFluidContainerRecipeManager.Input) rawInput;
				canInputSlot.put(input.container);
				tank.drain(input.fluid == null ? tank.getFluidAmount() : tank.getFluidAmount() - input.fluid.amount, true);
				break;
			}
			case EmptyLiquid:
				canInputSlot.put((ItemStack) rawInput);
				break;
			default:
				assert false;
		}
	}

	@Override
	protected boolean allowEmptyInput()
	{
		return true;
	}

	@Override
	protected MachineRecipeResult<Object, Object, Object> getOutputFor(Object input, boolean forAccept)
	{
		return this.getOutput(input, forAccept);
	}

	protected MachineRecipeResult<Object, Object, Object> getOutput(Object input, boolean forAccept)
	{
		switch (((TileEntityCanner) this.base).getMode())
		{
			case BottleSolid:
				return (MachineRecipeResult) Recipes.cannerBottle.apply((ICannerBottleRecipeManager.RawInput) input, forAccept);
			case EnrichLiquid:
				return (MachineRecipeResult) Recipes.cannerEnrich.apply((ICannerEnrichRecipeManager.RawInput) input, forAccept);
			case BottleLiquid:
				return (MachineRecipeResult) Recipes.fillFluidContainer.apply((IFillFluidContainerRecipeManager.Input) input, FluidContainerOutputMode.EmptyFullToOutput, forAccept);
			case EmptyLiquid:
				return (MachineRecipeResult) Recipes.emptyFluidContainer
					.apply(
						(ItemStack) input, this.getTankFluid() == null ? null : this.getTankFluid().getFluid(), FluidContainerOutputMode.EmptyFullToOutput, forAccept
					);
			default:
				assert false;
				return null;
		}
	}

	private FluidStack getTankFluid()
	{
		return ((TileEntityCanner) this.base).inputTank.getFluid();
	}
}
