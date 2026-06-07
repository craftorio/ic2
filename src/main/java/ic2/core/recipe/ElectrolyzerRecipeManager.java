package ic2.core.recipe;

import ic2.api.recipe.IElectrolyzerRecipeManager;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.level.material.Fluid;

public class ElectrolyzerRecipeManager implements IElectrolyzerRecipeManager
{
	private final Map<Fluid, IElectrolyzerRecipeManager.ElectrolyzerRecipe> fluidMap = new IdentityHashMap<>();

	@Override
	public void addRecipe(Fluid input, int inputAmount, int EUaTick, IElectrolyzerRecipeManager.ElectrolyzerOutput... outputs)
	{
		this.addRecipe(input, inputAmount, EUaTick, 200, outputs);
	}

	@Override
	public void addRecipe(Fluid input, int inputAmount, int EUaTick, int ticksNeeded, IElectrolyzerRecipeManager.ElectrolyzerOutput... outputs)
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
		return fluid == null ? null : this.fluidMap.get(fluid);
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
		return fluid != null && this.fluidMap.containsKey(fluid);
	}

	@Override
	public Set<Fluid> getAcceptedFluids()
	{
		return Collections.unmodifiableSet(this.fluidMap.keySet());
	}

	@Override
	public Map<Fluid, IElectrolyzerRecipeManager.ElectrolyzerRecipe> getRecipeMap()
	{
		return Collections.unmodifiableMap(this.fluidMap);
	}
}
