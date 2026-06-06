package ic2.api.recipe;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

public interface IElectrolyzerRecipeManager extends ILiquidAcceptManager
{
	void addRecipe(@Nonnull String var1, int var2, int var3, @Nonnull IElectrolyzerRecipeManager.ElectrolyzerOutput... var4);

	void addRecipe(@Nonnull String var1, int var2, int var3, int var4, @Nonnull IElectrolyzerRecipeManager.ElectrolyzerOutput... var5);

	IElectrolyzerRecipeManager.ElectrolyzerRecipe getElectrolysisInformation(Fluid var1);

	IElectrolyzerRecipeManager.ElectrolyzerOutput[] getOutput(Fluid var1);

	Map<String, IElectrolyzerRecipeManager.ElectrolyzerRecipe> getRecipeMap();

	@ParametersAreNonnullByDefault
	final class ElectrolyzerOutput
	{
		public final String fluidName;
		public final int fluidAmount;
		public final EnumFacing tankDirection;

		public ElectrolyzerOutput(String fluidName, int fluidAmount, EnumFacing tankDirection)
		{
			this.fluidName = fluidName;
			this.fluidAmount = fluidAmount;
			this.tankDirection = tankDirection;
		}

		public FluidStack getOutput()
		{
			return FluidRegistry.getFluid(this.fluidName) == null ? null : new FluidStack(FluidRegistry.getFluid(this.fluidName), this.fluidAmount);
		}

		public Pair<FluidStack, EnumFacing> getFullOutput()
		{
			return Pair.of(this.getOutput(), this.tankDirection);
		}
	}

	final class ElectrolyzerRecipe
	{
		public final int inputAmount;
		public final int EUaTick;
		public final int ticksNeeded;
		public final IElectrolyzerRecipeManager.ElectrolyzerOutput[] outputs;

		public ElectrolyzerRecipe(int inputAmount, int EUaTick, int ticksNeeded, IElectrolyzerRecipeManager.ElectrolyzerOutput... outputs)
		{
			this.inputAmount = inputAmount;
			this.EUaTick = EUaTick;
			this.ticksNeeded = ticksNeeded;
			this.outputs = this.validateOutputs(outputs);
		}

		private IElectrolyzerRecipeManager.ElectrolyzerOutput[] validateOutputs(IElectrolyzerRecipeManager.ElectrolyzerOutput[] outputs)
		{
			if (outputs.length >= 1 && outputs.length <= 5)
			{
				Set<EnumFacing> directions = new HashSet<>(outputs.length * 2, 0.5F);

				for (IElectrolyzerRecipeManager.ElectrolyzerOutput output : outputs)
				{
					if (!directions.add(output.tankDirection))
					{
						throw new RuntimeException("Duplicate direction in Electrolzer outputs (" + output.tankDirection + ')');
					}
				}

				return outputs;
			} else
			{
				throw new RuntimeException("Cannot have " + outputs.length + " outputs of an Electrolzer recipe, must be between 1 and 5");
			}
		}
	}
}
