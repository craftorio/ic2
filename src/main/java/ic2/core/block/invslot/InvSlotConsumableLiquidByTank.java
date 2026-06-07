package ic2.core.block.invslot;

import ic2.core.block.IInventorySlotHolder;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;

import java.util.Arrays;

import net.minecraft.world.level.material.Fluid;

public class InvSlotConsumableLiquidByTank extends InvSlotConsumableLiquid
{
	public final Ic2FluidTank tank;

	public InvSlotConsumableLiquidByTank(
		IInventorySlotHolder<?> base1,
		String name1,
		InvSlot.Access access1,
		int count,
		InvSlot.InvSide preferredSide1,
		InvSlotConsumableLiquid.OpType opType,
		Ic2FluidTank tank1
	)
	{
		super(base1, name1, access1, count, preferredSide1, opType);
		this.tank = tank1;
	}

	@Override
	protected boolean acceptsLiquid(Fluid fluid)
	{
		return this.tank.isEmpty() || this.tank.hasExactFluid(fluid);
	}

	@Override
	protected Iterable<Fluid> getPossibleFluids()
	{
		Ic2FluidStack fs = this.tank.getFluidStack();
		return fs != null ? Arrays.asList(fs.getFluid()) : null;
	}
}
