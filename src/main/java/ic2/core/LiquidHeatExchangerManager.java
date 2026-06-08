package ic2.core;

import ic2.api.recipe.ILiquidAcceptManager;
import ic2.api.recipe.ILiquidHeatExchangerManager;
import ic2.api.recipe.Recipes;
import ic2.core.fluid.FluidHandler;
import ic2.core.init.MainConfig;
import ic2.core.util.LogCategory;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.minecraft.world.level.material.Fluid;

public class LiquidHeatExchangerManager implements ILiquidHeatExchangerManager
{
	private final boolean heatup;
	private final LiquidHeatExchangerManager.SingleDirectionManager singleDirectionManager;
	private final Map<Fluid, ILiquidHeatExchangerManager.HeatExchangeProperty> map = new IdentityHashMap<>();

	public LiquidHeatExchangerManager(boolean heatup)
	{
		this.heatup = heatup;
		this.singleDirectionManager = new LiquidHeatExchangerManager.SingleDirectionManager();
	}

	@Override
	public boolean acceptsFluid(Fluid fluid)
	{
		return this.map.containsKey(fluid);
	}

	@Override
	public Set<Fluid> getAcceptedFluids()
	{
		return Collections.unmodifiableSet(this.map.keySet());
	}

	@Override
	public void addFluid(Fluid input, Fluid output, int huPerMB)
	{
		if (this.map.containsKey(input))
		{
			this.displayError("The fluid " + input + " does already have a HeatExchangerProperty assigned.");
		} else if (huPerMB == 0)
		{
			this.displayError("A mod tried to register a Fluid for the HeatExchanging recipe, without having an Energy value. Ignoring...");
		} else
		{
			if (this.heatup)
			{
				if (FluidHandler.getTemperature(input) >= FluidHandler.getTemperature(output))
				{
					this.displayError("Cannot heat up a warm liquid into a cold one. " + input + " -> " + output);
				}
			} else if (FluidHandler.getTemperature(input) <= FluidHandler.getTemperature(output))
			{
				this.displayError("Cannot cool down a cold liquid into a warm one. " + input + " -> " + output);
			}

			this.map.put(input, new ILiquidHeatExchangerManager.HeatExchangeProperty(output, Math.abs(huPerMB)));
		}
	}

	@Override
	public ILiquidHeatExchangerManager.HeatExchangeProperty getHeatExchangeProperty(Fluid fluid)
	{
		return this.map.get(fluid);
	}

	@Override
	public Map<Fluid, ILiquidHeatExchangerManager.HeatExchangeProperty> getHeatExchangeProperties()
	{
		return this.map;
	}

	private void displayError(String msg)
	{
		if (MainConfig.ignoreInvalidRecipes)
		{
			IC2.log.warn(LogCategory.Recipe, msg);
		} else
		{
			throw new RuntimeException(msg);
		}
	}

	@Override
	public ILiquidAcceptManager getSingleDirectionLiquidManager()
	{
		return this.singleDirectionManager;
	}

	public ILiquidHeatExchangerManager getOpposite()
	{
		return this.heatup ? Recipes.liquidCooldownManager : Recipes.liquidHeatUpManager;
	}

	public class SingleDirectionManager implements ILiquidAcceptManager
	{
		@Override
		public boolean acceptsFluid(Fluid fluid)
		{
			if (!LiquidHeatExchangerManager.this.acceptsFluid(fluid))
			{
				return false;
			}

			ILiquidHeatExchangerManager.HeatExchangeProperty property = LiquidHeatExchangerManager.this.getHeatExchangeProperty(fluid);
			return !LiquidHeatExchangerManager.this.getOpposite().acceptsFluid(property.outputFluid);
		}

		@Override
		public Set<Fluid> getAcceptedFluids()
		{
			Set<Fluid> ret = new HashSet<>();
			ILiquidHeatExchangerManager opposite = LiquidHeatExchangerManager.this.getOpposite();

			for (Entry<Fluid, ILiquidHeatExchangerManager.HeatExchangeProperty> e : LiquidHeatExchangerManager.this.map.entrySet())
			{
				if (!opposite.acceptsFluid(e.getValue().outputFluid))
				{
					ret.add(e.getKey());
				}
			}

			return ret;
		}
	}
}
