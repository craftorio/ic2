package ic2.api.item;

public enum HudMode
{
	DISABLED("ic2.hud.disabled"),
	BASIC("ic2.hud.basic"),
	EXTENDED("ic2.hud.extended"),
	ADVANCED("ic2.hud.advanced");

	private static final HudMode[] VALUES = values();
	private final String translationKey;

	HudMode(String key)
	{
		this.translationKey = key;
	}

	public static HudMode getFromID(int ID)
	{
		return VALUES[ID % VALUES.length];
	}

	public static int getMaxMode()
	{
		return VALUES.length - 1;
	}

	public boolean shouldDisplay()
	{
		return this != DISABLED;
	}

	public boolean hasTooltip()
	{
		return this == EXTENDED || this == ADVANCED;
	}

	public String getTranslationKey()
	{
		return this.translationKey;
	}

	public int getID()
	{
		return this.ordinal();
	}
}
