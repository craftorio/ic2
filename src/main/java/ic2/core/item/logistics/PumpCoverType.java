package ic2.core.item.logistics;

import ic2.core.block.state.IIdProvider;

public enum PumpCoverType implements IIdProvider
{
	pump_lv(640, 65280),
	pump_mv(2560, 16776960);

	public final int transferRate;
	public final int color;

	PumpCoverType(int transferRate, int color)
	{
		this.transferRate = transferRate;
		this.color = color;
	}

	@Override
	public String getName()
	{
		return this.name();
	}

	@Override
	public int getId()
	{
		return this.ordinal();
	}

	@Override
	public int getColor()
	{
		return this.color;
	}

	@Override
	public String getModelName()
	{
		return "pump";
	}
}
