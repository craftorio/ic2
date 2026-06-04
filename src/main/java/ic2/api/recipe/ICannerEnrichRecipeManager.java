package ic2.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface ICannerEnrichRecipeManager extends IMachineRecipeManager<ICannerEnrichRecipeManager.Input, FluidStack, ICannerEnrichRecipeManager.RawInput>
{
	@Deprecated
	void addRecipe(FluidStack paramFluidStack1, IRecipeInput paramIRecipeInput, FluidStack paramFluidStack2);

	@Deprecated
	RecipeOutput getOutputFor(FluidStack paramFluidStack, ItemStack paramItemStack, boolean paramBoolean1, boolean paramBoolean2);

	public static class Input
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
			return (this.fluid.isFluidEqual(fluid) && this.additive.matches(additive));
		}
	}

	public static class RawInput
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
