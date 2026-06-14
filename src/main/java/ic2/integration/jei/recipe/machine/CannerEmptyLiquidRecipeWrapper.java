package ic2.integration.jei.recipe.machine;

import ic2.core.fluid.Ic2FluidStack;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class CannerEmptyLiquidRecipeWrapper
{
	private final ItemStack filledContainer;
	private final List<ItemStack> drainedContainers;
	private final Ic2FluidStack fluid;

	public CannerEmptyLiquidRecipeWrapper(ItemStack filledContainer, List<ItemStack> drainedContainers, Ic2FluidStack fluid)
	{
		this.filledContainer = filledContainer;
		this.drainedContainers = drainedContainers;
		this.fluid = fluid;
	}

	public ItemStack getFilledContainer()
	{
		return this.filledContainer;
	}

	public List<ItemStack> getDrainedContainers()
	{
		return this.drainedContainers;
	}

	public FluidStack getFluidOutput()
	{
		return new FluidStack(this.fluid.getFluid(), this.fluid.getAmountMb());
	}
}
