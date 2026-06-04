// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import net.minecraftforge.fluids.FluidRegistry;
import java.util.Map;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.Fluid;

public interface IFermenterRecipeManager extends ILiquidAcceptManager
{
    void addRecipe(final String p0, final int p1, final int p2, final String p3, final int p4);
    
    FermentationProperty getFermentationInformation(final Fluid p0);
    
    FluidStack getOutput(final Fluid p0);
    
    Map<String, FermentationProperty> getRecipeMap();
    
    public static final class FermentationProperty
    {
        public final int inputAmount;
        public final int heat;
        public final String output;
        public final int outputAmount;
        
        public FermentationProperty(final int inputAmount, final int heat, final String output, final int outputAmount) {
            this.inputAmount = inputAmount;
            this.heat = heat;
            this.output = output;
            this.outputAmount = outputAmount;
        }
        
        public FluidStack getOutput() {
            return (FluidRegistry.getFluid(this.output) == null) ? null : new FluidStack(FluidRegistry.getFluid(this.output), this.outputAmount);
        }
    }
}
