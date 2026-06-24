package ic2.integration.jei.recipe.machine;

import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.MachineRecipe;
import ic2.core.fluid.Ic2FluidStack;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class CannerEnrichRecipeWrapper
{
	private final Ic2FluidStack fluidInput;
	private final List<ItemStack> additiveInputs;
	private final Ic2FluidStack fluidOutput;

	public CannerEnrichRecipeWrapper(MachineRecipe<ICannerEnrichRecipeManager.Input, Ic2FluidStack> recipe)
	{
		this.fluidInput = recipe.getInput().fluid();
		this.additiveInputs = recipe.getInput().additive().getInputs();
		this.fluidOutput = recipe.getOutput();
	}

	public FluidStack getFluidInput()
	{
		return new FluidStack(this.fluidInput.getFluid(), this.fluidInput.getAmountMb());
	}

	public List<ItemStack> getAdditiveInputs()
	{
		return this.additiveInputs;
	}

	public FluidStack getFluidOutput()
	{
		return new FluidStack(this.fluidOutput.getFluid(), this.fluidOutput.getAmountMb());
	}
}
