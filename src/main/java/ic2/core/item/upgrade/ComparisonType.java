package ic2.core.item.upgrade;

import java.util.Locale;

public enum ComparisonType
{
	IGNORED,
	DIRECT,
	COMPARISON,
	RANGE;

	public static final ComparisonType[] VALUES = values();
	final String name = "ic2.upgrade.advancedGUI." + this.name().toLowerCase(Locale.ENGLISH);

	public static ComparisonType getFromNBT(byte type)
	{
		return VALUES[type];
	}

	public boolean enabled()
	{
		return this != IGNORED;
	}

	public boolean ignoreFilters()
	{
		return this == IGNORED || this == DIRECT;
	}

	public byte getForNBT()
	{
		return (byte) this.ordinal();
	}
}
