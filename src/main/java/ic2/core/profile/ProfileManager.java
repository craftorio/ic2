// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.profile;

import java.util.Iterator;
import java.util.function.Consumer;
import net.minecraftforge.fml.client.FMLClientHandler;
import ic2.core.util.ReflectionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FallbackResourceManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.io.SequenceInputStream;
import java.util.Collection;
import java.io.ByteArrayInputStream;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Predicate;
import java.util.Objects;
import java.util.function.Function;
import com.google.common.collect.Iterables;
import ic2.core.init.Rezepte;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import ic2.core.util.Config;
import net.minecraftforge.fml.common.eventhandler.Event;
import ic2.api.event.ProfileEvent;
import java.util.Set;
import net.minecraftforge.common.MinecraftForge;
import java.util.zip.ZipFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import ic2.core.util.LogCategory;
import java.io.FilenameFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import java.io.File;
import ic2.core.IC2;
import ic2.core.init.MainConfig;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.resources.IResourcePack;
import java.util.List;
import java.util.Map;

public class ProfileManager
{
    public static final String EXPERIMENTAL = "Experimental";
    public static final String CLASSIC = "Classic";
    public static final Map<String, Profile> PROFILES;
    @SideOnly(Side.CLIENT)
    private static List<IResourcePack> textureChanges;
    public static Profile selected;
    
    private static Map<String, Profile> addDefaultProfiles() {
        final Map<String, Profile> ret = new TreeMap<String, Profile>(String.CASE_INSENSITIVE_ORDER);
        ret.put("Experimental", new Profile("Experimental", Collections.singleton(TextureStyle.EXPERIMENTAL), Version.NEW, new RecipeChange[0]));
        try {
            ret.put("Classic", ProfileParser.parse(ProfileTarget.fromJar("ic2/profiles/classic")));
        }
        catch (final IOException e) {
            throw new RuntimeException("Error opening profile XML", e);
        }
        return ret;
    }
    
    public static void init() {
        final Config.Value config = MainConfig.get().get("profiles/selected");
        if (config == null) {
            return;
        }
        final String active = config.value;
        final File root = new File(IC2.platform.getMinecraftDir(), "ic2_profiles");
        if (root.exists()) {
            for (final File file : root.listFiles()) {
                if (file.isDirectory()) {
                    for (final File profile : file.listFiles((FilenameFilter)new NameFileFilter("profile.xml"))) {
                        try {
                            final Profile p = ProfileParser.parse(new ProfileRoot(profile.getParentFile()));
                            if (!registerProfile(p)) {
                                IC2.log.warn(LogCategory.General, "Duplicate profile names: %s, skipping profile at %s", p.name, profile);
                            }
                            else {
                                IC2.log.debug(LogCategory.General, "Registered profile %s at %s", p.name, profile);
                            }
                        }
                        catch (final IOException e) {
                            throw new RuntimeException("Error opening " + profile, e);
                        }
                    }
                }
                else if (IOCase.INSENSITIVE.checkEquals(FilenameUtils.getExtension(file.getName()), "zip")) {
                    try (final ZipFile zip = new ZipFile(file)) {
                        final ZipEntry entry = zip.getEntry("profile.xml");
                        if (entry != null) {
                            final Profile p2 = ProfileParser.parse(new ProfileRoot(file));
                            if (!registerProfile(p2)) {
                                IC2.log.warn(LogCategory.General, "Duplicate profile names: %s, skipping profile in %s", p2.name, file);
                            }
                            else {
                                IC2.log.debug(LogCategory.General, "Registered profile %s in %s", p2.name, file);
                            }
                        }
                    }
                    catch (final IOException e2) {
                        IC2.log.warn(LogCategory.General, "Error opening zip at " + file, e2);
                    }
                }
            }
        }
        MinecraftForge.EVENT_BUS.post((Event)new ProfileEvent.Load(Collections.unmodifiableSet((Set<? extends String>)ProfileManager.PROFILES.keySet()), active));
        final Profile profile2 = get(active);
        if (profile2 != null) {
            if (ProfileManager.selected != profile2) {
                IC2.log.info(LogCategory.General, "Switching profiles from %s to %s", ProfileManager.selected.name, active);
            }
            switchProfiles(profile2);
        }
        else {
            IC2.log.warn(LogCategory.General, "Unknown/Invalid profile selected in the profile: %s, must be one of %s", active, ProfileManager.PROFILES);
        }
    }
    
    public static boolean registerProfile(final Profile profile) {
        return ProfileManager.PROFILES.putIfAbsent(profile.name, profile) == null;
    }
    
    public static ChangeAction switchProfiles(final Profile to) {
        final Profile from = ProfileManager.selected;
        if (from == to) {
            return ChangeAction.Nothing;
        }
        MinecraftForge.EVENT_BUS.post((Event)new ProfileEvent.Switch(from.name, to.name));
        applySwitch(to);
        if (from.style != to.style) {
            return ChangeAction.GameReload;
        }
        if (!from.recipeConfigs.equals(to.recipeConfigs) || !from.recipeRemovals.equals(to.recipeRemovals)) {
            return ChangeAction.GameReload;
        }
        if (!from.textures.equals(to.textures)) {
            return ChangeAction.ResourceReload;
        }
        return ChangeAction.Nothing;
    }
    
    private static void applySwitch(final Profile to) {
        ProfileManager.selected = to;
        IC2.version = to.style;
    }
    
    public static Profile get(final String name) {
        return ProfileManager.PROFILES.get(name);
    }
    
    public static Profile getOrError(final String name) {
        final Profile ret = ProfileManager.PROFILES.get(name);
        if (ret != null) {
            return ret;
        }
        throw new IllegalArgumentException("Cannot find profile " + name + "! Only have " + ProfileManager.PROFILES);
    }
    
    public static InputStream getRecipeConfig(final String name) {
        final List<RecipeChange> configs = ProfileManager.selected.processRecipeConfigs(name);
        if (configs.isEmpty()) {
            return Rezepte.getDefaultConfigFile(name);
        }
        final boolean isReplacing = configs.stream().anyMatch(change -> change.type == RecipeChange.ChangeType.REPLACEMENT);
        if (isReplacing && configs.size() == 1) {
            return ((RecipeChange)Iterables.getOnlyElement((Iterable)configs)).getStream();
        }
        final List<InputStream> streams = configs.stream().map((Function<? super Object, ?>)RecipeChange::getStream).filter(Objects::nonNull).collect((Collector<? super Object, ?, List<InputStream>>)Collectors.toList());
        if (!isReplacing) {
            streams.add(0, Rezepte.getDefaultConfigFile(name));
        }
        final byte[] split = { 10 };
        for (int i = configs.size() - 1; i > 0; --i) {
            streams.add(i, new ByteArrayInputStream(split));
        }
        return new SequenceInputStream(Collections.enumeration(streams));
    }
    
    public static void getRecipeRemovals(final String name) {
        final List<Object> configs = ProfileManager.selected.recipeRemovals.get(name);
        if (configs.isEmpty()) {}
    }
    
    @SideOnly(Side.CLIENT)
    public static void doTextureChanges() {
        if (ProfileManager.textureChanges == null) {
            ProfileManager.textureChanges = Collections.emptyList();
        }
        final List<IResourcePack> packs = new ArrayList<IResourcePack>();
        final Map<String, FallbackResourceManager> domainManagers = ReflectionUtil.getValue(Minecraft.getMinecraft().getResourceManager(), Map.class);
        IResourcePack pack = null;
        for (final TextureStyle texture : ProfileManager.selected.textures) {
            final FallbackResourceManager manager = domainManagers.get(texture.mod);
            if (manager == null) {
                continue;
            }
            ReflectionUtil.getValue(manager, List.class).removeAll(ProfileManager.textureChanges);
            pack = texture.applyChanges();
            if (pack == null) {
                continue;
            }
            manager.addResourcePack(pack);
            packs.add(pack);
        }
        final List<IResourcePack> defaultPacks = ReflectionUtil.getValue(FMLClientHandler.instance(), List.class);
        defaultPacks.removeAll(ProfileManager.textureChanges);
        assert !defaultPacks.stream().anyMatch(pack -> pack.getPackName().startsWith("IC2 Profile Pack for "));
        packs.forEach(defaultPacks::add);
        ProfileManager.textureChanges = packs;
    }
    
    static {
        PROFILES = addDefaultProfiles();
        ProfileManager.selected = getOrError("Experimental");
    }
    
    public enum ChangeAction
    {
        Nothing, 
        ResourceReload, 
        GameReload;
    }
}
