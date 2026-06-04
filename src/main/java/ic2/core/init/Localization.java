// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.init;

import net.minecraft.client.resources.Locale;
import net.minecraft.client.resources.I18n;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import ic2.core.util.ReflectionUtil;
import net.minecraft.util.text.translation.LanguageMap;
import java.io.Reader;
import java.io.InputStreamReader;
import com.google.common.base.Charsets;
import java.util.Properties;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Iterator;
import java.util.Set;
import java.io.FileNotFoundException;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import java.util.LinkedHashSet;
import java.util.HashMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.Minecraft;
import java.io.InputStream;
import java.io.IOException;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import java.util.Map;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.FMLCommonHandler;
import java.io.File;

public class Localization
{
    private static final String defaultLang = "en_us";
    private static final String ic2LangKey = "ic2.";
    
    public static void preInit(final File modSourceFile) {
        if (FMLCommonHandler.instance().getSide() == Side.SERVER) {
            final Map<String, String> map = getLanguageMapMap();
            loadServerLangFile(modSourceFile, map);
        }
        else {
            registerResourceReloadHook();
        }
    }
    
    private static void loadServerLangFile(final File modSourceFile, final Map<String, String> out) {
        final String path = "/assets/ic2/" + getLangPath("en_us");
        final InputStream is = Localization.class.getResourceAsStream(path);
        try {
            loadLocalization(is, out);
            IC2.log.trace(LogCategory.Resource, "Successfully loaded server localization.");
        }
        catch (final IOException e) {
            IC2.log.warn(LogCategory.Resource, "Failed to load server localization.");
            e.printStackTrace();
        }
    }
    
    private static String getLangPath(final String language) {
        return "lang_ic2/" + language + ".properties";
    }
    
    @SideOnly(Side.CLIENT)
    private static void registerResourceReloadHook() {
        final IResourceManager resManager = Minecraft.getMinecraft().getResourceManager();
        if (resManager instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager)resManager).registerReloadListener((IResourceManagerReloadListener)new IResourceManagerReloadListener() {
                public void onResourceManagerReload(final IResourceManager manager) {
                    final Map<String, String> tmpMap = new HashMap<String, String>();
                    final Map<String, String> lmMap = Localization.getLanguageMapMap();
                    final Map<String, String> localeMap = Localization.getLocaleMap();
                    final Set<String> languages = new LinkedHashSet<String>();
                    languages.add("en_us");
                    languages.add(Minecraft.getMinecraft().gameSettings.language);
                    for (final String lang : languages) {
                        try {
                            for (final IResource res : manager.getAllResources(new ResourceLocation("ic2", getLangPath(lang)))) {
                                try {
                                    tmpMap.clear();
                                    loadLocalization(res.getInputStream(), tmpMap);
                                    lmMap.putAll(tmpMap);
                                    localeMap.putAll(tmpMap);
                                    IC2.log.debug(LogCategory.Resource, "Loaded translation keys from %s.", res.getResourceLocation());
                                }
                                finally {
                                    try {
                                        res.close();
                                    }
                                    catch (final IOException ex) {}
                                }
                            }
                        }
                        catch (final FileNotFoundException e) {
                            IC2.log.debug(LogCategory.Resource, "No translation file for language %s.", lang);
                        }
                        catch (final IOException e2) {
                            throw new RuntimeException(e2);
                        }
                    }
                }
            });
        }
    }
    
    private static void loadLocalization(final InputStream inputStream, final Map<String, String> out) throws IOException {
        final Properties properties = new Properties();
        properties.load(new InputStreamReader(inputStream, Charsets.UTF_8));
        for (final Map.Entry<Object, Object> entries : properties.entrySet()) {
            final Object key = entries.getKey();
            final Object value = entries.getValue();
            if (key instanceof String && value instanceof String) {
                String newKey = (String)key;
                if (!newKey.startsWith("achievement.") && !newKey.startsWith("itemGroup.") && !newKey.startsWith("death.")) {
                    newKey = "ic2." + newKey;
                }
                out.put(newKey, (String)value);
            }
        }
    }
    
    protected static Map<String, String> getLanguageMapMap() {
        for (final Method method : LanguageMap.class.getDeclaredMethods()) {
            if (method.getReturnType() == LanguageMap.class) {
                method.setAccessible(true);
                final Field mapField = ReflectionUtil.getField(LanguageMap.class, Map.class);
                try {
                    return (Map)mapField.get(method.invoke(null, new Object[0]));
                }
                catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }
    
    protected static Map<String, String> getLocaleMap() {
        final Field localeField = ReflectionUtil.getField(I18n.class, Locale.class);
        final Field mapField = ReflectionUtil.getField(Locale.class, Map.class);
        try {
            return (Map)mapField.get(localeField.get(null));
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String translate(final String key) {
        return net.minecraft.util.text.translation.I18n.translateToLocal(key);
    }
    
    public static String translate(final String key, final Object... args) {
        return net.minecraft.util.text.translation.I18n.translateToLocalFormatted(key, args);
    }
}
