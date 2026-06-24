package ic2.forge;

import ic2.core.fluid.Ic2FluidStack;

import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

record Ic2FluidStackImpl(FluidStack parent) implements Ic2FluidStack
{
	Ic2FluidStackImpl
	{
		if (parent == null)
		{
			throw new NullPointerException();
		}

	}

	@Override
	public Ic2FluidStack copy()
	{
		return this.parent.getTag() == null ? Ic2FluidStack.create(this.parent.getFluid(), this.parent.getAmount()) : new Ic2FluidStackImpl(this.parent.copy());
	}

	@Override
	public Fluid getFluid()
	{
		return this.parent.getFluid();
	}

	@Override
	public boolean hasExactFluid(Fluid fluid)
	{
		return this.parent.getTag() == null && fluid == this.parent.getFluid();
	}

	@Override
	public boolean hasExactFluid(Ic2FluidStack fs)
	{
		return fs instanceof Ic2FluidStackImpl ? this.parent.isFluidEqual(((Ic2FluidStackImpl) fs).parent) : this.hasExactFluid(fs.getFluid());
	}

	@Override
	public int getAmountMb()
	{
		return this.parent.getAmount();
	}

	@Override
	public void setAmountMb(int amount)
	{
		if (amount < 0)
		{
			throw new IllegalArgumentException();
		}

		this.parent.setAmount(amount);
	}

	@Override
	public void toNbt(CompoundTag nbt)
	{
		this.parent.writeToNBT(nbt);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Ic2FluidStack)
		{
			return false;
		} else
		{
			Ic2FluidStack o = (Ic2FluidStack) obj;
			if (this.parent.getAmount() != o.getAmountMb() || this.parent.getFluid() != o.getFluid())
			{
				return false;
			} else
			{
				return o instanceof Ic2FluidStackImpl ? Objects.equals(this.parent.getTag(), ((Ic2FluidStackImpl) o).parent.getTag()) : this.parent.getTag() == null;
			}
		}
	}

	@Override
	public int hashCode()
	{
		return this.parent.getFluid().hashCode() ^ this.parent.getAmount();
	}

	@Override
	public String toString()
	{
		Fluid fluid = this.parent.getFluid();
		return String.format(
			"%dx%s@%s",
			this.parent.getAmount(),
			fluid != null ? ForgeRegistries.FLUIDS.getKey(fluid) : "(null)",
			this.parent.getTag() != null ? this.parent.getTag().toString() : "(-)"
		);
	}
}
