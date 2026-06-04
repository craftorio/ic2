package ic2.api.recipe;

import ic2.api.util.FluidContainerOutputMode;

import java.util.Collection;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IFillFluidContainerRecipeManager extends IMachineRecipeManager<Void, Collection<ItemStack>, IFillFluidContainerRecipeManager.Input>
{
	MachineRecipeResult<Void, Collection<ItemStack>, Input> apply(Input paramInput, FluidContainerOutputMode paramFluidContainerOutputMode, boolean paramBoolean);

	public static class Input
	{
		public final ItemStack container;

		public final FluidStack fluid;

		public Input(ItemStack container, FluidStack fluid)
		{
			this.container = container;
			this.fluid = fluid;
		}
	}
}
