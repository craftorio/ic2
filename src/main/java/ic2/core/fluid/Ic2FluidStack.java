package ic2.core.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public interface Ic2FluidStack
{
	Ic2FluidStack EMPTY = create(Fluids.EMPTY, 0);
	int BUCKET_MB = 1000;

	static Ic2FluidStack create(Fluid fluid, int amount)
	{
		if (fluid == null)
		{
			throw new NullPointerException();
		} else
		{
			return FluidHandler.ENV_HANDLER.createFluidStackMb(fluid, amount, null);
		}
	}

	static Ic2FluidStack get(ItemStack stack)
	{
		Item item = stack.getItem();
		return item instanceof Ic2FluidItem ? ((Ic2FluidItem) item).getFluidStack(stack) : FluidHandler.ENV_HANDLER.getFluidStack(stack);
	}

	static Ic2FluidStack[] getAll(ItemStack stack)
	{
		Item item = stack.getItem();
		return item instanceof Ic2FluidItem ? new Ic2FluidStack[] { ((Ic2FluidItem) item).getFluidStack(stack) } : FluidHandler.ENV_HANDLER.getFluidStacks(stack);
	}

	static Ic2FluidStack read(CompoundTag nbt)
	{
		return FluidHandler.ENV_HANDLER.readFluidStack(nbt);
	}

	Ic2FluidStack copy();

	default Ic2FluidStack copyWithAmountMb(int amountMb)
	{
		Ic2FluidStack ret = this.copy();
		ret.setAmountMb(amountMb);
		return ret;
	}

	Fluid getFluid();

	boolean hasExactFluid(Fluid var1);

	boolean hasExactFluid(Ic2FluidStack var1);

	default boolean isEmpty()
	{
		return this.getAmountMb() == 0;
	}

	int getAmountMb();

	void setAmountMb(int var1);

	default void increaseMb(int amount)
	{
		this.setAmountMb(this.getAmountMb() + amount);
	}

	default void decreaseMb(int amount)
	{
		this.setAmountMb(this.getAmountMb() - amount);
	}

	void toNbt(CompoundTag var1);
}
