package ic2.api.recipe;

import ic2.core.fluid.Ic2FluidStack;
import net.minecraft.world.item.ItemStack;

public interface ICannerEnrichRecipeManager extends IMachineRecipeManager<ICannerEnrichRecipeManager.Input, Ic2FluidStack, ICannerEnrichRecipeManager.RawInput>
{
	@Deprecated
	RecipeOutput getOutputFor(Ic2FluidStack var1, ItemStack var2, boolean var3, boolean var4);

	record Input(Ic2FluidStack fluid, IRecipeInput additive)
		{

			public boolean matches(Ic2FluidStack fluid, ItemStack additive)
			{
				return this.fluid.hasExactFluid(fluid) && this.additive.matches(additive);
			}
		}

	record RawInput(Ic2FluidStack fluid, ItemStack additive)
		{
		}
}
