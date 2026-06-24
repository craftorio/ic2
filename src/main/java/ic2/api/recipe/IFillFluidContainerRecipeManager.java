package ic2.api.recipe;

import ic2.api.util.FluidContainerOutputMode;
import ic2.core.fluid.Ic2FluidStack;

import java.util.Collection;

import net.minecraft.world.item.ItemStack;

public interface IFillFluidContainerRecipeManager extends IMachineRecipeManager<Void, Collection<ItemStack>, IFillFluidContainerRecipeManager.Input>
{
	MachineRecipeResult<Void, Collection<ItemStack>, IFillFluidContainerRecipeManager.Input> apply(
		IFillFluidContainerRecipeManager.Input var1, FluidContainerOutputMode var2, boolean var3
	);

	record Input(ItemStack container, Ic2FluidStack fluid)
		{
		}
}
