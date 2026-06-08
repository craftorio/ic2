package ic2.core.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

public class Ic2FluidTank
{
	private int capacity;
	private Ic2FluidStack fluidStack;

	public Ic2FluidTank(int capacity)
	{
		this.capacity = capacity;
	}

	public final int getCapacity()
	{
		return this.capacity;
	}

	public final void setCapacity(int capacity)
	{
		this.capacity = capacity;
	}

	public final Ic2FluidStack getFluidStack()
	{
		return this.fluidStack;
	}

	public final void setFluidStack(Ic2FluidStack fs)
	{
		this.fluidStack = fs;
	}

	public final boolean hasExactFluid(Fluid fluid)
	{
		return this.fluidStack != null && this.fluidStack.hasExactFluid(fluid);
	}

	public final boolean isEmpty()
	{
		return this.fluidStack == null || this.fluidStack.isEmpty();
	}

	public final int getFluidAmount()
	{
		return this.fluidStack != null ? this.fluidStack.getAmountMb() : 0;
	}

	public final void fromNbt(CompoundTag nbt)
	{
		if (nbt.contains("Empty"))
		{
			this.fluidStack = null;
		} else
		{
			this.fluidStack = Ic2FluidStack.read(nbt);
		}
	}

	public final CompoundTag toNbt(CompoundTag nbt)
	{
		if (this.fluidStack != null && !this.fluidStack.isEmpty())
		{
			this.fluidStack.toNbt(nbt);
		} else
		{
			nbt.putString("Empty", "");
		}

		return nbt;
	}

	public final Ic2FluidStack drainMb(int amount, boolean simulate)
	{
		return this.drainMb(amount, simulate, true);
	}

	public final Ic2FluidStack drainMbUnchecked(int amount, boolean simulate)
	{
		return this.drainMb(amount, simulate, false);
	}

	private Ic2FluidStack drainMb(int amount, boolean simulate, boolean external)
	{
		if (amount > 0 && this.fluidStack != null && !this.fluidStack.isEmpty() && (!external || this.canDrain()))
		{
			Ic2FluidStack ret;
			if (amount >= this.fluidStack.getAmountMb())
			{
				if (simulate)
				{
					ret = this.fluidStack.copy();
				} else
				{
					ret = this.fluidStack;
					this.fluidStack = null;
				}
			} else
			{
				ret = this.fluidStack.copyWithAmountMb(amount);
				if (!simulate)
				{
					this.fluidStack.decreaseMb(amount);
				}
			}

			return ret;
		} else
		{
			return Ic2FluidStack.EMPTY;
		}
	}

	public final int drainMb(Ic2FluidStack toDrain, boolean simulate)
	{
		return this.drainMb(toDrain, simulate, true);
	}

	public final int drainMbUnchecked(Ic2FluidStack toDrain, boolean simulate)
	{
		return this.drainMb(toDrain, simulate, false);
	}

	private int drainMb(Ic2FluidStack toDrain, boolean simulate, boolean external)
	{
		if (!toDrain.isEmpty()
			&& this.fluidStack != null
			&& !this.fluidStack.isEmpty()
			&& this.fluidStack.hasExactFluid(toDrain)
			&& (!external || this.canDrain()))
		{
			int amount = toDrain.getAmountMb();
			int ret;
			if (amount >= this.fluidStack.getAmountMb())
			{
				ret = this.fluidStack.getAmountMb();
				if (!simulate)
				{
					this.fluidStack = null;
				}
			} else
			{
				ret = amount;
				if (!simulate)
				{
					this.fluidStack.decreaseMb(amount);
				}
			}

			return ret;
		} else
		{
			return 0;
		}
	}

	public final int fillMb(Ic2FluidStack toFill, boolean simulate)
	{
		return this.fillMb(toFill, simulate, true);
	}

	public final int fillMbUnchecked(Ic2FluidStack toFill, boolean simulate)
	{
		return this.fillMb(toFill, simulate, false);
	}

	private int fillMb(Ic2FluidStack toFill, boolean simulate, boolean external)
	{
		if (!toFill.isEmpty()
			&& this.capacity > 0
			&& (this.fluidStack == null || this.fluidStack.getAmountMb() < this.capacity && (this.fluidStack.isEmpty() || this.fluidStack.hasExactFluid(toFill)))
			&& (!external || this.canFill(toFill.getFluid())))
		{
			int ret = Math.min(toFill.getAmountMb(), this.capacity - this.getFluidAmount());
			if (!simulate)
			{
				if (this.fluidStack != null && !this.fluidStack.isEmpty())
				{
					this.fluidStack.increaseMb(ret);
				} else
				{
					this.fluidStack = toFill.copyWithAmountMb(ret);
				}
			}

			return ret;
		} else
		{
			return 0;
		}
	}

	protected boolean canDrain()
	{
		return true;
	}

	protected boolean canFill(Fluid fluid)
	{
		return true;
	}
}
