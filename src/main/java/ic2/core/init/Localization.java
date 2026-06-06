package ic2.core.init;

import com.google.common.base.Charsets;
import ic2.core.IC2;
import ic2.core.util.LogCategory;
import ic2.core.util.ReflectionUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.Locale;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Localization
{
	private static final String defaultLang = "en_us";
	private static final String ic2LangKey = "ic2.";

	public static void preInit(File modSourceFile)
	{
		if (FMLCommonHandler.instance().getSide() == Side.SERVER)
		{
			Map<String, String> map = getLanguageMapMap();
			loadServerLangFile(modSourceFile, map);
		} else
		{
			registerResourceReloadHook();
		}
	}

	private static void loadServerLangFile(File modSourceFile, Map<String, String> out)
	{
		String path = "/assets/ic2/" + getLangPath("en_us");
		InputStream is = Localization.class.getResourceAsStream(path);

		try
		{
			loadLocalization(is, out);
			IC2.log.trace(LogCategory.Resource, "Successfully loaded server localization.");
		} catch (IOException e)
		{
			IC2.log.warn(LogCategory.Resource, "Failed to load server localization.");
			e.printStackTrace();
		}
	}

	private static String getLangPath(String language)
	{
		return "lang_ic2/" + language + ".properties";
	}

	@SideOnly(Side.CLIENT)
	private static void registerResourceReloadHook()
	{
		IResourceManager resManager = Minecraft.getMinecraft().getResourceManager();
		if (resManager instanceof IReloadableResourceManager)
		{
			((IReloadableResourceManager) resManager).registerReloadListener(new IResourceManagerReloadListener()
			{
				public void onResourceManagerReload(IResourceManager manager)
				{
					Map<String, String> tmpMap = new HashMap<>();
					Map<String, String> lmMap = Localization.getLanguageMapMap();
					Map<String, String> localeMap = Localization.getLocaleMap();
					Set<String> languages = new LinkedHashSet<>();
					languages.add("en_us");
					languages.add(Minecraft.getMinecraft().gameSettings.language);

					for (String lang : languages)
					{
						try
						{
							for (IResource res : manager.getAllResources(new ResourceLocation("ic2", Localization.getLangPath(lang))))
							{
								try
								{
									tmpMap.clear();
									Localization.loadLocalization(res.getInputStream(), tmpMap);
									lmMap.putAll(tmpMap);
									localeMap.putAll(tmpMap);
									IC2.log.debug(LogCategory.Resource, "Loaded translation keys from %s.", res.getResourceLocation());
								} finally
								{
									try
									{
										res.close();
									} catch (IOException var18)
									{
									}
								}
							}
						} catch (FileNotFoundException e)
						{
							IC2.log.debug(LogCategory.Resource, "No translation file for language %s.", lang);
						} catch (IOException e)
						{
							throw new RuntimeException(e);
						}
					}
				}
			});
		}
	}

	private static void loadLocalization(InputStream inputStream, Map<String, String> out) throws IOException
	{
		Properties properties = new Properties();
		properties.load(new InputStreamReader(inputStream, Charsets.UTF_8));

		for (Entry<Object, Object> entries : properties.entrySet())
		{
			Object key = entries.getKey();
			Object value = entries.getValue();
			if (key instanceof String && value instanceof String)
			{
				String newKey = (String) key;
				if (!newKey.startsWith("achievement.") && !newKey.startsWith("itemGroup.") && !newKey.startsWith("death."))
				{
					newKey = "ic2." + newKey;
				}

				out.put(newKey, (String) value);
			}
		}
	}

	protected static Map<String, String> getLanguageMapMap()
	{
		for (Method method : LanguageMap.class.getDeclaredMethods())
		{
			if (method.getReturnType() == LanguageMap.class)
			{
				method.setAccessible(true);
				Field mapField = ReflectionUtil.getField(LanguageMap.class, Map.class);

				try
				{
					return (Map<String, String>) mapField.get(method.invoke(null));
				} catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			}
		}

		return null;
	}

	protected static Map<String, String> getLocaleMap()
	{
		Field localeField = ReflectionUtil.getField(I18n.class, Locale.class);
		Field mapField = ReflectionUtil.getField(Locale.class, Map.class);

		try
		{
			return (Map<String, String>) mapField.get(localeField.get(null));
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static String translate(String key)
	{
		return net.minecraft.util.text.translation.I18n.translateToLocal(key);
	}

	public static String translate(String key, Object... args)
	{
		return net.minecraft.util.text.translation.I18n.translateToLocalFormatted(key, args);
	}
}
