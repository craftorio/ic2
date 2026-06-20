package ic2.forge;

import ic2.core.fluid.Ic2FluidStack;

import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.RegistryAccess;
import org.jetbrains.annotations.NotNull;

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
		return this.parent.isComponentsPatchEmpty() ? Ic2FluidStack.create(this.parent.getFluid(), this.parent.getAmount()) : new Ic2FluidStackImpl(this.parent.copy());
	}

	@Override
	public Fluid getFluid()
	{
		return this.parent.getFluid();
	}

	@Override
	public boolean hasExactFluid(Fluid fluid)
	{
		return this.parent.isComponentsPatchEmpty() && fluid == this.parent.getFluid();
	}

	@Override
	public boolean hasExactFluid(Ic2FluidStack fs)
	{
		return fs instanceof Ic2FluidStackImpl ? FluidStack.isSameFluidSameComponents(this.parent, ((Ic2FluidStackImpl) fs).parent) : this.hasExactFluid(fs.getFluid());
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
		ResourceLocation key = BuiltInRegistries.FLUID.getKey(this.parent.getFluid());
		nbt.putString("FluidName", key.toString());
		nbt.putInt("Amount", this.parent.getAmount());
		if (!this.parent.isComponentsPatchEmpty())
		{
			Tag saved = this.parent.saveOptional(RegistryAccess.EMPTY);
			if (saved instanceof CompoundTag tag)
			{
				nbt.put("Tag", tag);
			}
		}
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
				return o instanceof Ic2FluidStackImpl ? Objects.equals(this.parent.getComponentsPatch(), ((Ic2FluidStackImpl) o).parent.getComponentsPatch()) : this.parent.isComponentsPatchEmpty();
			}
		}
	}

	@Override
	public int hashCode()
	{
		return this.parent.getFluid().hashCode() ^ this.parent.getAmount();
	}

	@Override
	public @NotNull String toString()
	{
		Fluid fluid = this.parent.getFluid();
		return String.format("%dx%s@%s", this.parent.getAmount(), BuiltInRegistries.FLUID.getKey(fluid), !this.parent.isComponentsPatchEmpty() ? this.parent.getComponentsPatch().toString() : "(-)");
	}
}
