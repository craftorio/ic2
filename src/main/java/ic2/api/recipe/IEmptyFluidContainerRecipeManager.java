package ic2.api.recipe;

import ic2.api.util.FluidContainerOutputMode;
import ic2.core.fluid.Ic2FluidStack;

import java.util.Collection;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public interface IEmptyFluidContainerRecipeManager extends IMachineRecipeManager<Void, IEmptyFluidContainerRecipeManager.Output, ItemStack>
{
	MachineRecipeResult<Void, IEmptyFluidContainerRecipeManager.Output, ItemStack> apply(ItemStack var1, Fluid var2, FluidContainerOutputMode var3, boolean var4);

	class Output
	{
		public final Collection<ItemStack> container;
		public final Ic2FluidStack fluid;

		public Output(Collection<ItemStack> container, Ic2FluidStack fluid)
		{
			this.container = container;
			this.fluid = fluid;
		}
	}
}
