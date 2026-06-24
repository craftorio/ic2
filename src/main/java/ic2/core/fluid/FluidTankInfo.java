package ic2.core.fluid;

public record FluidTankInfo(int drainSideMask, int fillSideMask, int capacity, Ic2FluidStack content)
{
	public static final int ALL_SIDES = 63;


}
