package ic2.core.block.wiring;

import ic2.core.block.misc.WallBlock;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;

public enum CableFoam implements StringRepresentable
{
	SOFT("soft", null),
	HARD_WHITE("hard_white", DyeColor.WHITE),
	HARD_ORANGE("hard_orange", DyeColor.ORANGE),
	HARD_MAGENTA("hard_magenta", DyeColor.MAGENTA),
	HARD_LIGHT_BLUE("hard_light_blue", DyeColor.LIGHT_BLUE),
	HARD_YELLOW("hard_yellow", DyeColor.YELLOW),
	HARD_LIME("hard_lime", DyeColor.LIME),
	HARD_PINK("hard_pink", DyeColor.PINK),
	HARD_GRAY("hard_gray", DyeColor.GRAY),
	HARD_LIGHT_GRAY("hard_light_gray", DyeColor.LIGHT_GRAY),
	HARD_CYAN("hard_cyan", DyeColor.CYAN),
	HARD_PURPLE("hard_purple", DyeColor.PURPLE),
	HARD_BLUE("hard_blue", DyeColor.BLUE),
	HARD_BROWN("hard_brown", DyeColor.BROWN),
	HARD_GREEN("hard_green", DyeColor.GREEN),
	HARD_RED("hard_red", DyeColor.RED),
	HARD_BLACK("hard_black", DyeColor.BLACK);

	public static final CableFoam[] VALUES = values();
	private static final Map<String, CableFoam> NAME_MAP = createNameMap();
	private static final Map<DyeColor, CableFoam> COLOR_MAP = createColorMap();
	public static final CableFoam DEFAULT_HARD = getHard(WallBlock.DEFAULT_COLOR);
	private final String name;
	private final DyeColor color;

	public static CableFoam get(String name)
	{
		return NAME_MAP.get(name);
	}

	public static CableFoam getHard(DyeColor color)
	{
		return COLOR_MAP.get(color);
	}

	CableFoam(String name, DyeColor color)
	{
		this.name = name;
		this.color = color;
	}

	public boolean isPresent()
	{
		return true;
	}

	public boolean isSoft()
	{
		return this == SOFT;
	}

	public boolean isHard()
	{
		return this.isPresent() && !this.isSoft();
	}

	public DyeColor getColor()
	{
		if (this.color == null)
		{
			throw new IllegalStateException();
		} else
		{
			return this.color;
		}
	}

	public String getSerializedName()
	{
		return this.name;
	}

	private static Map<String, CableFoam> createNameMap()
	{
		Map<String, CableFoam> ret = new HashMap<>();

		for (CableFoam v : VALUES)
		{
			ret.put(v.name, v);
		}

		return ret;
	}

	private static Map<DyeColor, CableFoam> createColorMap()
	{
		Map<DyeColor, CableFoam> ret = new EnumMap<>(DyeColor.class);

		for (CableFoam v : VALUES)
		{
			if (v.color != null)
			{
				ret.put(v.color, v);
			}
		}

		return ret;
	}
}
