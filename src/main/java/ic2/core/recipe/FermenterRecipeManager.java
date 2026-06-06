package ic2.core.recipe;

import ic2.api.recipe.IFermenterRecipeManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class FermenterRecipeManager implements IFermenterRecipeManager
{
	private final Map<String, IFermenterRecipeManager.FermentationProperty> fluidMap = new HashMap<>();

	@Override
	public void addRecipe(String input, int inputAmount, int heat, String output, int outputAmount)
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
		return fluid == null ? null : this.fluidMap.get(fluid.getName());
	}

	@Override
	public FluidStack getOutput(Fluid input)
	{
		IFermenterRecipeManager.FermentationProperty fp = this.getFermentationInformation(input);
		if (fp == null)
		{
			return null;
		} else
		{
			return FluidRegistry.getFluid(fp.output) == null ? null : new FluidStack(FluidRegistry.getFluid(fp.output), fp.outputAmount);
		}
	}

	@Override
	public boolean acceptsFluid(Fluid fluid)
	{
		return fluid != null && this.fluidMap.containsKey(fluid.getName());
	}

	@Override
	public Set<Fluid> getAcceptedFluids()
	{
		Set<Fluid> ret = new HashSet<>(this.fluidMap.size() * 2, 0.5F);

		for (String fluidName : this.fluidMap.keySet())
		{
			Fluid fluid = FluidRegistry.getFluid(fluidName);
			if (fluid != null)
			{
				ret.add(fluid);
			}
		}

		return ret;
	}

	@Override
	public Map<String, IFermenterRecipeManager.FermentationProperty> getRecipeMap()
	{
		return Collections.unmodifiableMap(this.fluidMap);
	}
}
