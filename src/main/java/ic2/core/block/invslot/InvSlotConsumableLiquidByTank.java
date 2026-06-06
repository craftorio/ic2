package ic2.core.block.invslot;

import ic2.core.block.IInventorySlotHolder;

import java.util.Arrays;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public class InvSlotConsumableLiquidByTank extends InvSlotConsumableLiquid
{
	public final IFluidTank tank;

	public InvSlotConsumableLiquidByTank(
		IInventorySlotHolder<?> base1,
		String name1,
		InvSlot.Access access1,
		int count,
		InvSlot.InvSide preferredSide1,
		InvSlotConsumableLiquid.OpType opType,
		IFluidTank tank1
	)
	{
		super(base1, name1, access1, count, preferredSide1, opType);
		this.tank = tank1;
	}

	@Override
	protected boolean acceptsLiquid(Fluid fluid)
	{
		FluidStack fs = this.tank.getFluid();
		return fs == null || fs.getFluid() == fluid;
	}

	@Override
	protected Iterable<Fluid> getPossibleFluids()
	{
		FluidStack fs = this.tank.getFluid();
		return fs == null ? null : Arrays.asList(fs.getFluid());
	}
}
