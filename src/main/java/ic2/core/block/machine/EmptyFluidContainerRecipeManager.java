package ic2.core.block.machine;

import ic2.api.recipe.IEmptyFluidContainerRecipeManager;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.util.FluidContainerOutputMode;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public class EmptyFluidContainerRecipeManager implements IEmptyFluidContainerRecipeManager
{
	public boolean addRecipe(Void input, IEmptyFluidContainerRecipeManager.Output output, CompoundTag metadata, boolean replace)
	{
		return false;
	}

	public MachineRecipeResult<Void, IEmptyFluidContainerRecipeManager.Output, ItemStack> apply(ItemStack input, boolean acceptTest)
	{
		return this.apply(input, null, FluidContainerOutputMode.AnyToOutput, acceptTest);
	}

	@Override
	public MachineRecipeResult<Void, IEmptyFluidContainerRecipeManager.Output, ItemStack> apply(
		ItemStack input, Fluid requiredFluid, FluidContainerOutputMode outputMode, boolean acceptTest
	)
	{
		if (StackUtil.isEmpty(input))
		{
			return null;
		}

		LiquidUtil.FluidOperationResult result = LiquidUtil.drainContainer(input, requiredFluid, Integer.MAX_VALUE, outputMode);
		if (result == null)
		{
			return null;
		}

		Collection<ItemStack> output = StackUtil.isEmpty(result.extraOutput) ? Collections.emptyList() : Collections.singletonList(result.extraOutput);
		return (MachineRecipeResult) new MachineRecipe<>(null, new IEmptyFluidContainerRecipeManager.Output(output, result.fluidChange)).getResult(result.inPlaceOutput);
	}

	@Override
	public Iterable<? extends MachineRecipe<Void, IEmptyFluidContainerRecipeManager.Output>> getRecipes()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isIterable()
	{
		return false;
	}
}
