package ic2.core.block.invslot;

import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IFillFluidContainerRecipeManager;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.util.FluidContainerOutputMode;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.util.StackUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class InvSlotProcessableCanner extends InvSlotProcessable<Object, Object, Object>
{
	public InvSlotProcessableCanner(TileEntityCanner base1, String name1, int count)
	{
		super(base1, name1, count, null);
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		return switch (((TileEntityCanner) this.base).getMode())
		{
			case BottleSolid, EnrichLiquid -> super.accepts(stack);
			case BottleLiquid, EmptyLiquid -> false;
		};
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

		Ic2FluidStack fluid = ((TileEntityCanner) this.base).inputTank.getFluidStack();
		if (fluid != null && fluid.isEmpty())
		{
			((TileEntityCanner) this.base).inputTank.setFluidStack(null);
		}
	}

	@Override
	protected Object getInput(ItemStack fill)
	{
		ItemStack container = ((TileEntityCanner) this.base).canInputSlot.get();
		return switch (((TileEntityCanner) this.base).getMode())
		{
			case BottleSolid -> new ICannerBottleRecipeManager.RawInput(container, fill);
			case EnrichLiquid -> new ICannerEnrichRecipeManager.RawInput(this.getTankFluid(), fill);
			case BottleLiquid -> new IFillFluidContainerRecipeManager.Input(container, this.getTankFluid());
			case EmptyLiquid -> container;
		};
	}

	@Override
	protected void setInput(Object rawInput)
	{
		InvSlotConsumableCanner canInputSlot = ((TileEntityCanner) this.base).canInputSlot;
		Ic2FluidTank tank = ((TileEntityCanner) this.base).inputTank;
		switch (((TileEntityCanner) this.base).getMode())
		{
			case BottleSolid:
			{
				ICannerBottleRecipeManager.RawInput input = (ICannerBottleRecipeManager.RawInput) rawInput;
				canInputSlot.put(input.container());
				this.put(input.fill());
				break;
			}
			case EnrichLiquid:
			{
				ICannerEnrichRecipeManager.RawInput input = (ICannerEnrichRecipeManager.RawInput) rawInput;
				this.put(input.additive());
				tank.drainMbUnchecked(input.fluid() == null ? tank.getFluidAmount() : tank.getFluidAmount() - input.fluid().getAmountMb(), false);
				break;
			}
			case BottleLiquid:
			{
				IFillFluidContainerRecipeManager.Input input = (IFillFluidContainerRecipeManager.Input) rawInput;
				canInputSlot.put(input.container());
				tank.drainMbUnchecked(input.fluid() == null ? tank.getFluidAmount() : tank.getFluidAmount() - input.fluid().getAmountMb(), false);
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
		Level world = this.base.getParent().getLevel();
		return switch (((TileEntityCanner) this.base).getMode())
		{
			case BottleSolid ->
				(MachineRecipeResult) Recipes.cannerBottle.get(world).apply((ICannerBottleRecipeManager.RawInput) input, forAccept);
			case EnrichLiquid ->
				(MachineRecipeResult) Recipes.cannerEnrich.get(world).apply((ICannerEnrichRecipeManager.RawInput) input, forAccept);
			case BottleLiquid ->
				(MachineRecipeResult) Recipes.fillFluidContainer.apply((IFillFluidContainerRecipeManager.Input) input, FluidContainerOutputMode.EmptyFullToOutput, forAccept);
			case EmptyLiquid ->
				(MachineRecipeResult) Recipes.emptyFluidContainer.apply((ItemStack) input, this.getTankFluid() == null ? null : this.getTankFluid().getFluid(), FluidContainerOutputMode.EmptyFullToOutput, forAccept);
		};
	}

	private Ic2FluidStack getTankFluid()
	{
		return ((TileEntityCanner) this.base).inputTank.getFluidStack();
	}
}
