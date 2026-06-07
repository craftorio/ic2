package ic2.forge;

import ic2.core.fluid.Ic2FluidItem;
import ic2.core.fluid.Ic2FluidStack;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.apache.commons.lang3.mutable.Mutable;

final class ItemFluidCapImpl implements ICapabilityProvider, IFluidHandlerItem, NonNullSupplier<IFluidHandlerItem>, Mutable<ItemStack>
{
	private ItemStack stack;

	public ItemFluidCapImpl(ItemStack stack)
	{
		this.stack = stack;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing)
	{
		return capability == ForgeCapabilities.FLUID_HANDLER_ITEM ? LazyOptional.of(this) : LazyOptional.empty();
	}

	@Override
	public int getTanks()
	{
		return 1;
	}

	@Override
	public int getTankCapacity(int tank)
	{
		if (tank != 0)
		{
			return 0;
		}

		Ic2FluidItem parent = (Ic2FluidItem) this.stack.getItem();
		return parent.getCapacityMb(this.stack);
	}

	@Override
	public FluidStack getFluidInTank(int tank)
	{
		if (tank != 0)
		{
			return FluidStack.EMPTY;
		}

		Ic2FluidItem parent = (Ic2FluidItem) this.stack.getItem();
		Ic2FluidStack fs = parent.drainMb(this.stack, Integer.MAX_VALUE, true, null);
		return EnvFluidHandlerForge.getForgeFs(fs);
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack fs)
	{
		return tank == 0;
	}

	@Override
	public FluidStack drain(int amount, IFluidHandler.FluidAction action)
	{
		if (amount > 0 && this.stack.m_41613_() == 1)
		{
			Ic2FluidItem parent = (Ic2FluidItem) this.stack.getItem();
			return EnvFluidHandlerForge.getForgeFs(parent.drainMb(this.stack, amount, action.simulate(), this));
		} else
		{
			return FluidStack.EMPTY;
		}
	}

	@Override
	public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action)
	{
		if (resource != null && !resource.isEmpty() && this.stack.m_41613_() == 1)
		{
			Ic2FluidItem parent = (Ic2FluidItem) this.stack.getItem();
			int amount = parent.drainMb(this.stack, new Ic2FluidStackImpl(resource), action.simulate(), this);
			if (amount <= 0)
			{
				return FluidStack.EMPTY;
			}

			resource = resource.copy();
			resource.setAmount(amount);
			return resource;
		} else
		{
			return FluidStack.EMPTY;
		}
	}

	@Override
	public int fill(FluidStack resource, IFluidHandler.FluidAction action)
	{
		if (resource != null && !resource.isEmpty() && this.stack.m_41613_() == 1)
		{
			Ic2FluidItem parent = (Ic2FluidItem) this.stack.getItem();
			return parent.fillMb(this.stack, new Ic2FluidStackImpl(resource), action.simulate(), this);
		} else
		{
			return 0;
		}
	}

	@Override
	public ItemStack getContainer()
	{
		return this.stack;
	}

	public IFluidHandlerItem get()
	{
		return this;
	}

	public ItemStack getValue()
	{
		return this.stack;
	}

	public void setValue(ItemStack value)
	{
		this.stack = value;
	}
}
