package ic2.core.block.wiring;

import ic2.core.block.state.IIdProvider;
import ic2.core.util.Ic2Color;

import java.util.HashMap;
import java.util.Map;

public enum CableType implements IIdProvider
{
	copper(1, 1, 0.25F, 0.2, 128),
	glass(0, 0, 0.25F, 0.025, 8192),
	gold(2, 1, 0.1875F, 0.4, 512),
	iron(3, 1, 0.375F, 0.8, 2048),
	tin(1, 1, 0.25F, 0.2, 32),
	detector(0, Integer.MAX_VALUE, 0.5F, 0.5, 8192),
	splitter(0, Integer.MAX_VALUE, 0.5F, 0.5, 8192);

	public final int maxInsulation;
	public final int minColoredInsulation;
	public final float thickness;
	public final double loss;
	public final int capacity;
	public static final CableType[] values = values();
	private static final Map<String, CableType> nameMap = new HashMap<>();

	CableType(int maxInsulation, int minColoredInsulation, float thickness, double loss, int capacity)
	{
		this.maxInsulation = maxInsulation;
		this.minColoredInsulation = minColoredInsulation;
		this.thickness = thickness;
		this.loss = loss;
		this.capacity = capacity;
	}

	public String getName(int insulation, Ic2Color color)
	{
		StringBuilder ret = new StringBuilder(this.getName());
		ret.append("_cable");
		if (this.maxInsulation != 0)
		{
			ret.append('_');
			ret.append(insulation);
		}

		if (insulation >= this.minColoredInsulation && color != null)
		{
			ret.append('_');
			ret.append(color.name());
		}

		return ret.toString();
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

	public static CableType get(String name)
	{
		return nameMap.get(name);
	}

	static
	{
		for (CableType type : values)
		{
			nameMap.put(type.getName(), type);
		}
	}
}
