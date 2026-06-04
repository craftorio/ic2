// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import ic2.api.recipe.Recipes;
import ic2.api.recipe.ILiquidAcceptManager;
import ic2.core.util.LogCategory;
import ic2.core.init.MainConfig;
import java.util.Iterator;
import net.minecraftforge.fluids.FluidRegistry;
import java.util.HashSet;
import java.util.Set;
import net.minecraftforge.fluids.Fluid;
import java.util.HashMap;
import java.util.Map;
import ic2.api.recipe.ILiquidHeatExchangerManager;

public class LiquidHeatExchangerManager implements ILiquidHeatExchangerManager
{
    private final boolean heatup;
    private final SingleDirectionManager singleDirectionManager;
    private Map<String, HeatExchangeProperty> map;
    
    public LiquidHeatExchangerManager(final boolean heatup) {
        this.map = new HashMap<String, HeatExchangeProperty>();
        this.heatup = heatup;
        this.singleDirectionManager = new SingleDirectionManager();
    }
    
    @Override
    public boolean acceptsFluid(final Fluid fluid) {
        return this.map.containsKey(fluid.getName());
    }
    
    @Override
    public Set<Fluid> getAcceptedFluids() {
        final Set<Fluid> fluidSet = new HashSet<Fluid>();
        for (final String fluidName : this.map.keySet()) {
            fluidSet.add(FluidRegistry.getFluid(fluidName));
        }
        return fluidSet;
    }
    
    @Override
    public void addFluid(final String fluidName, final String fluidOutput, final int huPerMB) {
        if (this.map.containsKey(fluidName)) {
            this.displayError("The fluid " + fluidName + " does already have a HeatExchangerProperty assigned.");
            return;
        }
        if (huPerMB == 0) {
            this.displayError("A mod tried to register a Fluid for the HeatExchanging recipe, without having an Energy value. Ignoring...");
            return;
        }
        final Fluid liquid1 = FluidRegistry.getFluid(fluidName);
        final Fluid liquid2 = FluidRegistry.getFluid(fluidOutput);
        if (liquid1 == null || liquid2 == null) {
            this.displayError("Could not get both fluids for " + fluidName + " and " + fluidOutput + ".");
            return;
        }
        if (this.heatup) {
            if (liquid1.getTemperature() >= liquid2.getTemperature()) {
                this.displayError("Cannot heat up a warm liquid into a cold one. " + fluidName + " -> " + fluidOutput);
            }
        }
        else if (liquid1.getTemperature() <= liquid2.getTemperature()) {
            this.displayError("Cannot cool down a cold liquid into a warm one. " + fluidName + " -> " + fluidOutput);
        }
        this.map.put(fluidName, new HeatExchangeProperty(FluidRegistry.getFluid(fluidOutput), Math.abs(huPerMB)));
    }
    
    @Override
    public HeatExchangeProperty getHeatExchangeProperty(final Fluid fluid) {
        if (this.map.containsKey(fluid.getName())) {
            return this.map.get(fluid.getName());
        }
        return null;
    }
    
    @Override
    public Map<String, HeatExchangeProperty> getHeatExchangeProperties() {
        return this.map;
    }
    
    private void displayError(final String msg) {
        if (MainConfig.ignoreInvalidRecipes) {
            IC2.log.warn(LogCategory.Recipe, msg);
            return;
        }
        throw new RuntimeException(msg);
    }
    
    @Override
    public ILiquidAcceptManager getSingleDirectionLiquidManager() {
        return this.singleDirectionManager;
    }
    
    public ILiquidHeatExchangerManager getOpposite() {
        return this.heatup ? Recipes.liquidCooldownManager : Recipes.liquidHeatupManager;
    }
    
    public class SingleDirectionManager implements ILiquidAcceptManager
    {
        @Override
        public boolean acceptsFluid(final Fluid fluid) {
            if (!LiquidHeatExchangerManager.this.acceptsFluid(fluid)) {
                return false;
            }
            final HeatExchangeProperty property = LiquidHeatExchangerManager.this.getHeatExchangeProperty(fluid);
            return !LiquidHeatExchangerManager.this.getOpposite().acceptsFluid(property.outputFluid);
        }
        
        @Override
        public Set<Fluid> getAcceptedFluids() {
            final Set<Fluid> ret = new HashSet<Fluid>();
            final ILiquidHeatExchangerManager opposite = LiquidHeatExchangerManager.this.getOpposite();
            for (final Map.Entry<String, HeatExchangeProperty> e : LiquidHeatExchangerManager.this.map.entrySet()) {
                if (!opposite.acceptsFluid(e.getValue().outputFluid)) {
                    ret.add(FluidRegistry.getFluid((String)e.getKey()));
                }
            }
            return ret;
        }
    }
}
