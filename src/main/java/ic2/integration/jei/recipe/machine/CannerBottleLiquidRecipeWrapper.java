package ic2.integration.jei.recipe.machine;

import ic2.core.fluid.Ic2FluidStack;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class CannerBottleLiquidRecipeWrapper
{
	private final List<ItemStack> emptyContainers;
	private final Ic2FluidStack fluid;
	private final ItemStack filledContainer;

	public CannerBottleLiquidRecipeWrapper(List<ItemStack> emptyContainers, Ic2FluidStack fluid, ItemStack filledContainer)
	{
		this.emptyContainers = emptyContainers;
		this.fluid = fluid;
		this.filledContainer = filledContainer;
	}

	public List<ItemStack> getEmptyContainers()
	{
		return this.emptyContainers;
	}

	public FluidStack getFluidInput()
	{
		return new FluidStack(this.fluid.getFluid(), this.fluid.getAmountMb());
	}

	public ItemStack getFilledContainer()
	{
		return this.filledContainer;
	}
}
