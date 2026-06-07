package ic2.core.init;

import com.google.common.base.Joiner;
import ic2.core.IC2;
import ic2.core.util.Config;
import ic2.core.util.ConfigUtil;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class MainConfig
{
	public static boolean ignoreInvalidRecipes = false;
	private static Config config;
	private static Config defaultConfig;

	public static void load()
	{
		config = new Config("ic2 general config");
		defaultConfig = new Config("ic2 default config");

		try
		{
			config.load(IC2.class.getResourceAsStream("/assets/ic2/config/general.ini"));
			defaultConfig.load(IC2.class.getResourceAsStream("/assets/ic2/config/general.ini"));
		} catch (Exception e)
		{
			throw new RuntimeException("Error loading base config", e);
		}

		File configFile = getFile();

		try
		{
			if (configFile.exists())
			{
				config.load(configFile);
			}
		} catch (Exception e)
		{
			throw new RuntimeException("Error loading user config", e);
		}

		upgradeContents();
		save();
		ignoreInvalidRecipes = ConfigUtil.getBool(get(), "recipes/ignoreInvalidRecipes");
	}

	public static void save()
	{
		try
		{
			config.save(getFile());
		} catch (Exception e)
		{
			throw new RuntimeException("Error saving user config", e);
		}
	}

	public static Config get()
	{
		return config;
	}

	public static Config.Value getDefault(String config)
	{
		return defaultConfig.get(config);
	}

	public static Iterator<Config.Value> getDefaults(String sub)
	{
		return defaultConfig.getSub(sub).valueIterator();
	}

	private static File getFile()
	{
		File folder = new File(IC2.sideProxy.getMinecraftDir(), "config");
		folder.mkdirs();
		return new File(folder, "IC2.ini");
	}

	private static void upgradeContents()
	{
		if (config.get("worldgen/copperOre") != null)
		{
			String[] ores = new String[] { "copper", "tin", "uranium", "lead" };

			for (String ore : ores)
			{
				Config.Value oldValue = config.remove("worldgen/" + ore + "Ore");
				if (oldValue != null && !oldValue.getBool())
				{
					Config.Value newValue = config.get("worldgen/" + ore + "/enabled");
					newValue.set(false);
				}
			}
		}

		List<String> blacklist = ConfigUtil.asList(ConfigUtil.getString(config, "balance/recyclerBlacklist"));
		if (blacklist.contains("IC2:blockScaffold"))
		{
			blacklist.set(blacklist.indexOf("IC2:blockScaffold"), "IC2:scaffold");
			config.set("balance/recyclerBlacklist", Joiner.on(", ").join(blacklist));
		}

		if (config.get("misc/enableIc2Audio") != null)
		{
			config.get("audio/enabled").set(config.remove("misc/enableIc2Audio").getBool());
		}

		if (config.get("misc/maxAudioSourceCount") != null)
		{
			config.get("audio/maxSourceCount").set(config.remove("misc/maxAudioSourceCount").getInt());
		}
	}
}
