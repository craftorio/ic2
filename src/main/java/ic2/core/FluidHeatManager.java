package ic2.core;

import ic2.api.recipe.IFluidHeatManager;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.level.material.Fluid;

public class FluidHeatManager implements IFluidHeatManager
{
	private final Map<Fluid, IFluidHeatManager.BurnProperty> burnProperties = new IdentityHashMap<>();

	@Override
	public void addFluid(Fluid fluid, int amount, int heat)
	{
		if (this.burnProperties.containsKey(fluid))
		{
			throw new RuntimeException("The fluid " + fluid + " does already have a burn property assigned.");
		}

		this.burnProperties.put(fluid, new IFluidHeatManager.BurnProperty(amount, heat));
	}

	@Override
	public IFluidHeatManager.BurnProperty getBurnProperty(Fluid fluid)
	{
		return fluid == null ? null : this.burnProperties.get(fluid);
	}

	@Override
	public boolean acceptsFluid(Fluid fluid)
	{
		return this.burnProperties.containsKey(fluid);
	}

	@Override
	public Set<Fluid> getAcceptedFluids()
	{
		return Collections.unmodifiableSet(this.burnProperties.keySet());
	}

	@Override
	public Map<Fluid, IFluidHeatManager.BurnProperty> getBurnProperties()
	{
		return Collections.unmodifiableMap(this.burnProperties);
	}
}
