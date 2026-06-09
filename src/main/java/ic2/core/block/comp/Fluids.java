package ic2.core.block.comp;

import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import ic2.api.recipe.ILiquidAcceptManager;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.fluid.FluidTankInfo;
import ic2.core.fluid.Ic2FluidBlock;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public class Fluids extends TileEntityComponent implements Ic2FluidBlock
{
	protected final List<Fluids.InternalFluidTank> managedTanks = new ArrayList<>();
	protected final List<Supplier<? extends Collection<Fluids.InternalFluidTank>>> unmanagedTanks = new ArrayList<>();

	public Fluids(Ic2TileEntity parent)
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
		String name, int capacity, Collection<Direction> inputSides, Collection<Direction> outputSides, Predicate<Fluid> acceptedFluids
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

	public void changeConnectivity(Fluids.InternalFluidTank tank, Collection<Direction> inputSides, Collection<Direction> outputSides)
	{
		assert this.managedTanks.contains(tank);
		tank.inputSides = inputSides;
		tank.outputSides = outputSides;
	}

	public Ic2FluidTank getFluidTank(String name)
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
	public void readFromNbt(CompoundTag nbt)
	{
		for (Fluids.InternalFluidTank tank : this.managedTanks)
		{
			if (nbt.contains(tank.identifier, 10))
			{
				tank.fromNbt(nbt.getCompound(tank.identifier));
			}
		}
	}

	@Override
	public CompoundTag writeToNbt()
	{
		CompoundTag nbt = new CompoundTag();

		for (Fluids.InternalFluidTank tank : this.managedTanks)
		{
			CompoundTag subTag = new CompoundTag();
			tank.toNbt(subTag);
			nbt.put(tank.identifier, subTag);
		}

		return nbt;
	}

	public FluidTankInfo[] getTankInfos()
	{
		if (this.managedTanks.isEmpty())
		{
			return null;
		}

		FluidTankInfo[] ret = new FluidTankInfo[this.managedTanks.size()];
		int writeIdx = 0;

		for (Fluids.InternalFluidTank tank : this.managedTanks)
		{
			int drainSideMask = 0;
			int fillSideMask = 0;

			for (Direction side : Util.ALL_DIRS)
			{
				int mask = 1 << side.ordinal();
				if (tank.canDrain(side))
				{
					drainSideMask |= mask;
				}

				if (tank.canFill(side))
				{
					fillSideMask |= mask;
				}
			}

			if ((drainSideMask | fillSideMask) != 0)
			{
				ret[writeIdx++] = new FluidTankInfo(drainSideMask, fillSideMask, tank.getCapacity(), tank.getFluidStack());
			}
		}

		if (writeIdx == 0)
		{
			return null;
		}

		if (writeIdx != ret.length)
		{
			ret = Arrays.copyOf(ret, writeIdx);
		}

		return ret;
	}

	public Ic2FluidStack drainMb(Direction side, int amount, boolean simulate)
	{
		if (amount <= 0)
		{
			return Ic2FluidStack.EMPTY;
		}

		for (Fluids.InternalFluidTank tank : this.getAllTanks())
		{
			if (tank.canDrain(side))
			{
				Ic2FluidStack fs = tank.drainMb(amount, true);
				if (fs != null && !fs.isEmpty())
				{
					fs.setAmountMb(amount);
					amount = this.drainMb(side, fs, simulate);
					fs.setAmountMb(amount);
					return fs;
				}
			}
		}

		return Ic2FluidStack.EMPTY;
	}

	public int drainMb(Direction side, Ic2FluidStack drainFs, boolean simulate)
	{
		if (drainFs != null && !drainFs.isEmpty())
		{
			int initialAmount = drainFs.getAmountMb();

			for (Fluids.InternalFluidTank tank : this.getAllTanks())
			{
				if (tank.canDrain(side))
				{
					int drained = tank.drainMb(drainFs, simulate);
					if (drained > 0)
					{
						assert drained <= drainFs.getAmountMb();
						drainFs.decreaseMb(drained);
						if (drainFs.isEmpty())
						{
							break;
						}
					}
				}
			}

			int ret = initialAmount - drainFs.getAmountMb();
			drainFs.setAmountMb(initialAmount);
			return ret;
		} else
		{
			return 0;
		}
	}

	public int fillMb(Direction side, Ic2FluidStack fillFs, boolean simulate)
	{
		if (fillFs != null && !fillFs.isEmpty())
		{
			int initialAmount = fillFs.getAmountMb();

			for (Fluids.InternalFluidTank tank : this.getAllTanks())
			{
				if (tank.canFill(fillFs.getFluid(), side))
				{
					int filled = tank.fillMb(fillFs, simulate);
					if (filled > 0)
					{
						assert filled <= fillFs.getAmountMb();
						fillFs.decreaseMb(filled);
						if (fillFs.isEmpty())
						{
							break;
						}
					}
				}
			}

			int ret = initialAmount - fillFs.getAmountMb();
			fillFs.setAmountMb(initialAmount);
			return ret;
		} else
		{
			return 0;
		}
	}

	public static Predicate<Fluid> fluidPredicate(Fluid... fluids)
	{
		Collection<Fluid> acceptedFluids;
		if (fluids.length > 10)
		{
			acceptedFluids = new HashSet<>(Arrays.asList(fluids));
		} else
		{
			acceptedFluids = Arrays.asList(fluids);
		}

		return acceptedFluids::contains;
	}

	public static Predicate<Fluid> fluidPredicate(TagKey<Fluid> tag)
	{
		return f -> f.is(tag);
	}

	public static Predicate<Fluid> fluidPredicate(ILiquidAcceptManager manager)
	{
		return manager::acceptsFluid;
	}

	public Iterable<Fluids.InternalFluidTank> getAllTanks()
	{
		if (this.unmanagedTanks.isEmpty())
		{
			return this.managedTanks;
		}

		List<InternalFluidTank> tanks = new ArrayList<>(this.managedTanks);

		for (Supplier<? extends Collection<Fluids.InternalFluidTank>> suppl : this.unmanagedTanks)
		{
			tanks.addAll(suppl.get());
		}

		return tanks;
	}

	// ---- Ic2FluidBlock ----

	@Override
	public boolean isFluidBlock(BlockState state, Level world, BlockPos pos, BlockEntity be)
	{
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfos(BlockState state, Level world, BlockPos pos, BlockEntity be)
	{
		return this.getTankInfos();
	}

	@Override
	public Ic2FluidStack drainMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, int amount, boolean simulate)
	{
		return this.drainMb(side, amount, simulate);
	}

	@Override
	public int drainMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, Ic2FluidStack drainFs, boolean simulate)
	{
		return this.drainMb(side, drainFs, simulate);
	}

	@Override
	public int fillMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, Ic2FluidStack fillFs, boolean simulate)
	{
		return this.fillMb(side, fillFs, simulate);
	}

	public static class InternalFluidTank extends Ic2FluidTank
	{
		protected final String identifier;
		private final Predicate<Fluid> acceptedFluids;
		private Collection<Direction> inputSides;
		private Collection<Direction> outputSides;

		protected InternalFluidTank(
			String identifier, Collection<Direction> inputSides, Collection<Direction> outputSides, Predicate<Fluid> acceptedFluids, int capacity
		)
		{
			super(capacity);
			this.identifier = identifier;
			this.acceptedFluids = acceptedFluids;
			this.inputSides = inputSides;
			this.outputSides = outputSides;
		}

		@Override
		public boolean canFill(Fluid fluid)
		{
			return fluid != null && this.acceptedFluids.test(fluid);
		}

		public boolean canFill(Direction side)
		{
			return this.inputSides.contains(side);
		}

		public boolean canFill(Fluid fluid, Direction side)
		{
			return this.canFill(fluid) && this.canFill(side);
		}

		public boolean canDrain(Direction side)
		{
			return this.outputSides.contains(side);
		}
	}
}
