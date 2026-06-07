package ic2.core.init;

import net.minecraft.client.resources.language.I18n;

public class Localization
{
	private static final String defaultLang = "en_us";
	private static final String ic2LangKey = "ic2.";

	public static String translate(String key)
	{
		return I18n.m_118938_(key, new Object[0]);
	}

	public static String translate(String key, Object... args)
	{
		return I18n.m_118938_(key, args);
	}
}
