package ic2.api.recipe;

import ic2.core.fluid.Ic2FluidStack;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.tuple.Pair;

public interface IElectrolyzerRecipeManager extends ILiquidAcceptManager
{
	void addRecipe(Fluid var1, int var2, int var3, IElectrolyzerRecipeManager.ElectrolyzerOutput... var4);

	void addRecipe(Fluid var1, int var2, int var3, int var4, IElectrolyzerRecipeManager.ElectrolyzerOutput... var5);

	IElectrolyzerRecipeManager.ElectrolyzerRecipe getElectrolysisInformation(Fluid var1);

	IElectrolyzerRecipeManager.ElectrolyzerOutput[] getOutput(Fluid var1);

	Map<Fluid, IElectrolyzerRecipeManager.ElectrolyzerRecipe> getRecipeMap();

	record ElectrolyzerOutput(Fluid fluid, int fluidAmount, Direction tankDirection)
		{

			public Ic2FluidStack getOutput()
			{
				return this.fluid == null ? null : Ic2FluidStack.create(this.fluid, this.fluidAmount);
			}
	
			public Pair<Ic2FluidStack, Direction> getFullOutput()
			{
				return Pair.of(this.getOutput(), this.tankDirection);
			}
		}

	record ElectrolyzerRecipe(int inputAmount, int EUaTick, int ticksNeeded, ElectrolyzerOutput... outputs)
		{
			public ElectrolyzerRecipe(int inputAmount, int EUaTick, int ticksNeeded, ElectrolyzerOutput... outputs)
			{
				this.inputAmount = inputAmount;
				this.EUaTick = EUaTick;
				this.ticksNeeded = ticksNeeded;
				this.outputs = this.validateOutputs(outputs);
			}
	
			private ElectrolyzerOutput[] validateOutputs(ElectrolyzerOutput[] outputs)
			{
				if (outputs.length >= 1 && outputs.length <= 5)
				{
					Set<Direction> directions = new HashSet<>(outputs.length * 2, 0.5F);
	
					for (ElectrolyzerOutput output : outputs)
					{
						if (!directions.add(output.tankDirection))
						{
							throw new RuntimeException("Duplicate direction in Electrolzer outputs (" + output.tankDirection + ")");
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
