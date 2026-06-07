package ic2.core;

import ic2.api.recipe.ISemiFluidFuelManager;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.level.material.Fluid;

public class SemiFluidFuelManager implements ISemiFluidFuelManager
{
	private final Map<Fluid, ISemiFluidFuelManager.BurnProperty> burnProperties = new IdentityHashMap<>();

	@Override
	public void addFluid(Fluid fluid, int amount, double power)
	{
		if (this.burnProperties.containsKey(fluid))
		{
			throw new RuntimeException("The fluid " + fluid + " does already have a burn property assigned.");
		}

		this.burnProperties.put(fluid, new ISemiFluidFuelManager.BurnProperty(amount, power));
	}

	@Override
	public ISemiFluidFuelManager.BurnProperty getBurnProperty(Fluid fluid)
	{
		return fluid == null ? null : this.burnProperties.get(fluid);
	}

	@Override
	public boolean acceptsFluid(Fluid fluid)
	{
		return fluid != null && this.burnProperties.containsKey(fluid);
	}

	@Override
	public Set<Fluid> getAcceptedFluids()
	{
		return Collections.unmodifiableSet(this.burnProperties.keySet());
	}

	@Override
	public Map<Fluid, ISemiFluidFuelManager.BurnProperty> getBurnProperties()
	{
		return Collections.unmodifiableMap(this.burnProperties);
	}
}
