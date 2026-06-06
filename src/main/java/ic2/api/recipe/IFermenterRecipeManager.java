package ic2.api.recipe;

import java.util.Map;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public interface IFermenterRecipeManager extends ILiquidAcceptManager
{
	void addRecipe(String var1, int var2, int var3, String var4, int var5);

	IFermenterRecipeManager.FermentationProperty getFermentationInformation(Fluid var1);

	FluidStack getOutput(Fluid var1);

	Map<String, IFermenterRecipeManager.FermentationProperty> getRecipeMap();

	final class FermentationProperty
	{
		public final int inputAmount;
		public final int heat;
		public final String output;
		public final int outputAmount;

		public FermentationProperty(int inputAmount, int heat, String output, int outputAmount)
		{
			this.inputAmount = inputAmount;
			this.heat = heat;
			this.output = output;
			this.outputAmount = outputAmount;
		}

		public FluidStack getOutput()
		{
			return FluidRegistry.getFluid(this.output) == null ? null : new FluidStack(FluidRegistry.getFluid(this.output), this.outputAmount);
		}
	}
}
