// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import java.util.Collections;
import java.util.Iterator;
import net.minecraftforge.fluids.FluidRegistry;
import java.util.HashSet;
import java.util.Set;
import net.minecraftforge.fluids.Fluid;
import java.util.HashMap;
import java.util.Map;
import ic2.api.recipe.ISemiFluidFuelManager;

public class SemiFluidFuelManager implements ISemiFluidFuelManager
{
    private final Map<String, FuelProperty> fuelProperties;
    
    public SemiFluidFuelManager() {
        this.fuelProperties = new HashMap<String, FuelProperty>();
    }
    
    @Override
    public void addFluid(final String fluidName, final long energyPerMb, final long energyPerTick) {
        if (this.fuelProperties.containsKey(fluidName)) {
            throw new RuntimeException("The fluid " + fluidName + " does already have a fuel property assigned.");
        }
        this.fuelProperties.put(fluidName, new FuelProperty(energyPerMb, energyPerTick));
    }
    
    @Override
    public void removeFluid(final String fluidName) {
        this.fuelProperties.remove(fluidName);
    }
    
    @Override
    public FuelProperty getFuelProperty(final Fluid fluid) {
        if (fluid == null) {
            return null;
        }
        return this.fuelProperties.get(fluid.getName());
    }
    
    @Override
    public boolean acceptsFluid(final Fluid fluid) {
        return fluid != null && this.fuelProperties.containsKey(fluid.getName());
    }
    
    @Override
    public Set<Fluid> getAcceptedFluids() {
        final Set<Fluid> ret = new HashSet<Fluid>(this.fuelProperties.size() * 2, 0.5f);
        for (final String fluidName : this.fuelProperties.keySet()) {
            final Fluid fluid = FluidRegistry.getFluid(fluidName);
            if (fluid != null) {
                ret.add(fluid);
            }
        }
        return ret;
    }
    
    @Override
    public Map<String, FuelProperty> getFuelProperties() {
        return Collections.unmodifiableMap((Map<? extends String, ? extends FuelProperty>)this.fuelProperties);
    }
}
