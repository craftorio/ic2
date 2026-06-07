package ic2.core.block.transport.cover;

import ic2.core.fluid.Ic2FluidStack;

import java.util.Set;

import net.minecraft.world.item.ItemStack;

public interface ICoverItem
{
	boolean isSuitableFor(ItemStack var1, Set<CoverProperty> var2);

	boolean onTick(ItemStack var1, ICoverHolder var2);

	boolean allowsInput(ItemStack var1);

	boolean allowsInput(Ic2FluidStack var1);

	boolean allowsOutput(ItemStack var1);

	boolean allowsOutput(Ic2FluidStack var1);
}
