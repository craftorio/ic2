// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import java.util.Collections;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.Fluid;
import java.util.HashMap;
import java.util.Map;
import ic2.api.recipe.IFermenterRecipeManager;

public class FermenterRecipeManager implements IFermenterRecipeManager
{
    private final Map<String, FermentationProperty> fluidMap;
    
    public FermenterRecipeManager() {
        this.fluidMap = new HashMap<String, FermentationProperty>();
    }
    
    @Override
    public void addRecipe(final String input, final int inputAmount, final int heat, final String output, final int outputAmount) {
        if (this.fluidMap.containsKey(input)) {
            throw new RuntimeException("The fluid " + input + " already has an output assigned.");
        }
        this.fluidMap.put(input, new FermentationProperty(inputAmount, heat, output, outputAmount));
    }
    
    @Override
    public FermentationProperty getFermentationInformation(final Fluid fluid) {
        return (fluid == null) ? null : this.fluidMap.get(fluid.getName());
    }
    
    @Override
    public FluidStack getOutput(final Fluid input) {
        final FermentationProperty fp = this.getFermentationInformation(input);
        if (fp == null) {
            return null;
        }
        return (FluidRegistry.getFluid(fp.output) == null) ? null : new FluidStack(FluidRegistry.getFluid(fp.output), fp.outputAmount);
    }
    
    @Override
    public boolean acceptsFluid(final Fluid fluid) {
        return fluid != null && this.fluidMap.containsKey(fluid.getName());
    }
    
    @Override
    public Set<Fluid> getAcceptedFluids() {
        final Set<Fluid> ret = new HashSet<Fluid>(this.fluidMap.size() * 2, 0.5f);
        for (final String fluidName : this.fluidMap.keySet()) {
            final Fluid fluid = FluidRegistry.getFluid(fluidName);
            if (fluid != null) {
                ret.add(fluid);
            }
        }
        return ret;
    }
    
    @Override
    public Map<String, FermentationProperty> getRecipeMap() {
        return Collections.unmodifiableMap((Map<? extends String, ? extends FermentationProperty>)this.fluidMap);
    }
}
