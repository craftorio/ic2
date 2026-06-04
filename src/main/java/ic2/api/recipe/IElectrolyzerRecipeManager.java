// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import java.util.Set;
import java.util.HashSet;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.util.EnumFacing;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import net.minecraftforge.fluids.Fluid;
import javax.annotation.Nonnull;

public interface IElectrolyzerRecipeManager extends ILiquidAcceptManager
{
    void addRecipe(@Nonnull final String p0, final int p1, final int p2, @Nonnull final ElectrolyzerOutput... p3);
    
    void addRecipe(@Nonnull final String p0, final int p1, final int p2, final int p3, @Nonnull final ElectrolyzerOutput... p4);
    
    ElectrolyzerRecipe getElectrolysisInformation(final Fluid p0);
    
    ElectrolyzerOutput[] getOutput(final Fluid p0);
    
    Map<String, ElectrolyzerRecipe> getRecipeMap();
    
    @ParametersAreNonnullByDefault
    public static final class ElectrolyzerOutput
    {
        public final String fluidName;
        public final int fluidAmount;
        public final EnumFacing tankDirection;
        
        public ElectrolyzerOutput(final String fluidName, final int fluidAmount, final EnumFacing tankDirection) {
            this.fluidName = fluidName;
            this.fluidAmount = fluidAmount;
            this.tankDirection = tankDirection;
        }
        
        public FluidStack getOutput() {
            return (FluidRegistry.getFluid(this.fluidName) == null) ? null : new FluidStack(FluidRegistry.getFluid(this.fluidName), this.fluidAmount);
        }
        
        public Pair<FluidStack, EnumFacing> getFullOutput() {
            return (Pair<FluidStack, EnumFacing>)Pair.of((Object)this.getOutput(), (Object)this.tankDirection);
        }
    }
    
    public static final class ElectrolyzerRecipe
    {
        public final int inputAmount;
        public final int EUaTick;
        public final int ticksNeeded;
        public final ElectrolyzerOutput[] outputs;
        
        public ElectrolyzerRecipe(final int inputAmount, final int EUaTick, final int ticksNeeded, final ElectrolyzerOutput... outputs) {
            this.inputAmount = inputAmount;
            this.EUaTick = EUaTick;
            this.ticksNeeded = ticksNeeded;
            this.outputs = this.validateOutputs(outputs);
        }
        
        private ElectrolyzerOutput[] validateOutputs(final ElectrolyzerOutput[] outputs) {
            if (outputs.length < 1 || outputs.length > 5) {
                throw new RuntimeException("Cannot have " + outputs.length + " outputs of an Electrolzer recipe, must be between 1 and 5");
            }
            final Set<EnumFacing> directions = new HashSet<EnumFacing>(outputs.length * 2, 0.5f);
            for (final ElectrolyzerOutput output : outputs) {
                if (!directions.add(output.tankDirection)) {
                    throw new RuntimeException("Duplicate direction in Electrolzer outputs (" + output.tankDirection + ')');
                }
            }
            return outputs;
        }
    }
}
