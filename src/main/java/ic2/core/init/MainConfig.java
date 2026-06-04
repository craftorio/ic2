// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.init;

import java.util.List;
import com.google.common.base.Joiner;
import java.util.Iterator;
import java.io.File;
import ic2.core.util.ConfigUtil;
import ic2.core.IC2;
import ic2.core.util.Config;

public class MainConfig
{
    public static boolean ignoreInvalidRecipes;
    private static Config config;
    private static Config defaultConfig;
    
    public static void load() {
        MainConfig.config = new Config("ic2 general config");
        MainConfig.defaultConfig = new Config("ic2 default config");
        try {
            MainConfig.config.load(IC2.class.getResourceAsStream("/assets/ic2/config/general.ini"));
            MainConfig.defaultConfig.load(IC2.class.getResourceAsStream("/assets/ic2/config/general.ini"));
        }
        catch (final Exception e) {
            throw new RuntimeException("Error loading base config", e);
        }
        final File configFile = getFile();
        try {
            if (configFile.exists()) {
                MainConfig.config.load(configFile);
            }
        }
        catch (final Exception e2) {
            throw new RuntimeException("Error loading user config", e2);
        }
        upgradeContents();
        save();
        MainConfig.ignoreInvalidRecipes = ConfigUtil.getBool(get(), "recipes/ignoreInvalidRecipes");
    }
    
    public static void save() {
        try {
            MainConfig.config.save(getFile());
        }
        catch (final Exception e) {
            throw new RuntimeException("Error saving user config", e);
        }
    }
    
    public static Config get() {
        return MainConfig.config;
    }
    
    public static Config.Value getDefault(final String config) {
        return MainConfig.defaultConfig.get(config);
    }
    
    public static Iterator<Config.Value> getDefaults(final String sub) {
        return MainConfig.defaultConfig.getSub(sub).valueIterator();
    }
    
    private static File getFile() {
        final File folder = new File(IC2.platform.getMinecraftDir(), "config");
        folder.mkdirs();
        return new File(folder, "IC2.ini");
    }
    
    private static void upgradeContents() {
        if (MainConfig.config.get("worldgen/copperOre") != null) {
            final String[] array;
            final String[] ores = array = new String[] { "copper", "tin", "uranium", "lead" };
            for (final String ore : array) {
                final Config.Value oldValue = MainConfig.config.remove("worldgen/" + ore + "Ore");
                if (oldValue != null) {
                    if (!oldValue.getBool()) {
                        final Config.Value newValue = MainConfig.config.get("worldgen/" + ore + "/enabled");
                        newValue.set(false);
                    }
                }
            }
        }
        final List<String> blacklist = ConfigUtil.asList(ConfigUtil.getString(MainConfig.config, "balance/recyclerBlacklist"));
        if (blacklist.contains("IC2:blockScaffold")) {
            blacklist.set(blacklist.indexOf("IC2:blockScaffold"), "IC2:scaffold");
            MainConfig.config.set("balance/recyclerBlacklist", Joiner.on(", ").join((Iterable)blacklist));
        }
        if (MainConfig.config.get("misc/enableIc2Audio") != null) {
            MainConfig.config.get("audio/enabled").set(MainConfig.config.remove("misc/enableIc2Audio").getBool());
        }
        if (MainConfig.config.get("misc/maxAudioSourceCount") != null) {
            MainConfig.config.get("audio/maxSourceCount").set(MainConfig.config.remove("misc/maxAudioSourceCount").getInt());
        }
    }
    
    static {
        MainConfig.ignoreInvalidRecipes = false;
    }
}
