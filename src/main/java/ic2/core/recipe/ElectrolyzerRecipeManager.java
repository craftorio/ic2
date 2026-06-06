package ic2.core.recipe;

import ic2.api.recipe.IElectrolyzerRecipeManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class ElectrolyzerRecipeManager implements IElectrolyzerRecipeManager
{
	private final Map<String, IElectrolyzerRecipeManager.ElectrolyzerRecipe> fluidMap = new HashMap<>();

	@Override
	public void addRecipe(String input, int inputAmount, int EUaTick, IElectrolyzerRecipeManager.ElectrolyzerOutput... outputs)
	{
		this.addRecipe(input, inputAmount, EUaTick, 200, outputs);
	}

	@Override
	public void addRecipe(
		@Nonnull String input, int inputAmount, int EUaTick, int ticksNeeded, @Nonnull IElectrolyzerRecipeManager.ElectrolyzerOutput... outputs
	)
	{
		if (this.fluidMap.containsKey(input))
		{
			throw new RuntimeException("The fluid " + input + " already has an output assigned.");
		}

		this.fluidMap.put(input, new IElectrolyzerRecipeManager.ElectrolyzerRecipe(inputAmount, EUaTick, ticksNeeded, outputs));
	}

	@Override
	public IElectrolyzerRecipeManager.ElectrolyzerRecipe getElectrolysisInformation(Fluid fluid)
	{
		return fluid == null ? null : this.fluidMap.get(fluid.getName());
	}

	@Override
	public IElectrolyzerRecipeManager.ElectrolyzerOutput[] getOutput(Fluid input)
	{
		IElectrolyzerRecipeManager.ElectrolyzerRecipe er = this.getElectrolysisInformation(input);
		return er == null ? null : er.outputs;
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
	public Map<String, IElectrolyzerRecipeManager.ElectrolyzerRecipe> getRecipeMap()
	{
		return Collections.unmodifiableMap(this.fluidMap);
	}
}
