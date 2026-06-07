package ic2.core.block.machine;

import ic2.api.recipe.IFillFluidContainerRecipeManager;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.util.FluidContainerOutputMode;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class FillFluidContainerRecipeManager implements IFillFluidContainerRecipeManager
{
	public boolean addRecipe(Void input, Collection<ItemStack> output, CompoundTag metadata, boolean replace)
	{
		return false;
	}

	public MachineRecipeResult<Void, Collection<ItemStack>, IFillFluidContainerRecipeManager.Input> apply(
		IFillFluidContainerRecipeManager.Input input, boolean acceptTest
	)
	{
		return this.apply(input, FluidContainerOutputMode.AnyToOutput, acceptTest);
	}

	@Override
	public MachineRecipeResult<Void, Collection<ItemStack>, IFillFluidContainerRecipeManager.Input> apply(
		IFillFluidContainerRecipeManager.Input input, FluidContainerOutputMode outputMode, boolean acceptTest
	)
	{
		if (!StackUtil.isEmpty(input.container) && input.fluid != null)
		{
			if (input.fluid.isEmpty())
			{
				return null;
			}

			LiquidUtil.FluidOperationResult result = LiquidUtil.fillContainer(input.container, input.fluid, outputMode);
			if (result == null)
			{
				return null;
			}

			Collection<ItemStack> output = StackUtil.isEmpty(result.extraOutput) ? Collections.emptyList() : Collections.singletonList(result.extraOutput);
			Ic2FluidStack changedFluid = result.fluidChange.getAmountMb() >= input.fluid.getAmountMb()
				? null
				: input.fluid.copyWithAmountMb(input.fluid.getAmountMb() - result.fluidChange.getAmountMb());
			return new MachineRecipe<>(null, output).getResult(new IFillFluidContainerRecipeManager.Input(result.inPlaceOutput, changedFluid));
		} else if (!acceptTest)
		{
			return null;
		} else if (StackUtil.isEmpty(input.container) && input.fluid == null)
		{
			return null;
		} else
		{
			return !StackUtil.isEmpty(input.container) && !LiquidUtil.isFillableFluidContainer(input.container)
				? null
				: new MachineRecipe<>(null, Collections.emptyList()).getResult(input);
		}
	}

	@Override
	public Iterable<? extends MachineRecipe<Void, Collection<ItemStack>>> getRecipes()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isIterable()
	{
		return false;
	}
}
