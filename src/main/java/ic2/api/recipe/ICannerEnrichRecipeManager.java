package ic2.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface ICannerEnrichRecipeManager extends IMachineRecipeManager<ICannerEnrichRecipeManager.Input, FluidStack, ICannerEnrichRecipeManager.RawInput>
{
	@Deprecated
	void addRecipe(FluidStack var1, IRecipeInput var2, FluidStack var3);

	@Deprecated
	RecipeOutput getOutputFor(FluidStack var1, ItemStack var2, boolean var3, boolean var4);

	class Input
	{
		public final FluidStack fluid;
		public final IRecipeInput additive;

		public Input(FluidStack fluid, IRecipeInput additive)
		{
			this.fluid = fluid;
			this.additive = additive;
		}

		public boolean matches(FluidStack fluid, ItemStack additive)
		{
			return this.fluid.isFluidEqual(fluid) && this.additive.matches(additive);
		}
	}

	class RawInput
	{
		public final FluidStack fluid;
		public final ItemStack additive;

		public RawInput(FluidStack fluid, ItemStack additive)
		{
			this.fluid = fluid;
			this.additive = additive;
		}
	}
}
