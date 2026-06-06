package ic2.core.util;

import ic2.core.block.state.IIdProvider;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.item.EnumDyeColor;

public enum Ic2Color implements IIdProvider
{
	black(EnumDyeColor.BLACK, "dyeBlack"),
	blue(EnumDyeColor.BLUE, "dyeBlue"),
	brown(EnumDyeColor.BROWN, "dyeBrown"),
	cyan(EnumDyeColor.CYAN, "dyeCyan"),
	gray(EnumDyeColor.GRAY, "dyeGray"),
	green(EnumDyeColor.GREEN, "dyeGreen"),
	light_blue(EnumDyeColor.LIGHT_BLUE, "dyeLightBlue"),
	light_gray(EnumDyeColor.SILVER, "dyeLightGray"),
	lime(EnumDyeColor.LIME, "dyeLime"),
	magenta(EnumDyeColor.MAGENTA, "dyeMagenta"),
	orange(EnumDyeColor.ORANGE, "dyeOrange"),
	pink(EnumDyeColor.PINK, "dyePink"),
	purple(EnumDyeColor.PURPLE, "dyePurple"),
	red(EnumDyeColor.RED, "dyeRed"),
	white(EnumDyeColor.WHITE, "dyeWhite"),
	yellow(EnumDyeColor.YELLOW, "dyeYellow");

	public static final Ic2Color[] values = values();
	private static final Map<EnumDyeColor, Ic2Color> mcColorMap = new EnumMap<>(EnumDyeColor.class);
	public final EnumDyeColor mcColor;
	public final String oreDictDyeName;

	Ic2Color(EnumDyeColor mcColor, String oreDictDyeName)
	{
		this.mcColor = mcColor;
		this.oreDictDyeName = oreDictDyeName;
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

	public static Ic2Color get(EnumDyeColor mcColor)
	{
		return mcColorMap.get(mcColor);
	}

	static
	{
		for (Ic2Color color : values)
		{
			mcColorMap.put(color.mcColor, color);
		}
	}
}
