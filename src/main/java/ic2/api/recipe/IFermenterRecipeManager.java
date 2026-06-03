package ic2.api.recipe;

import java.util.Map;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public interface IFermenterRecipeManager extends ILiquidAcceptManager {
  void addRecipe(String paramString1, int paramInt1, int paramInt2, String paramString2, int paramInt3);
  
  FermentationProperty getFermentationInformation(Fluid paramFluid);
  
  FluidStack getOutput(Fluid paramFluid);
  
  Map<String, FermentationProperty> getRecipeMap();
  
  public static final class FermentationProperty {
    public final int inputAmount;
    
    public final int heat;
    
    public final String output;
    
    public final int outputAmount;
    
    public FermentationProperty(int inputAmount, int heat, String output, int outputAmount) {
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
