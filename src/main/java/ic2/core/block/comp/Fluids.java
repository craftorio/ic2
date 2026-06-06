package ic2.core.block.comp;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import ic2.api.recipe.ILiquidAcceptManager;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.invslot.InvSlot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class Fluids extends TileEntityComponent
{
	protected final List<Fluids.InternalFluidTank> managedTanks = new ArrayList<>();
	protected final List<Supplier<? extends Collection<Fluids.InternalFluidTank>>> unmanagedTanks = new ArrayList<>();

	public Fluids(TileEntityBlock parent)
	{
		super(parent);
	}

	public Fluids.InternalFluidTank addTankInsert(String name, int capacity)
	{
		return this.addTankInsert(name, capacity, Predicates.alwaysTrue());
	}

	public Fluids.InternalFluidTank addTankInsert(String name, int capacity, Predicate<Fluid> acceptedFluids)
	{
		return this.addTankInsert(name, capacity, InvSlot.InvSide.ANY, acceptedFluids);
	}

	public Fluids.InternalFluidTank addTankInsert(String name, int capacity, InvSlot.InvSide side)
	{
		return this.addTankInsert(name, capacity, side, Predicates.alwaysTrue());
	}

	public Fluids.InternalFluidTank addTankInsert(String name, int capacity, InvSlot.InvSide side, Predicate<Fluid> acceptedFluids)
	{
		return this.addTank(name, capacity, InvSlot.Access.I, side, acceptedFluids);
	}

	public Fluids.InternalFluidTank addTankExtract(String name, int capacity)
	{
		return this.addTankExtract(name, capacity, InvSlot.InvSide.ANY);
	}

	public Fluids.InternalFluidTank addTankExtract(String name, int capacity, InvSlot.InvSide side)
	{
		return this.addTank(name, capacity, InvSlot.Access.O, side);
	}

	public Fluids.InternalFluidTank addTank(String name, int capacity)
	{
		return this.addTank(name, capacity, InvSlot.Access.IO);
	}

	public Fluids.InternalFluidTank addTank(String name, int capacity, InvSlot.Access access)
	{
		return this.addTank(name, capacity, access, InvSlot.InvSide.ANY);
	}

	public Fluids.InternalFluidTank addTank(String name, int capacity, Predicate<Fluid> acceptedFluids)
	{
		return this.addTank(name, capacity, InvSlot.Access.IO, InvSlot.InvSide.ANY, acceptedFluids);
	}

	public Fluids.InternalFluidTank addTank(String name, int capacity, InvSlot.Access access, InvSlot.InvSide side)
	{
		return this.addTank(name, capacity, access, side, Predicates.alwaysTrue());
	}

	public Fluids.InternalFluidTank addTank(String name, int capacity, InvSlot.Access access, InvSlot.InvSide side, Predicate<Fluid> acceptedFluids)
	{
		return this.addTank(
			name,
			capacity,
			access.isInput() ? side.getAcceptedSides() : Collections.emptySet(),
			access.isOutput() ? side.getAcceptedSides() : Collections.emptySet(),
			acceptedFluids
		);
	}

	public Fluids.InternalFluidTank addTank(
		String name, int capacity, Collection<EnumFacing> inputSides, Collection<EnumFacing> outputSides, Predicate<Fluid> acceptedFluids
	)
	{
		return this.addTank(new Fluids.InternalFluidTank(name, inputSides, outputSides, acceptedFluids, capacity));
	}

	public Fluids.InternalFluidTank addTank(Fluids.InternalFluidTank tank)
	{
		this.managedTanks.add(tank);
		return tank;
	}

	public void addUnmanagedTanks(Fluids.InternalFluidTank tank)
	{
		this.unmanagedTanks.add(Suppliers.ofInstance(Collections.singleton(tank)));
	}

	public void addUnmanagedTanks(Collection<Fluids.InternalFluidTank> tanks)
	{
		this.addUnmanagedTankHook(Suppliers.ofInstance(tanks));
	}

	public void addUnmanagedTankHook(Supplier<? extends Collection<Fluids.InternalFluidTank>> suppl)
	{
		this.unmanagedTanks.add(suppl);
	}

	public void changeConnectivity(Fluids.InternalFluidTank tank, InvSlot.Access access, InvSlot.InvSide side)
	{
		this.changeConnectivity(
			tank, access.isInput() ? side.getAcceptedSides() : Collections.emptySet(), access.isOutput() ? side.getAcceptedSides() : Collections.emptySet()
		);
	}

	public void changeConnectivity(Fluids.InternalFluidTank tank, Collection<EnumFacing> inputSides, Collection<EnumFacing> outputSides)
	{
		assert this.managedTanks.contains(tank);
		tank.inputSides = inputSides;
		tank.outputSides = outputSides;
	}

	public FluidTank getFluidTank(String name)
	{
		for (Fluids.InternalFluidTank tank : this.getAllTanks())
		{
			if (tank.identifier.equals(name))
			{
				return tank;
			}
		}

		throw new IllegalArgumentException("Unable to find tank: " + name);
	}

	@Override
	public void readFromNbt(NBTTagCompound nbt)
	{
		for (Fluids.InternalFluidTank tank : this.managedTanks)
		{
			if (nbt.hasKey(tank.identifier, 10))
			{
				tank.readFromNBT(nbt.getCompoundTag(tank.identifier));
			}
		}
	}

	@Override
	public NBTTagCompound writeToNbt()
	{
		NBTTagCompound nbt = new NBTTagCompound();

		for (Fluids.InternalFluidTank tank : this.managedTanks)
		{
			NBTTagCompound subTag = new NBTTagCompound();
			subTag = tank.writeToNBT(subTag);
			nbt.setTag(tank.identifier, subTag);
		}

		return nbt;
	}

	@Override
	public Collection<? extends Capability<?>> getProvidedCapabilities(EnumFacing side)
	{
		return Collections.singleton(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
	}

	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing side)
	{
		return (T) (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ? new Fluids.FluidHandler(side) : super.getCapability(cap, side));
	}

	public static Predicate<Fluid> fluidPredicate(Fluid... fluids)
	{
		final Collection<Fluid> acceptedFluids;
		if (fluids.length > 10)
		{
			acceptedFluids = new HashSet<>(Arrays.asList(fluids));
		} else
		{
			acceptedFluids = Arrays.asList(fluids);
		}

		return new Predicate<Fluid>()
		{
			public boolean apply(Fluid fluid)
			{
				return acceptedFluids.contains(fluid);
			}
		};
	}

	public static Predicate<Fluid> fluidPredicate(final ILiquidAcceptManager manager)
	{
		return new Predicate<Fluid>()
		{
			public boolean apply(Fluid fluid)
			{
				return manager.acceptsFluid(fluid);
			}
		};
	}

	public Iterable<Fluids.InternalFluidTank> getAllTanks()
	{
		if (this.unmanagedTanks.isEmpty())
		{
			return this.managedTanks;
		}

		List<Fluids.InternalFluidTank> tanks = new ArrayList<>();
		tanks.addAll(this.managedTanks);

		for (Supplier<? extends Collection<Fluids.InternalFluidTank>> suppl : this.unmanagedTanks)
		{
			tanks.addAll((Collection<? extends Fluids.InternalFluidTank>) suppl.get());
		}

		return tanks;
	}

	private class FluidHandler implements IFluidHandler
	{
		private final EnumFacing side;

		FluidHandler(EnumFacing side)
		{
			this.side = side;
		}

		@Override
		public IFluidTankProperties[] getTankProperties()
		{
			List<IFluidTankProperties> props = new ArrayList<>(Fluids.this.managedTanks.size());

			for (Fluids.InternalFluidTank tank : Fluids.this.getAllTanks())
			{
				if (tank.canFill(this.side) || tank.canDrain(this.side))
				{
					props.add(tank.getTankProperties(this.side));
				}
			}

			return props.toArray(new IFluidTankProperties[0]);
		}

		@Override
		public int fill(FluidStack resource, boolean doFill)
		{
			if (resource != null && resource.amount > 0)
			{
				int total = 0;
				FluidStack missing = resource.copy();

				for (Fluids.InternalFluidTank tank : Fluids.this.getAllTanks())
				{
					if (tank.canFill(this.side))
					{
						total += tank.fill(missing, doFill);
						missing.amount = resource.amount - total;
						if (missing.amount <= 0)
						{
							break;
						}
					}
				}

				return total;
			} else
			{
				return 0;
			}
		}

		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain)
		{
			if (resource != null && resource.amount > 0)
			{
				FluidStack ret = new FluidStack(resource.getFluid(), 0);

				for (Fluids.InternalFluidTank tank : Fluids.this.getAllTanks())
				{
					if (tank.canDrain(this.side))
					{
						FluidStack inTank = tank.getFluid();
						if (inTank != null && inTank.getFluid() == resource.getFluid())
						{
							FluidStack add = tank.drain(resource.amount - ret.amount, doDrain);
							if (add != null)
							{
								assert add.getFluid() == resource.getFluid();
								ret.amount = ret.amount + add.amount;
								if (ret.amount >= resource.amount)
								{
									break;
								}
							}
						}
					}
				}

				return ret.amount == 0 ? null : ret;
			} else
			{
				return null;
			}
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain)
		{
			for (Fluids.InternalFluidTank tank : Fluids.this.getAllTanks())
			{
				if (tank.canDrain(this.side))
				{
					FluidStack stack = tank.drain(maxDrain, false);
					if (stack != null)
					{
						stack.amount = maxDrain;
						return this.drain(stack, doDrain);
					}
				}
			}

			return null;
		}
	}

	public static class InternalFluidTank extends FluidTank
	{
		protected final String identifier;
		private final Predicate<Fluid> acceptedFluids;
		private Collection<EnumFacing> inputSides;
		private Collection<EnumFacing> outputSides;

		protected InternalFluidTank(
			String identifier, Collection<EnumFacing> inputSides, Collection<EnumFacing> outputSides, Predicate<Fluid> acceptedFluids, int capacity
		)
		{
			super(capacity);
			this.identifier = identifier;
			this.acceptedFluids = acceptedFluids;
			this.inputSides = inputSides;
			this.outputSides = outputSides;
		}

		@Override
		public boolean canFillFluidType(FluidStack fluid)
		{
			return fluid != null && this.acceptsFluid(fluid.getFluid());
		}

		@Override
		public boolean canDrainFluidType(FluidStack fluid)
		{
			return fluid != null && this.acceptsFluid(fluid.getFluid());
		}

		public boolean acceptsFluid(Fluid fluid)
		{
			return this.acceptedFluids.apply(fluid);
		}

		IFluidTankProperties getTankProperties(final EnumFacing side)
		{
			assert side == null || this.inputSides.contains(side) || this.outputSides.contains(side);
			return new IFluidTankProperties()
			{
				@Override
				public FluidStack getContents()
				{
					return InternalFluidTank.this.getFluid();
				}

				@Override
				public int getCapacity()
				{
					return InternalFluidTank.this.capacity;
				}

				@Override
				public boolean canFillFluidType(FluidStack fluidStack)
				{
					return fluidStack != null && fluidStack.amount > 0
						? InternalFluidTank.this.acceptsFluid(fluidStack.getFluid()) && (side == null || InternalFluidTank.this.canFill(side))
						: false;
				}

				@Override
				public boolean canFill()
				{
					return InternalFluidTank.this.canFill(side);
				}

				@Override
				public boolean canDrainFluidType(FluidStack fluidStack)
				{
					return fluidStack != null && fluidStack.amount > 0
						? InternalFluidTank.this.acceptsFluid(fluidStack.getFluid()) && (side == null || InternalFluidTank.this.canDrain(side))
						: false;
				}

				@Override
				public boolean canDrain()
				{
					return InternalFluidTank.this.canDrain(side);
				}
			};
		}

		public boolean canFill(EnumFacing side)
		{
			return this.inputSides.contains(side);
		}

		public boolean canDrain(EnumFacing side)
		{
			return this.outputSides.contains(side);
		}
	}
}
