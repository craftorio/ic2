// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import java.util.Collections;
import java.util.Iterator;
import net.minecraftforge.fluids.FluidRegistry;
import java.util.HashSet;
import java.util.Set;
import net.minecraftforge.fluids.Fluid;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import ic2.api.recipe.IElectrolyzerRecipeManager;

public class ElectrolyzerRecipeManager implements IElectrolyzerRecipeManager
{
    private final Map<String, ElectrolyzerRecipe> fluidMap;
    
    public ElectrolyzerRecipeManager() {
        this.fluidMap = new HashMap<String, ElectrolyzerRecipe>();
    }
    
    @Override
    public void addRecipe(final String input, final int inputAmount, final int EUaTick, final ElectrolyzerOutput... outputs) {
        this.addRecipe(input, inputAmount, EUaTick, 200, outputs);
    }
    
    @Override
    public void addRecipe(@Nonnull final String input, final int inputAmount, final int EUaTick, final int ticksNeeded, @Nonnull final ElectrolyzerOutput... outputs) {
        if (this.fluidMap.containsKey(input)) {
            throw new RuntimeException("The fluid " + input + " already has an output assigned.");
        }
        this.fluidMap.put(input, new ElectrolyzerRecipe(inputAmount, EUaTick, ticksNeeded, outputs));
    }
    
    @Override
    public ElectrolyzerRecipe getElectrolysisInformation(final Fluid fluid) {
        return (fluid == null) ? null : this.fluidMap.get(fluid.getName());
    }
    
    @Override
    public ElectrolyzerOutput[] getOutput(final Fluid input) {
        final ElectrolyzerRecipe er = this.getElectrolysisInformation(input);
        return (ElectrolyzerOutput[])((er == null) ? null : er.outputs);
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
    public Map<String, ElectrolyzerRecipe> getRecipeMap() {
        return Collections.unmodifiableMap((Map<? extends String, ? extends ElectrolyzerRecipe>)this.fluidMap);
    }
}
