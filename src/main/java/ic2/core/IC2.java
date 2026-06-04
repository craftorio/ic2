// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
package ic2.core;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.IEnergyNet;
import ic2.api.info.IInfoProvider;
import ic2.api.info.Info;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.IEnhancedOverlayProvider;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.IRecipeInputFactory;
import ic2.api.recipe.Recipes;
import ic2.api.tile.ExplosionWhitelist;
import ic2.api.tile.IWrenchable;
import ic2.api.util.IKeyboard;
import ic2.api.util.Keys;
import ic2.core.apihelper.ApiHelper;
import ic2.core.audio.AudioManager;
import ic2.core.block.BlockIC2Fluid;
import ic2.core.block.EntityDynamite;
import ic2.core.block.EntityItnt;
import ic2.core.block.EntityNuke;
import ic2.core.block.EntityStickyDynamite;
import ic2.core.block.beam.EntityParticle;
import ic2.core.block.comp.Components;
import ic2.core.block.comp.Obscuration;
import ic2.core.block.generator.tileentity.TileEntitySemifluidGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntityFluidHeatGenerator;
import ic2.core.block.machine.tileentity.TileEntityBlastFurnace;
import ic2.core.block.machine.tileentity.TileEntityBlockCutter;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.block.machine.tileentity.TileEntityCentrifuge;
import ic2.core.block.machine.tileentity.TileEntityCompressor;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;
import ic2.core.block.machine.tileentity.TileEntityExtractor;
import ic2.core.block.machine.tileentity.TileEntityFermenter;
import ic2.core.block.machine.tileentity.TileEntityLiquidHeatExchanger;
import ic2.core.block.machine.tileentity.TileEntityMacerator;
import ic2.core.block.machine.tileentity.TileEntityMatter;
import ic2.core.block.machine.tileentity.TileEntityMetalFormer;
import ic2.core.block.machine.tileentity.TileEntityOreWashing;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import ic2.core.block.steam.TileEntityCokeKiln;
import ic2.core.command.CommandIc2;
import ic2.core.crop.IC2Crops;
import ic2.core.energy.grid.EnergyNetGlobal;
import ic2.core.init.BlocksItems;
import ic2.core.init.Ic2Loot;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.init.OreValues;
import ic2.core.init.Rezepte;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.EntityBoatCarbon;
import ic2.core.item.EntityBoatElectric;
import ic2.core.item.EntityBoatRubber;
import ic2.core.item.EntityIC2Boat;
import ic2.core.item.GatewayElectricItemManager;
import ic2.core.item.ItemIC2Boat;
import ic2.core.item.armor.jetpack.JetpackAttachmentRecipe;
import ic2.core.item.armor.jetpack.JetpackHandler;
import ic2.core.item.tfbp.Tfbp;
import ic2.core.item.tool.EntityMiningLaser;
import ic2.core.item.tool.EntityParticle;
import ic2.core.item.type.CellType;
import ic2.core.item.type.CraftingItemType;
import ic2.core.network.NetworkManager;
import ic2.core.profile.ProfileManager;
import ic2.core.profile.Version;
import ic2.core.recipe.OreDictionaryEntries;
import ic2.core.recipe.RecipeInputFactory;
import ic2.core.recipe.ScrapboxRecipeManager;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.ConfigUtil;
import ic2.core.util.ItemInfo;
import ic2.core.util.Keyboard;
import ic2.core.util.Log;
import ic2.core.util.LogCategory;
import ic2.core.util.PriorityExecutor;
import ic2.core.util.RotationUtil;
import ic2.core.util.SideGateway;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.uu.UuIndex;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.command.ICommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.IFuelHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import org.lwjgl.opengl.GL11;

@Mod(modid = "ic2", name = "IndustrialCraft 2", version = "2.8.222-ex112", acceptedMinecraftVersions = "[1.12]", useMetadata = true, certificateFingerprint = "de041f9f6187debbc77034a344134053277aa3b0", dependencies = "required-after:forge@[13.20.0.2206,)", guiFactory = "ic2.core.gui.Ic2GuiFactory")
public class IC2 implements IFuelHandler {
  public static final String VERSION = "2.8.222-ex112";
  
  public static final String MODID = "ic2";
  
  public static final String RESOURCE_DOMAIN = "ic2";
  
  static {
    try {
      (new BlockPos(1, 2, 3)).func_177982_a(2, 3, 4);
    } catch (Throwable t) {
      throw new Error("IC2 is incompatible with this environment, use the normal IC2 version, not the dev one.", t);
    } 
  }
  
  public IC2() {
    this.threadPool = new PriorityExecutor(Math.max(Runtime.getRuntime().availableProcessors(), 2));
    Info.ic2ModInstance = instance = this;
  }
  
  public static IC2 getInstance() {
    return instance;
  }
  
  private static IC2 instance = null;
  
  @SidedProxy(clientSide = "ic2.core.PlatformClient", serverSide = "ic2.core.Platform")
  public static Platform platform;
  
  public static SideGateway<NetworkManager> network = new SideGateway("ic2.core.network.NetworkManager", "ic2.core.network.NetworkManagerClient");
  
  @SidedProxy(clientSide = "ic2.core.util.KeyboardClient", serverSide = "ic2.core.util.Keyboard")
  public static Keyboard keyboard;
  
  @SidedProxy(clientSide = "ic2.core.audio.AudioManagerClient", serverSide = "ic2.core.audio.AudioManager")
  public static AudioManager audioManager;
  
  public static Log log;
  
  public static IC2Achievements achievements;
  
  public static TickHandler tickHandler;
  
  @EventHandler
  public void load(FMLPreInitializationEvent event) {
    long startTime = System.nanoTime();
    log = new Log(event.getModLog());
    log.debug(LogCategory.General, "Starting pre-init.");
    MainConfig.load();
    ProfileManager.init();
    Localization.preInit(event.getSourceFile());
    tickHandler = new TickHandler();
    audioManager.initialize();
    Recipes.inputFactory = (IRecipeInputFactory)new RecipeInputFactory();
    ElectricItem.manager = (IElectricItemManager)new GatewayElectricItemManager();
    ElectricItem.rawManager = (IElectricItemManager)new ElectricItemManager();
    Info.itemInfo = (IInfoProvider)new ItemInfo();
    Keys.instance = (IKeyboard)keyboard;
    Components.init();
    BlocksItems.init();
    Blocks.field_150343_Z.func_149752_b(60.0F);
    Blocks.field_150381_bn.func_149752_b(60.0F);
    Blocks.field_150477_bB.func_149752_b(60.0F);
    Blocks.field_150467_bQ.func_149752_b(60.0F);
    Blocks.field_150355_j.func_149752_b(30.0F);
    Blocks.field_150358_i.func_149752_b(30.0F);
    Blocks.field_150353_l.func_149752_b(30.0F);
    ExplosionWhitelist.addWhitelistedBlock(Blocks.field_150357_h);
    ScrapboxRecipeManager.setup();
    Tfbp.init();
    TileEntityCanner.init();
    TileEntityCompressor.init();
    TileEntityExtractor.init();
    TileEntityMacerator.init();
    TileEntityRecycler.init();
    TileEntityCentrifuge.init();
    TileEntityMatter.init();
    TileEntityMetalFormer.init();
    TileEntitySemifluidGenerator.init();
    TileEntityOreWashing.init();
    TileEntityFluidHeatGenerator.init();
    TileEntityBlockCutter.init();
    TileEntityBlastFurnace.init();
    TileEntityLiquidHeatExchanger.init();
    TileEntityFermenter.init();
    TileEntityElectrolyzer.init();
    TileEntityCokeKiln.init();
    Rezepte.registerRecipes();
    EntityIC2Boat.init();
    MinecraftForge.EVENT_BUS.register(this);
    Rezepte.registerWithSorter();
    for (String oreName : OreDictionary.getOreNames()) {
      for (ItemStack ore : OreDictionary.getOres(oreName))
        registerOre(new OreDictionary.OreRegisterEvent(oreName, ore)); 
    } 
    OreDictionaryEntries.load();
    EnergyNet.instance = (IEnergyNet)EnergyNetGlobal.create();
    IC2Crops.init();
    IC2Potion.init();
    ApiHelper.preload();
    achievements = new IC2Achievements();
    Ic2Loot.init();
    EntityRegistry.registerModEntity(getIdentifier("mining_laser"), EntityMiningLaser.class, "MiningLaser", 0, this, 160, 5, true);
    EntityRegistry.registerModEntity(getIdentifier("dynamite"), EntityDynamite.class, "Dynamite", 1, this, 160, 5, true);
    EntityRegistry.registerModEntity(getIdentifier("sticky_dynamite"), EntityStickyDynamite.class, "StickyDynamite", 2, this, 160, 5, true);
    EntityRegistry.registerModEntity(getIdentifier("itnt"), EntityItnt.class, "Itnt", 3, this, 160, 5, true);
    EntityRegistry.registerModEntity(getIdentifier("nuke"), EntityNuke.class, "Nuke", 4, this, 160, 5, true);
    EntityRegistry.registerModEntity(getIdentifier("carbon_boat"), EntityBoatCarbon.class, "BoatCarbon", 5, this, 80, 3, true);
    EntityRegistry.registerModEntity(getIdentifier("rubber_boat"), EntityBoatRubber.class, "BoatRubber", 6, this, 80, 3, true);
    EntityRegistry.registerModEntity(getIdentifier("electric_boat"), EntityBoatElectric.class, "BoatElectric", 7, this, 80, 3, true);
    EntityRegistry.registerModEntity(getIdentifier("particle"), EntityParticle.class, "Particle", 8, this, 160, 1, true);
    if (Util.inDev())
      EntityRegistry.registerModEntity(getIdentifier("beam"), EntityParticle.class, "Beam", 9, this, 160, 1, true); 
    EntityRegistry.registerModEntity(getIdentifier("fireproof_item"), ItemIC2Boat.FireproofItem.class, "FireproofItem", 10, this, 80, 1, false);
    int d = Integer.parseInt((new SimpleDateFormat("Mdd")).format(new Date()));
    suddenlyHoes = (d > Math.cbrt(6.4E7D) && d < Math.cbrt(6.5939264E7D));
    seasonal = (d > Math.cbrt(1.089547389E9D) && d < Math.cbrt(1.338273208E9D));
    GameRegistry.registerWorldGenerator(new Ic2WorldDecorator(), 0);
    GameRegistry.registerFuelHandler(this);
    MinecraftForge.EVENT_BUS.register(new IC2BucketHandler());
    TeBlock.registerTeMappings();
    Obscuration.ObscurationComponentEventHandler.init();
    platform.preInit();
    initialized = true;
    log.debug(LogCategory.General, "Finished pre-init after %d ms.", new Object[] { Long.valueOf((System.nanoTime() - startTime) / 1000000L) });
  }
  
  @EventHandler
  public void init(FMLInitializationEvent event) {
    long startTime = System.nanoTime();
    log.debug(LogCategory.General, "Starting init.");
    ScrapboxRecipeManager.load();
    new ChunkLoaderLogic();
    TeBlock.buildDummies();
    IC2Crops.ensureInit();
    log.debug(LogCategory.General, "Finished init after %d ms.", new Object[] { Long.valueOf((System.nanoTime() - startTime) / 1000000L) });
  }
  
  @EventHandler
  public void modsLoaded(FMLPostInitializationEvent event) {
    long startTime = System.nanoTime();
    log.debug(LogCategory.General, "Starting post-init.");
    if (!initialized)
      platform.displayError("IndustrialCraft 2 has failed to initialize properly.", new Object[0]); 
    Rezepte.loadFailedRecipes();
    for (IRecipeInput input : ConfigUtil.asRecipeInputList(MainConfig.get(), "misc/additionalValuableOres")) {
      for (ItemStack stack : input.getInputs())
        OreValues.add(stack, 1); 
    } 
    if (loadSubModule("bcIntegration"))
      log.debug(LogCategory.SubModule, "BuildCraft integration module loaded."); 
    List<IRecipeInput> purgedRecipes = new ArrayList<>();
    purgedRecipes.addAll(ConfigUtil.asRecipeInputList(MainConfig.get(), "recipes/purge"));
    if (ConfigUtil.getBool(MainConfig.get(), "balance/disableEnderChest"))
      purgedRecipes.add(Recipes.inputFactory.forStack(new ItemStack(Blocks.field_150477_bB))); 
    List<IRecipe> recipesToPurge = new ArrayList<>();
    for (IRecipe recipe : ForgeRegistries.RECIPES) {
      ItemStack output = recipe.func_77571_b();
      if (StackUtil.isEmpty(output))
        continue; 
      if (recipe.getRegistryName().func_110624_b() == "ic2")
        continue; 
      for (IRecipeInput input : purgedRecipes) {
        if (input.matches(output))
          recipesToPurge.add(recipe); 
      } 
    } 
    recipesToPurge.stream().map(IForgeRegistryEntry::getRegistryName).forEach((IForgeRegistryModifiable)ForgeRegistries.RECIPES::remove);
    if (ConfigUtil.getBool(MainConfig.get(), "recipes/smeltToIc2Items")) {
      Map<ItemStack, ItemStack> smeltingMap = FurnaceRecipes.func_77602_a().func_77599_b();
      for (Map.Entry<ItemStack, ItemStack> entry : smeltingMap.entrySet()) {
        ItemStack output = entry.getValue();
        if (StackUtil.isEmpty(output))
          continue; 
        boolean found = false;
        for (int oreId : OreDictionary.getOreIDs(output)) {
          String oreName = OreDictionary.getOreName(oreId);
          for (ItemStack ore : OreDictionary.getOres(oreName)) {
            if (ore.getItem() != null && Util.getName(ore.getItem()).func_110624_b().equals("ic2")) {
              entry.setValue(StackUtil.copyWithSize(ore, StackUtil.getSize(output)));
              found = true;
              break;
            } 
          } 
          if (found)
            break; 
        } 
      } 
    } 
    TileEntityRecycler.initLate();
    JetpackAttachmentRecipe.init();
    JetpackHandler.init();
    UuIndex.instance.init();
    UuIndex.instance.refresh(true);
    platform.onPostInit();
    log.debug(LogCategory.General, "Finished post-init after %d ms.", new Object[] { Long.valueOf((System.nanoTime() - startTime) / 1000000L) });
    log.info(LogCategory.General, "%s version %s loaded.", new Object[] { "ic2", "2.8.222-ex112" });
  }
  
  @EventHandler
  public void finished(FMLLoadCompleteEvent event) {}
  
  private static boolean loadSubModule(String name) {
    log.debug(LogCategory.SubModule, "Loading %s submodule: %s.", new Object[] { "ic2", name });
    try {
      Class<?> subModuleClass = IC2.class.getClassLoader().loadClass("ic2." + name + ".SubModule");
      return ((Boolean)subModuleClass.getMethod("init", new Class[0]).invoke(null, new Object[0])).booleanValue();
    } catch (Throwable t) {
      log.debug(LogCategory.SubModule, "Submodule %s not loaded.", new Object[] { name });
      return false;
    } 
  }
  
  @EventHandler
  public void serverStart(FMLServerStartingEvent event) {
    event.registerServerCommand((ICommand)new CommandIc2());
  }
  
  public int getBurnTime(ItemStack stack) {
    if (!BlockName.sapling.hasItemStack())
      return 0; 
    if (stack != null) {
      Item item = stack.getItem();
      if (StackUtil.checkItemEquality(stack, BlockName.sapling.getItemStack()))
        return 80; 
      if (item == Items.field_151120_aE)
        return 50; 
      if (item == Item.func_150898_a((Block)Blocks.field_150434_aF))
        return 50; 
      if (StackUtil.checkItemEquality(stack, ItemName.crafting.getItemStack((Enum)CraftingItemType.scrap)))
        return 350; 
      if (StackUtil.checkItemEquality(stack, ItemName.crafting.getItemStack((Enum)CraftingItemType.scrap_box)))
        return 3150; 
      if (item == ItemName.fluid_cell.getInstance()) {
        FluidStack fs = FluidUtil.getFluidContained(stack);
        if (fs != null && fs.getFluid() == FluidRegistry.LAVA) {
          int ret = TileEntityFurnace.func_145952_a(new ItemStack(Items.field_151129_at));
          return ret * fs.amount / 1000;
        } 
      } else if (StackUtil.checkItemEquality(stack, ItemName.cell.getItemStack((Enum)CellType.lava))) {
        return TileEntityFurnace.func_145952_a(new ItemStack(Items.field_151129_at));
      } 
    } 
    return 0;
  }
  
  public static Random random = new Random();
  
  @SubscribeEvent
  public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {}
  
  @SubscribeEvent
  public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
    if (platform.isSimulating())
      keyboard.removePlayerReferences(event.player); 
  }
  
  @SubscribeEvent
  public void onWorldUnload(WorldEvent.Unload event) {
    WorldData.onWorldUnload(event.getWorld());
  }
  
  public static void explodeMachineAt(World world, int x, int y, int z, boolean noDrop) {
    ExplosionIC2 explosion = new ExplosionIC2(world, null, 0.5D + x, 0.5D + y, 0.5D + z, 2.5F, 0.75F);
    explosion.destroy(x, y, z, noDrop);
    explosion.doExplosion();
  }
  
  public static int getSeaLevel(World world) {
    return world.field_73011_w.func_76557_i();
  }
  
  public static int getWorldHeight(World world) {
    return world.func_72800_K();
  }
  
  @SubscribeEvent
  public void registerOre(OreDictionary.OreRegisterEvent event) {
    String oreClass = event.getName();
    ItemStack ore = event.getOre();
    if (!(ore.getItem() instanceof net.minecraft.item.ItemBlock))
      return; 
    int multiplier = 1;
    if (oreClass.startsWith("dense")) {
      multiplier *= 3;
      oreClass = oreClass.substring("dense".length());
    } 
    int value = 0;
    if (oreClass.equals("oreCoal")) {
      value = 1;
    } else if (oreClass.equals("oreCopper") || oreClass.equals("oreTin") || oreClass.equals("oreLead") || oreClass.equals("oreQuartz")) {
      value = 2;
    } else if (oreClass.equals("oreIron") || oreClass.equals("oreGold") || oreClass.equals("oreRedstone") || oreClass.equals("oreLapis") || oreClass.equals("oreSilver")) {
      value = 3;
    } else if (oreClass.equals("oreUranium") || oreClass.equals("oreGemRuby") || oreClass.equals("oreGemGreenSapphire") || oreClass.equals("oreGemSapphire") || oreClass.equals("oreRuby") || oreClass.equals("oreGreenSapphire") || oreClass.equals("oreSapphire")) {
      value = 4;
    } else if (oreClass.equals("oreDiamond") || oreClass.equals("oreEmerald") || oreClass.equals("oreTungsten")) {
      value = 5;
    } else if (oreClass.startsWith("ore")) {
      value = 1;
    } 
    if (value > 0 && multiplier >= 1)
      OreValues.add(ore, value * multiplier); 
  }
  
  @SubscribeEvent
  public void onLivingSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
    if (seasonal && (event.getEntityLiving() instanceof net.minecraft.entity.monster.EntityZombie || event.getEntityLiving() instanceof net.minecraft.entity.monster.EntitySkeleton) && (event.getEntityLiving().func_130014_f_()).field_73012_v.nextFloat() < 0.1F) {
      EntityLiving entity = (EntityLiving)event.getEntityLiving();
      for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
        entity.func_184642_a(slot, Float.NEGATIVE_INFINITY); 
      if (entity instanceof net.minecraft.entity.monster.EntityZombie)
        entity.func_184201_a(EntityEquipmentSlot.MAINHAND, ItemName.nano_saber.getItemStack()); 
      if ((entity.getEntityWorld()).field_73012_v.nextFloat() < 0.1F) {
        entity.func_184201_a(EntityEquipmentSlot.HEAD, ItemName.quantum_helmet.getItemStack());
        entity.func_184201_a(EntityEquipmentSlot.CHEST, ItemName.quantum_chestplate.getItemStack());
        entity.func_184201_a(EntityEquipmentSlot.LEGS, ItemName.quantum_leggings.getItemStack());
        entity.func_184201_a(EntityEquipmentSlot.FEET, ItemName.quantum_boots.getItemStack());
      } else {
        entity.func_184201_a(EntityEquipmentSlot.HEAD, ItemName.nano_helmet.getItemStack());
        entity.func_184201_a(EntityEquipmentSlot.CHEST, ItemName.nano_chestplate.getItemStack());
        entity.func_184201_a(EntityEquipmentSlot.LEGS, ItemName.nano_leggings.getItemStack());
        entity.func_184201_a(EntityEquipmentSlot.FEET, ItemName.nano_boots.getItemStack());
      } 
    } 
  }
  
  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  public void onViewRenderFogDensity(EntityViewRenderEvent.FogDensity event) {
    if (!(event.getState().getBlock() instanceof BlockIC2Fluid))
      return; 
    event.setCanceled(true);
    Fluid fluid = ((BlockIC2Fluid)event.getState().getBlock()).getFluid();
    GlStateManager.func_187430_a(GlStateManager.FogMode.EXP);
    event.setDensity((float)Util.map(Math.abs(fluid.getDensity()), 20000.0D, 2.0D));
  }
  
  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  public void onViewRenderFogColors(EntityViewRenderEvent.FogColors event) {
    if (!(event.getState().getBlock() instanceof BlockIC2Fluid))
      return; 
    int color = ((BlockIC2Fluid)event.getState().getBlock()).getColor();
    event.setRed((color >>> 16 & 0xFF) / 255.0F);
    event.setGreen((color >>> 8 & 0xFF) / 255.0F);
    event.setBlue((color & 0xFF) / 255.0F);
  }
  
  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  public void renderEnhancedOverlay(DrawBlockHighlightEvent event) {
    ItemStack inHand = StackUtil.get(event.getPlayer(), EnumHand.MAIN_HAND);
    if (event.getSubID() == 0 && (event.getTarget()).typeOfHit == RayTraceResult.Type.BLOCK && inHand.getItem() instanceof IEnhancedOverlayProvider) {
      World world = (event.getPlayer()).field_70170_p;
      BlockPos blockPos = event.getTarget().getBlockPos();
      EnumFacing side = (event.getTarget()).field_178784_b;
      if (((IEnhancedOverlayProvider)inHand.getItem()).providesEnhancedOverlay(world, blockPos, side, event.getPlayer(), inHand)) {
        GL11.glPushMatrix();
        EnhancedOverlay.transformToFace((Entity)event.getPlayer(), blockPos, side, event.getPartialTicks());
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.5F);
        GL11.glBegin(1);
        GL11.glVertex3d(0.5D, 0.0D, -0.25D);
        GL11.glVertex3d(-0.5D, 0.0D, -0.25D);
        GL11.glVertex3d(0.5D, 0.0D, 0.25D);
        GL11.glVertex3d(-0.5D, 0.0D, 0.25D);
        GL11.glVertex3d(0.25D, 0.0D, -0.5D);
        GL11.glVertex3d(0.25D, 0.0D, 0.5D);
        GL11.glVertex3d(-0.25D, 0.0D, -0.5D);
        GL11.glVertex3d(-0.25D, 0.0D, 0.5D);
        GL11.glVertex3d(0.5D, 0.0D, -0.5D);
        GL11.glVertex3d(-0.5D, 0.0D, -0.5D);
        GL11.glVertex3d(0.5D, 0.0D, 0.5D);
        GL11.glVertex3d(-0.5D, 0.0D, 0.5D);
        GL11.glVertex3d(0.5D, 0.0D, -0.5D);
        GL11.glVertex3d(0.5D, 0.0D, 0.5D);
        GL11.glVertex3d(-0.5D, 0.0D, -0.5D);
        GL11.glVertex3d(-0.5D, 0.0D, 0.5D);
        GL11.glEnd();
        GlStateManager.enableBlend();
        GlStateManager.func_187428_a(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.func_179090_x();
        GlStateManager.depthMask(false);
        Map<EnumFacing, Runnable> additionalRenders = new EnumMap<>(EnumFacing.class);
        if (world.getBlockState(blockPos).getBlock() instanceof IWrenchable) {
          EnumFacing hoveredSpin = RotationUtil.rotateByRay(event.getTarget());
          IWrenchable block = (IWrenchable)world.getBlockState(blockPos).getBlock();
          List<EnhancedOverlay.Segment> skippedSegments = new ArrayList<>();
          for (EnhancedOverlay.Segment segment : EnhancedOverlay.Segment.values()) {
            EnumFacing spin;
            switch (segment) {
              case X:
                spin = side;
                break;
              case Y:
                if (side.func_176740_k().func_176720_b()) {
                  spin = EnumFacing.NORTH;
                  break;
                } 
                spin = EnumFacing.UP;
                break;
              case Z:
                if (side.func_176740_k().func_176720_b()) {
                  spin = EnumFacing.SOUTH;
                  break;
                } 
                spin = EnumFacing.DOWN;
                break;
              case null:
                if (side.func_176740_k().func_176720_b()) {
                  spin = EnumFacing.WEST;
                  break;
                } 
                spin = side.func_176746_e();
                break;
              case null:
                if (side.func_176740_k().func_176720_b()) {
                  spin = EnumFacing.EAST;
                  break;
                } 
                spin = side.func_176735_f();
                break;
              case null:
              case null:
              case null:
              case null:
                spin = side.func_176734_d();
                break;
              default:
                throw new IllegalStateException("Unexpected segment: " + segment);
            } 
            if (block.canSetFacing(world, blockPos, spin, event.getPlayer())) {
              int red, green, blue;
              if (hoveredSpin == spin) {
                red = blue = 0;
                green = 255;
              } else {
                red = green = 0;
                blue = 255;
              } 
              EnhancedOverlay.forFace(side).drawArea(segment, Tessellator.getInstance().getBuffer(), red, green, blue);
              if (hoveredSpin == spin)
                if (side.func_176734_d() == spin) {
                  EnumFacing[] edges = null, sides = null;
                  switch (side.func_176740_k()) {
                    case X:
                      edges = new EnumFacing[] { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH };
                      break;
                    case Y:
                      sides = EnumFacing.Plane.HORIZONTAL.func_179516_a();
                      break;
                    case Z:
                      sides = EnumFacing.Plane.VERTICAL.func_179516_a();
                      edges = new EnumFacing[] { EnumFacing.WEST, EnumFacing.EAST };
                      break;
                  } 
                  if (edges != null)
                    for (EnumFacing face : edges) {
                      additionalRenders.put(face, () -> {
                            GlStateManager.color(red / 255.0F, green / 255.0F, blue / 255.0F, 0.5F);
                            EnhancedOverlay.drawArea(face, new EnhancedOverlay.Segment[] { EnhancedOverlay.Segment.TOP_LEFT, EnhancedOverlay.Segment.TOP, EnhancedOverlay.Segment.TOP_RIGHT, EnhancedOverlay.Segment.BOTTOM_LEFT, EnhancedOverlay.Segment.BOTTOM, EnhancedOverlay.Segment.BOTTOM_RIGHT });
                          });
                    }  
                  if (sides != null)
                    for (EnumFacing face : sides) {
                      additionalRenders.put(face, () -> {
                            GlStateManager.color(red / 255.0F, green / 255.0F, blue / 255.0F, 0.5F);
                            EnhancedOverlay.drawArea(face, new EnhancedOverlay.Segment[] { EnhancedOverlay.Segment.TOP_LEFT, EnhancedOverlay.Segment.LEFT, EnhancedOverlay.Segment.BOTTOM_LEFT, EnhancedOverlay.Segment.TOP_RIGHT, EnhancedOverlay.Segment.RIGHT, EnhancedOverlay.Segment.BOTTOM_RIGHT });
                          });
                    }  
                } else if (segment == EnhancedOverlay.Segment.CENTRE) {
                  additionalRenders.put(spin, () -> {
                        GlStateManager.color(red / 255.0F, green / 255.0F, blue / 255.0F, 0.5F);
                        EnhancedOverlay.drawArea(spin, (EnhancedOverlay.Segment[])skippedSegments.toArray((Object[])new EnhancedOverlay.Segment[skippedSegments.size()]));
                      });
                } else {
                  additionalRenders.put(spin, () -> EnhancedOverlay.forFace(spin).drawSide(Tessellator.getInstance().getBuffer(), red, green, blue));
                }  
            } else {
              skippedSegments.add(segment);
            } 
          } 
        } else {
          EnhancedOverlay.forFace(side).drawArea(EnhancedOverlay.Segment.forRayTrace(event.getTarget()), Tessellator.getInstance().getBuffer(), 0, 0, 0);
        } 
        Runnable r = additionalRenders.remove(side);
        if (r != null)
          r.run(); 
        GL11.glPopMatrix();
        for (Map.Entry<EnumFacing, Runnable> entry : additionalRenders.entrySet()) {
          GlStateManager.func_179094_E();
          EnhancedOverlay.transformToFace((Entity)event.getPlayer(), blockPos, entry.getKey(), event.getPartialTicks());
          ((Runnable)entry.getValue()).run();
          GlStateManager.func_179121_F();
        } 
        GlStateManager.depthMask(true);
        GlStateManager.func_179098_w();
        GlStateManager.func_179084_k();
      } 
    } 
  }
  
  public static ResourceLocation getIdentifier(String name) {
    return new ResourceLocation("ic2", name);
  }
  
  public static boolean suddenlyHoes = false;
  
  public static boolean seasonal = false;
  
  public static boolean initialized = false;
  
  public static Version version = ProfileManager.selected.style;
  
  public static final CreativeTabIC2 tabIC2 = new CreativeTabIC2();
  
  public static final int setBlockNotify = 1;
  
  public static final int setBlockUpdate = 2;
  
  public static final int setBlockNoUpdateFromClient = 4;
  
  public final PriorityExecutor threadPool;
}
