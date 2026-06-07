package ic2.core.recipe;

import ic2.api.recipe.IFermenterRecipeManager;
import ic2.core.fluid.Ic2FluidStack;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.level.material.Fluid;

public class FermenterRecipeManager implements IFermenterRecipeManager
{
	private final Map<Fluid, IFermenterRecipeManager.FermentationProperty> fluidMap = new IdentityHashMap<>();

	@Override
	public void addRecipe(Fluid input, int inputAmount, int heat, Fluid output, int outputAmount)
	{
		if (this.fluidMap.containsKey(input))
		{
			throw new RuntimeException("The fluid " + input + " already has an output assigned.");
		}

		this.fluidMap.put(input, new IFermenterRecipeManager.FermentationProperty(inputAmount, heat, output, outputAmount));
	}

	@Override
	public IFermenterRecipeManager.FermentationProperty getFermentationInformation(Fluid fluid)
	{
		return fluid == null ? null : this.fluidMap.get(fluid);
	}

	@Override
	public Ic2FluidStack getOutput(Fluid input)
	{
		IFermenterRecipeManager.FermentationProperty fp = this.getFermentationInformation(input);
		if (fp == null)
		{
			return null;
		} else
		{
			return fp.output == null ? null : Ic2FluidStack.create(fp.output, fp.outputAmount);
		}
	}

	@Override
	public boolean acceptsFluid(Fluid fluid)
	{
		return fluid != null && this.fluidMap.containsKey(fluid);
	}

	@Override
	public Set<Fluid> getAcceptedFluids()
	{
		return Collections.unmodifiableSet(this.fluidMap.keySet());
	}

	@Override
	public Map<Fluid, IFermenterRecipeManager.FermentationProperty> getRecipeMap()
	{
		return Collections.unmodifiableMap(this.fluidMap);
	}
}
