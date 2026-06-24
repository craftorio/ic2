package ic2.api.recipe;

import ic2.api.util.FluidContainerOutputMode;
import ic2.core.fluid.Ic2FluidStack;

import java.util.Collection;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public interface IEmptyFluidContainerRecipeManager extends IMachineRecipeManager<Void, IEmptyFluidContainerRecipeManager.Output, ItemStack>
{
	MachineRecipeResult<Void, IEmptyFluidContainerRecipeManager.Output, ItemStack> apply(ItemStack var1, Fluid var2, FluidContainerOutputMode var3, boolean var4);

	record Output(Collection<ItemStack> container, Ic2FluidStack fluid)
		{
		}
}
