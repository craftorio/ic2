package ic2.core.fluid;

import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.Mutable;

public interface Ic2FluidItem
{
	Ic2FluidStack getFluidStack(ItemStack var1);

	int getCapacityMb(ItemStack var1);

	Ic2FluidStack drainMb(ItemStack var1, int var2, boolean var3, Mutable<ItemStack> var4);

	int drainMb(ItemStack var1, Ic2FluidStack var2, boolean var3, Mutable<ItemStack> var4);

	int fillMb(ItemStack var1, Ic2FluidStack var2, boolean var3, Mutable<ItemStack> var4);
}
