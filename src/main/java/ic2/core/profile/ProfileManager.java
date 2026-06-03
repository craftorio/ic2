package ic2.core.profile;

import com.google.common.collect.Iterables;
import ic2.api.event.ProfileEvent;
import ic2.core.IC2;
import ic2.core.init.MainConfig;
import ic2.core.init.Rezepte;
import ic2.core.util.Config;
import ic2.core.util.LogCategory;
import ic2.core.util.ReflectionUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.NameFileFilter;

public class ProfileManager {
  public static final String EXPERIMENTAL = "Experimental";
  
  public static final String CLASSIC = "Classic";
  
  public static final Map<String, Profile> PROFILES = addDefaultProfiles();
  
  @SideOnly(Side.CLIENT)
  private static List<IResourcePack> textureChanges;
  
  private static Map<String, Profile> addDefaultProfiles() {
    Map<String, Profile> ret = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    ret.put("Experimental", new Profile("Experimental", Collections.singleton(TextureStyle.EXPERIMENTAL), Version.NEW, new RecipeChange[0]));
    try {
      ret.put("Classic", ProfileParser.parse(ProfileTarget.fromJar("ic2/profiles/classic")));
    } catch (IOException e) {
      throw new RuntimeException("Error opening profile XML", e);
    } 
    return ret;
  }
  
  public enum ChangeAction {
    Nothing, ResourceReload, GameReload;
  }
  
  public static void init() {
    Config.Value config = MainConfig.get().get("profiles/selected");
    if (config == null)
      return; 
    String active = config.value;
    File root = new File(IC2.platform.getMinecraftDir(), "ic2_profiles");
    if (root.exists())
      for (File file : root.listFiles()) {
        if (file.isDirectory()) {
          for (File file1 : file.listFiles((FilenameFilter)new NameFileFilter("profile.xml"))) {
            try {
              Profile p = ProfileParser.parse(new ProfileRoot(file1.getParentFile()));
              if (!registerProfile(p)) {
                IC2.log.warn(LogCategory.General, "Duplicate profile names: %s, skipping profile at %s", new Object[] { p.name, file1 });
              } else {
                IC2.log.debug(LogCategory.General, "Registered profile %s at %s", new Object[] { p.name, file1 });
              } 
            } catch (IOException e) {
              throw new RuntimeException("Error opening " + file1, e);
            } 
          } 
        } else if (IOCase.INSENSITIVE.checkEquals(FilenameUtils.getExtension(file.getName()), "zip")) {
          try (ZipFile zip = new ZipFile(file)) {
            ZipEntry entry = zip.getEntry("profile.xml");
            if (entry != null) {
              Profile p = ProfileParser.parse(new ProfileRoot(file));
              if (!registerProfile(p)) {
                IC2.log.warn(LogCategory.General, "Duplicate profile names: %s, skipping profile in %s", new Object[] { p.name, file });
              } else {
                IC2.log.debug(LogCategory.General, "Registered profile %s in %s", new Object[] { p.name, file });
              } 
            } 
          } catch (IOException e) {
            IC2.log.warn(LogCategory.General, "Error opening zip at " + file, new Object[] { e });
          } 
        } 
      }  
    MinecraftForge.EVENT_BUS.post((Event)new ProfileEvent.Load(Collections.unmodifiableSet(PROFILES.keySet()), active));
    Profile profile = get(active);
    if (profile != null) {
      if (selected != profile)
        IC2.log.info(LogCategory.General, "Switching profiles from %s to %s", new Object[] { selected.name, active }); 
      switchProfiles(profile);
    } else {
      IC2.log.warn(LogCategory.General, "Unknown/Invalid profile selected in the profile: %s, must be one of %s", new Object[] { active, PROFILES });
    } 
  }
  
  public static boolean registerProfile(Profile profile) {
    return (PROFILES.putIfAbsent(profile.name, profile) == null);
  }
  
  public static ChangeAction switchProfiles(Profile to) {
    Profile from = selected;
    if (from == to)
      return ChangeAction.Nothing; 
    MinecraftForge.EVENT_BUS.post((Event)new ProfileEvent.Switch(from.name, to.name));
    applySwitch(to);
    if (from.style != to.style)
      return ChangeAction.GameReload; 
    if (!from.recipeConfigs.equals(to.recipeConfigs) || !from.recipeRemovals.equals(to.recipeRemovals))
      return ChangeAction.GameReload; 
    if (!from.textures.equals(to.textures))
      return ChangeAction.ResourceReload; 
    return ChangeAction.Nothing;
  }
  
  private static void applySwitch(Profile to) {
    selected = to;
    IC2.version = to.style;
  }
  
  public static Profile get(String name) {
    return PROFILES.get(name);
  }
  
  public static Profile getOrError(String name) {
    Profile ret = PROFILES.get(name);
    if (ret != null)
      return ret; 
    throw new IllegalArgumentException("Cannot find profile " + name + "! Only have " + PROFILES);
  }
  
  public static InputStream getRecipeConfig(String name) {
    List<RecipeChange> configs = selected.processRecipeConfigs(name);
    if (configs.isEmpty())
      return Rezepte.getDefaultConfigFile(name); 
    boolean isReplacing = configs.stream().anyMatch(change -> (change.type == RecipeChange.ChangeType.REPLACEMENT));
    if (isReplacing && configs.size() == 1)
      return ((RecipeChange)Iterables.getOnlyElement(configs)).getStream(); 
    List<InputStream> streams = (List<InputStream>)configs.stream().map(RecipeChange::getStream).filter(Objects::nonNull).collect(Collectors.toList());
    if (!isReplacing)
      streams.add(0, Rezepte.getDefaultConfigFile(name)); 
    byte[] split = { 10 };
    for (int i = configs.size() - 1; i > 0; i--)
      streams.add(i, new ByteArrayInputStream(split)); 
    return new SequenceInputStream(Collections.enumeration(streams));
  }
  
  public static void getRecipeRemovals(String name) {
    List<Object> configs = selected.recipeRemovals.get(name);
    if (configs.isEmpty());
  }
  
  @SideOnly(Side.CLIENT)
  public static void doTextureChanges() {
    if (textureChanges == null)
      textureChanges = Collections.emptyList(); 
    List<IResourcePack> packs = new ArrayList<>();
    Map<String, FallbackResourceManager> domainManagers = (Map<String, FallbackResourceManager>)ReflectionUtil.getValue(Minecraft.func_71410_x().func_110442_L(), Map.class);
    for (TextureStyle texture : selected.textures) {
      FallbackResourceManager manager = domainManagers.get(texture.mod);
      if (manager == null)
        continue; 
      ((List)ReflectionUtil.getValue(manager, List.class)).removeAll(textureChanges);
      IResourcePack pack = texture.applyChanges();
      if (pack != null) {
        manager.func_110538_a(pack);
        packs.add(pack);
      } 
    } 
    List<IResourcePack> defaultPacks = (List<IResourcePack>)ReflectionUtil.getValue(FMLClientHandler.instance(), List.class);
    defaultPacks.removeAll(textureChanges);
    assert !defaultPacks.stream().anyMatch(pack -> pack.func_130077_b().startsWith("IC2 Profile Pack for "));
    packs.forEach(defaultPacks::add);
    textureChanges = packs;
  }
  
  public static Profile selected = getOrError("Experimental");
}
