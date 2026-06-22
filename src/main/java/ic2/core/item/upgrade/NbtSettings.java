package ic2.core.item.upgrade;

import java.util.Locale;

public enum NbtSettings
{
	IGNORED,
	FUZZY,
	EXACT;

	public static final NbtSettings[] VALUES = values();
	final String name = "ic2.upgrade.advancedGUI." + this.name().toLowerCase(Locale.ENGLISH);

	public static NbtSettings getFromNBT(byte type)
	{
		return VALUES[type];
	}

	public boolean enabled()
	{
		return this != IGNORED;
	}

	public byte getForNBT()
	{
		return (byte) this.ordinal();
	}
}
