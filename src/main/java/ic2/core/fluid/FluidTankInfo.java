package ic2.core.fluid;

public final class FluidTankInfo
{
	public static final int ALL_SIDES = 63;
	private final int drainSideMask;
	private final int fillSideMask;
	private final int capacity;
	private final Ic2FluidStack content;

	public FluidTankInfo(int drainSideMask, int fillSideMask, int capacity, Ic2FluidStack content)
	{
		this.drainSideMask = drainSideMask;
		this.fillSideMask = fillSideMask;
		this.capacity = capacity;
		this.content = content;
	}

	public int getDrainSideMask()
	{
		return this.drainSideMask;
	}

	public int getFillSideMask()
	{
		return this.fillSideMask;
	}

	public int getCapacity()
	{
		return this.capacity;
	}

	public Ic2FluidStack getContent()
	{
		return this.content;
	}
}
