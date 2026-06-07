package ic2.core.util;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.item.DyeColor;

public enum Ic2Color
{
	BLACK(DyeColor.BLACK, 1908001),
	BLUE(DyeColor.BLUE, 3949738),
	BROWN(DyeColor.BROWN, 8606770),
	CYAN(DyeColor.CYAN, 1481884),
	GRAY(DyeColor.GRAY, 4673362),
	GREEN(DyeColor.GREEN, 6192150),
	LIGHT_BLUE(DyeColor.LIGHT_BLUE, 3847130),
	LIGHT_GRAY(DyeColor.LIGHT_GRAY, 10329495),
	LIME(DyeColor.LIME, 8439583),
	MAGENTA(DyeColor.MAGENTA, 13061821),
	ORANGE(DyeColor.ORANGE, 16351261),
	PINK(DyeColor.PINK, 15961002),
	PURPLE(DyeColor.PURPLE, 8991416),
	RED(DyeColor.RED, 11546150),
	WHITE(DyeColor.WHITE, 16383998),
	YELLOW(DyeColor.YELLOW, 16701501);

	public static final Ic2Color[] values = values();
	private static final Map<DyeColor, Ic2Color> dyeColorMap = new EnumMap<>(DyeColor.class);
	private static final Map<Integer, Ic2Color> colorMap = new HashMap<>();
	public final DyeColor dyeColor;
	public final int color;

	Ic2Color(DyeColor mcColor, int color)
	{
		this.dyeColor = mcColor;
		this.color = color;
	}

	public int getId()
	{
		return this.ordinal();
	}

	public int getColor()
	{
		return this.color;
	}

	public static Ic2Color get(DyeColor mcColor)
	{
		return dyeColorMap.get(mcColor);
	}

	public static Ic2Color byColor(int color)
	{
		return colorMap.get(color);
	}

	static
	{
		for (Ic2Color color : values)
		{
			dyeColorMap.put(color.dyeColor, color);
			colorMap.put(color.color, color);
		}
	}
}
