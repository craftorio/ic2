// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.renderer.Tessellator;
import ic2.core.util.RotationUtil;
import ic2.api.tile.IWrenchable;
import java.util.EnumMap;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;
import ic2.api.item.IEnhancedOverlayProvider;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fluids.Fluid;
import net.minecraft.client.renderer.GlStateManager;
import ic2.core.block.BlockIC2Fluid;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraft.item.ItemBlock;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fluids.FluidStack;
import ic2.core.item.type.CellType;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidUtil;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.init.Items;
import ic2.core.ref.BlockName;
import net.minecraft.command.ICommand;
import ic2.core.command.CommandIc2;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import java.util.List;
import ic2.core.uu.UuIndex;
import ic2.core.item.armor.jetpack.JetpackHandler;
import ic2.core.item.armor.jetpack.JetpackAttachmentRecipe;
import java.util.Map;
import net.minecraft.item.crafting.FurnaceRecipes;
import java.util.function.Consumer;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import java.util.function.Function;
import net.minecraftforge.registries.IForgeRegistryEntry;
import ic2.core.util.StackUtil;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraft.item.crafting.IRecipe;
import java.util.Collection;
import java.util.ArrayList;
import ic2.core.init.OreValues;
import ic2.api.recipe.IRecipeInput;
import ic2.core.util.ConfigUtil;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import java.util.Iterator;
import ic2.core.block.comp.Obscuration;
import ic2.core.ref.TeBlock;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;
import java.util.Date;
import java.text.SimpleDateFormat;
import ic2.core.item.ItemIC2Boat;
import ic2.core.util.Util;
import ic2.core.item.tool.EntityParticle;
import ic2.core.item.EntityBoatElectric;
import ic2.core.item.EntityBoatRubber;
import ic2.core.item.EntityBoatCarbon;
import ic2.core.block.EntityNuke;
import ic2.core.block.EntityItnt;
import ic2.core.block.EntityStickyDynamite;
import ic2.core.block.EntityDynamite;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import ic2.core.item.tool.EntityMiningLaser;
import ic2.core.init.Ic2Loot;
import ic2.core.apihelper.ApiHelper;
import ic2.core.crop.IC2Crops;
import ic2.api.energy.EnergyNet;
import ic2.core.energy.grid.EnergyNetGlobal;
import ic2.core.recipe.OreDictionaryEntries;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.common.MinecraftForge;
import ic2.core.item.EntityIC2Boat;
import ic2.core.init.Rezepte;
import ic2.core.block.steam.TileEntityCokeKiln;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;
import ic2.core.block.machine.tileentity.TileEntityFermenter;
import ic2.core.block.machine.tileentity.TileEntityLiquidHeatExchanger;
import ic2.core.block.machine.tileentity.TileEntityBlastFurnace;
import ic2.core.block.machine.tileentity.TileEntityBlockCutter;
import ic2.core.block.heatgenerator.tileentity.TileEntityFluidHeatGenerator;
import ic2.core.block.machine.tileentity.TileEntityOreWashing;
import ic2.core.block.generator.tileentity.TileEntitySemifluidGenerator;
import ic2.core.block.machine.tileentity.TileEntityMetalFormer;
import ic2.core.block.machine.tileentity.TileEntityMatter;
import ic2.core.block.machine.tileentity.TileEntityCentrifuge;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import ic2.core.block.machine.tileentity.TileEntityMacerator;
import ic2.core.block.machine.tileentity.TileEntityExtractor;
import ic2.core.block.machine.tileentity.TileEntityCompressor;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.item.tfbp.Tfbp;
import ic2.core.recipe.ScrapboxRecipeManager;
import ic2.api.tile.ExplosionWhitelist;
import net.minecraft.init.Blocks;
import ic2.core.init.BlocksItems;
import ic2.core.block.comp.Components;
import ic2.api.util.Keys;
import ic2.core.util.ItemInfo;
import ic2.core.item.ElectricItemManager;
import ic2.api.item.ElectricItem;
import ic2.core.item.GatewayElectricItemManager;
import ic2.api.recipe.Recipes;
import ic2.core.recipe.RecipeInputFactory;
import ic2.core.init.Localization;
import ic2.core.profile.ProfileManager;
import ic2.core.init.MainConfig;
import ic2.core.util.LogCategory;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import ic2.api.info.Info;
import ic2.core.util.PriorityExecutor;
import ic2.core.profile.Version;
import java.util.Random;
import ic2.core.util.Log;
import ic2.core.audio.AudioManager;
import ic2.core.util.Keyboard;
import ic2.core.network.NetworkManager;
import ic2.core.util.SideGateway;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.IFuelHandler;

@Mod(modid = "ic2", name = "IndustrialCraft 2", version = "2.8.222-ex112", acceptedMinecraftVersions = "[1.12]", useMetadata = true, certificateFingerprint = "de041f9f6187debbc77034a344134053277aa3b0", dependencies = "required-after:forge@[13.20.0.2206,)", guiFactory = "ic2.core.gui.Ic2GuiFactory")
public class IC2 implements IFuelHandler
{
    public static final String VERSION = "2.8.222-ex112";
    public static final String MODID = "ic2";
    public static final String RESOURCE_DOMAIN = "ic2";
    private static IC2 instance;
    @SidedProxy(clientSide = "ic2.core.PlatformClient", serverSide = "ic2.core.Platform")
    public static Platform platform;
    public static SideGateway<NetworkManager> network;
    @SidedProxy(clientSide = "ic2.core.util.KeyboardClient", serverSide = "ic2.core.util.Keyboard")
    public static Keyboard keyboard;
    @SidedProxy(clientSide = "ic2.core.audio.AudioManagerClient", serverSide = "ic2.core.audio.AudioManager")
    public static AudioManager audioManager;
    public static Log log;
    public static IC2Achievements achievements;
    public static TickHandler tickHandler;
    public static Random random;
    public static boolean suddenlyHoes;
    public static boolean seasonal;
    public static boolean initialized;
    public static Version version;
    public static final CreativeTabIC2 tabIC2;
    public static final int setBlockNotify = 1;
    public static final int setBlockUpdate = 2;
    public static final int setBlockNoUpdateFromClient = 4;
    public final PriorityExecutor threadPool;
    
    public IC2() {
        this.threadPool = new PriorityExecutor(Math.max(Runtime.getRuntime().availableProcessors(), 2));
        IC2.instance = this;
        Info.ic2ModInstance = this;
    }
    
    public static IC2 getInstance() {
        return IC2.instance;
    }
    
    @Mod.EventHandler
    public void load(final FMLPreInitializationEvent event) {
        final long startTime = System.nanoTime();
        (IC2.log = new Log(event.getModLog())).debug(LogCategory.General, "Starting pre-init.");
        MainConfig.load();
        ProfileManager.init();
        Localization.preInit(event.getSourceFile());
        IC2.tickHandler = new TickHandler();
        IC2.audioManager.initialize();
        Recipes.inputFactory = new RecipeInputFactory();
        ElectricItem.manager = new GatewayElectricItemManager();
        ElectricItem.rawManager = new ElectricItemManager();
        Info.itemInfo = new ItemInfo();
        Keys.instance = IC2.keyboard;
        Components.init();
        BlocksItems.init();
        Blocks.OBSIDIAN.setResistance(60.0f);
        Blocks.ENCHANTING_TABLE.setResistance(60.0f);
        Blocks.ENDER_CHEST.setResistance(60.0f);
        Blocks.ANVIL.setResistance(60.0f);
        Blocks.WATER.setResistance(30.0f);
        Blocks.FLOWING_WATER.setResistance(30.0f);
        Blocks.LAVA.setResistance(30.0f);
        ExplosionWhitelist.addWhitelistedBlock(Blocks.BEDROCK);
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
        MinecraftForge.EVENT_BUS.register((Object)this);
        Rezepte.registerWithSorter();
        for (final String oreName : OreDictionary.getOreNames()) {
            for (final ItemStack ore : OreDictionary.getOres(oreName)) {
                this.registerOre(new OreDictionary.OreRegisterEvent(oreName, ore));
            }
        }
        OreDictionaryEntries.load();
        EnergyNet.instance = EnergyNetGlobal.create();
        IC2Crops.init();
        IC2Potion.init();
        ApiHelper.preload();
        IC2.achievements = new IC2Achievements();
        Ic2Loot.init();
        EntityRegistry.registerModEntity(getIdentifier("mining_laser"), (Class)EntityMiningLaser.class, "MiningLaser", 0, (Object)this, 160, 5, true);
        EntityRegistry.registerModEntity(getIdentifier("dynamite"), (Class)EntityDynamite.class, "Dynamite", 1, (Object)this, 160, 5, true);
        EntityRegistry.registerModEntity(getIdentifier("sticky_dynamite"), (Class)EntityStickyDynamite.class, "StickyDynamite", 2, (Object)this, 160, 5, true);
        EntityRegistry.registerModEntity(getIdentifier("itnt"), (Class)EntityItnt.class, "Itnt", 3, (Object)this, 160, 5, true);
        EntityRegistry.registerModEntity(getIdentifier("nuke"), (Class)EntityNuke.class, "Nuke", 4, (Object)this, 160, 5, true);
        EntityRegistry.registerModEntity(getIdentifier("carbon_boat"), (Class)EntityBoatCarbon.class, "BoatCarbon", 5, (Object)this, 80, 3, true);
        EntityRegistry.registerModEntity(getIdentifier("rubber_boat"), (Class)EntityBoatRubber.class, "BoatRubber", 6, (Object)this, 80, 3, true);
        EntityRegistry.registerModEntity(getIdentifier("electric_boat"), (Class)EntityBoatElectric.class, "BoatElectric", 7, (Object)this, 80, 3, true);
        EntityRegistry.registerModEntity(getIdentifier("particle"), (Class)EntityParticle.class, "Particle", 8, (Object)this, 160, 1, true);
        if (Util.inDev()) {
            EntityRegistry.registerModEntity(getIdentifier("beam"), (Class)ic2.core.block.beam.EntityParticle.class, "Beam", 9, (Object)this, 160, 1, true);
        }
        EntityRegistry.registerModEntity(getIdentifier("fireproof_item"), (Class)ItemIC2Boat.FireproofItem.class, "FireproofItem", 10, (Object)this, 80, 1, false);
        final int d = Integer.parseInt(new SimpleDateFormat("Mdd").format(new Date()));
        IC2.suddenlyHoes = (d > Math.cbrt(6.4E7) && d < Math.cbrt(6.5939264E7));
        IC2.seasonal = (d > Math.cbrt(1.089547389E9) && d < Math.cbrt(1.338273208E9));
        GameRegistry.registerWorldGenerator((IWorldGenerator)new Ic2WorldDecorator(), 0);
        GameRegistry.registerFuelHandler((IFuelHandler)this);
        MinecraftForge.EVENT_BUS.register((Object)new IC2BucketHandler());
        TeBlock.registerTeMappings();
        Obscuration.ObscurationComponentEventHandler.init();
        IC2.platform.preInit();
        IC2.initialized = true;
        IC2.log.debug(LogCategory.General, "Finished pre-init after %d ms.", (System.nanoTime() - startTime) / 1000000L);
    }
    
    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        final long startTime = System.nanoTime();
        IC2.log.debug(LogCategory.General, "Starting init.");
        ScrapboxRecipeManager.load();
        new ChunkLoaderLogic();
        TeBlock.buildDummies();
        IC2Crops.ensureInit();
        IC2.log.debug(LogCategory.General, "Finished init after %d ms.", (System.nanoTime() - startTime) / 1000000L);
    }
    
    @Mod.EventHandler
    public void modsLoaded(final FMLPostInitializationEvent event) {
        final long startTime = System.nanoTime();
        IC2.log.debug(LogCategory.General, "Starting post-init.");
        if (!IC2.initialized) {
            IC2.platform.displayError("IndustrialCraft 2 has failed to initialize properly.", new Object[0]);
        }
        Rezepte.loadFailedRecipes();
        for (final IRecipeInput input : ConfigUtil.asRecipeInputList(MainConfig.get(), "misc/additionalValuableOres")) {
            for (final ItemStack stack : input.getInputs()) {
                OreValues.add(stack, 1);
            }
        }
        if (loadSubModule("bcIntegration")) {
            IC2.log.debug(LogCategory.SubModule, "BuildCraft integration module loaded.");
        }
        final List<IRecipeInput> purgedRecipes = new ArrayList<IRecipeInput>();
        purgedRecipes.addAll(ConfigUtil.asRecipeInputList(MainConfig.get(), "recipes/purge"));
        if (ConfigUtil.getBool(MainConfig.get(), "balance/disableEnderChest")) {
            purgedRecipes.add(Recipes.inputFactory.forStack(new ItemStack(Blocks.ENDER_CHEST)));
        }
        final List<IRecipe> recipesToPurge = new ArrayList<IRecipe>();
        for (final IRecipe recipe : ForgeRegistries.RECIPES) {
            final ItemStack output = recipe.getRecipeOutput();
            if (StackUtil.isEmpty(output)) {
                continue;
            }
            if (recipe.getRegistryName().getResourceDomain() == "ic2") {
                continue;
            }
            for (final IRecipeInput input2 : purgedRecipes) {
                if (input2.matches(output)) {
                    recipesToPurge.add(recipe);
                    break;
                }
            }
        }
        recipesToPurge.stream().map((Function<? super Object, ?>)IForgeRegistryEntry::getRegistryName).forEach((Consumer<? super Object>)(IForgeRegistryModifiable)ForgeRegistries.RECIPES::remove);
        if (ConfigUtil.getBool(MainConfig.get(), "recipes/smeltToIc2Items")) {
            final Map<ItemStack, ItemStack> smeltingMap = FurnaceRecipes.instance().getSmeltingList();
            for (final Map.Entry<ItemStack, ItemStack> entry : smeltingMap.entrySet()) {
                final ItemStack output2 = entry.getValue();
                if (StackUtil.isEmpty(output2)) {
                    continue;
                }
                boolean found = false;
                for (final int oreId : OreDictionary.getOreIDs(output2)) {
                    final String oreName = OreDictionary.getOreName(oreId);
                    for (final ItemStack ore : OreDictionary.getOres(oreName)) {
                        if (ore.getItem() != null && Util.getName(ore.getItem()).getResourceDomain().equals("ic2")) {
                            entry.setValue(StackUtil.copyWithSize(ore, StackUtil.getSize(output2)));
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
            }
        }
        TileEntityRecycler.initLate();
        JetpackAttachmentRecipe.init();
        JetpackHandler.init();
        UuIndex.instance.init();
        UuIndex.instance.refresh(true);
        IC2.platform.onPostInit();
        IC2.log.debug(LogCategory.General, "Finished post-init after %d ms.", (System.nanoTime() - startTime) / 1000000L);
        IC2.log.info(LogCategory.General, "%s version %s loaded.", "ic2", "2.8.222-ex112");
    }
    
    @Mod.EventHandler
    public void finished(final FMLLoadCompleteEvent event) {
    }
    
    private static boolean loadSubModule(final String name) {
        IC2.log.debug(LogCategory.SubModule, "Loading %s submodule: %s.", "ic2", name);
        try {
            final Class<?> subModuleClass = IC2.class.getClassLoader().loadClass("ic2." + name + ".SubModule");
            return (boolean)subModuleClass.getMethod("init", (Class<?>[])new Class[0]).invoke(null, new Object[0]);
        }
        catch (final Throwable t) {
            IC2.log.debug(LogCategory.SubModule, "Submodule %s not loaded.", name);
            return false;
        }
    }
    
    @Mod.EventHandler
    public void serverStart(final FMLServerStartingEvent event) {
        event.registerServerCommand((ICommand)new CommandIc2());
    }
    
    public int getBurnTime(final ItemStack stack) {
        if (!BlockName.sapling.hasItemStack()) {
            return 0;
        }
        if (stack != null) {
            final Item item = stack.getItem();
            if (StackUtil.checkItemEquality(stack, BlockName.sapling.getItemStack())) {
                return 80;
            }
            if (item == Items.REEDS) {
                return 50;
            }
            if (item == Item.getItemFromBlock((Block)Blocks.CACTUS)) {
                return 50;
            }
            if (StackUtil.checkItemEquality(stack, ItemName.crafting.getItemStack(CraftingItemType.scrap))) {
                return 350;
            }
            if (StackUtil.checkItemEquality(stack, ItemName.crafting.getItemStack(CraftingItemType.scrap_box))) {
                return 3150;
            }
            if (item == ItemName.fluid_cell.getInstance()) {
                final FluidStack fs = FluidUtil.getFluidContained(stack);
                if (fs != null && fs.getFluid() == FluidRegistry.LAVA) {
                    final int ret = TileEntityFurnace.getItemBurnTime(new ItemStack(Items.LAVA_BUCKET));
                    return ret * fs.amount / 1000;
                }
            }
            else if (StackUtil.checkItemEquality(stack, ItemName.cell.getItemStack(CellType.lava))) {
                return TileEntityFurnace.getItemBurnTime(new ItemStack(Items.LAVA_BUCKET));
            }
        }
        return 0;
    }
    
    @SubscribeEvent
    public void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
    }
    
    @SubscribeEvent
    public void onPlayerLogout(final PlayerEvent.PlayerLoggedOutEvent event) {
        if (IC2.platform.isSimulating()) {
            IC2.keyboard.removePlayerReferences(event.player);
        }
    }
    
    @SubscribeEvent
    public void onWorldUnload(final WorldEvent.Unload event) {
        WorldData.onWorldUnload(event.getWorld());
    }
    
    public static void explodeMachineAt(final World world, final int x, final int y, final int z, final boolean noDrop) {
        final ExplosionIC2 explosion = new ExplosionIC2(world, null, 0.5 + x, 0.5 + y, 0.5 + z, 2.5f, 0.75f);
        explosion.destroy(x, y, z, noDrop);
        explosion.doExplosion();
    }
    
    public static int getSeaLevel(final World world) {
        return world.provider.getAverageGroundLevel();
    }
    
    public static int getWorldHeight(final World world) {
        return world.getHeight();
    }
    
    @SubscribeEvent
    public void registerOre(final OreDictionary.OreRegisterEvent event) {
        String oreClass = event.getName();
        final ItemStack ore = event.getOre();
        if (!(ore.getItem() instanceof ItemBlock)) {
            return;
        }
        int multiplier = 1;
        if (oreClass.startsWith("dense")) {
            multiplier *= 3;
            oreClass = oreClass.substring("dense".length());
        }
        int value = 0;
        if (oreClass.equals("oreCoal")) {
            value = 1;
        }
        else if (oreClass.equals("oreCopper") || oreClass.equals("oreTin") || oreClass.equals("oreLead") || oreClass.equals("oreQuartz")) {
            value = 2;
        }
        else if (oreClass.equals("oreIron") || oreClass.equals("oreGold") || oreClass.equals("oreRedstone") || oreClass.equals("oreLapis") || oreClass.equals("oreSilver")) {
            value = 3;
        }
        else if (oreClass.equals("oreUranium") || oreClass.equals("oreGemRuby") || oreClass.equals("oreGemGreenSapphire") || oreClass.equals("oreGemSapphire") || oreClass.equals("oreRuby") || oreClass.equals("oreGreenSapphire") || oreClass.equals("oreSapphire")) {
            value = 4;
        }
        else if (oreClass.equals("oreDiamond") || oreClass.equals("oreEmerald") || oreClass.equals("oreTungsten")) {
            value = 5;
        }
        else if (oreClass.startsWith("ore")) {
            value = 1;
        }
        if (value > 0 && multiplier >= 1) {
            OreValues.add(ore, value * multiplier);
        }
    }
    
    @SubscribeEvent
    public void onLivingSpecialSpawn(final LivingSpawnEvent.SpecialSpawn event) {
        if (IC2.seasonal && (event.getEntityLiving() instanceof EntityZombie || event.getEntityLiving() instanceof EntitySkeleton) && event.getEntityLiving().getEntityWorld().rand.nextFloat() < 0.1f) {
            final EntityLiving entity = (EntityLiving)event.getEntityLiving();
            for (final EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                entity.setDropChance(slot, Float.NEGATIVE_INFINITY);
            }
            if (entity instanceof EntityZombie) {
                entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemName.nano_saber.getItemStack());
            }
            if (entity.getEntityWorld().rand.nextFloat() < 0.1f) {
                entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemName.quantum_helmet.getItemStack());
                entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, ItemName.quantum_chestplate.getItemStack());
                entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, ItemName.quantum_leggings.getItemStack());
                entity.setItemStackToSlot(EntityEquipmentSlot.FEET, ItemName.quantum_boots.getItemStack());
            }
            else {
                entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemName.nano_helmet.getItemStack());
                entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, ItemName.nano_chestplate.getItemStack());
                entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, ItemName.nano_leggings.getItemStack());
                entity.setItemStackToSlot(EntityEquipmentSlot.FEET, ItemName.nano_boots.getItemStack());
            }
        }
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onViewRenderFogDensity(final EntityViewRenderEvent.FogDensity event) {
        if (!(event.getState().getBlock() instanceof BlockIC2Fluid)) {
            return;
        }
        event.setCanceled(true);
        final Fluid fluid = ((BlockIC2Fluid)event.getState().getBlock()).getFluid();
        GlStateManager.setFog(GlStateManager.FogMode.EXP);
        event.setDensity((float)Util.map(Math.abs(fluid.getDensity()), 20000.0, 2.0));
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onViewRenderFogColors(final EntityViewRenderEvent.FogColors event) {
        if (!(event.getState().getBlock() instanceof BlockIC2Fluid)) {
            return;
        }
        final int color = ((BlockIC2Fluid)event.getState().getBlock()).getColor();
        event.setRed((color >>> 16 & 0xFF) / 255.0f);
        event.setGreen((color >>> 8 & 0xFF) / 255.0f);
        event.setBlue((color & 0xFF) / 255.0f);
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void renderEnhancedOverlay(final DrawBlockHighlightEvent event) {
        final ItemStack inHand = StackUtil.get(event.getPlayer(), EnumHand.MAIN_HAND);
        if (event.getSubID() == 0 && event.getTarget().typeOfHit == RayTraceResult.Type.BLOCK && inHand.getItem() instanceof IEnhancedOverlayProvider) {
            final World world = event.getPlayer().world;
            final BlockPos blockPos = event.getTarget().getBlockPos();
            final EnumFacing side = event.getTarget().sideHit;
            if (((IEnhancedOverlayProvider)inHand.getItem()).providesEnhancedOverlay(world, blockPos, side, event.getPlayer(), inHand)) {
                GL11.glPushMatrix();
                EnhancedOverlay.transformToFace((Entity)event.getPlayer(), blockPos, side, event.getPartialTicks());
                GL11.glLineWidth(2.0f);
                GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.5f);
                GL11.glBegin(1);
                GL11.glVertex3d(0.5, 0.0, -0.25);
                GL11.glVertex3d(-0.5, 0.0, -0.25);
                GL11.glVertex3d(0.5, 0.0, 0.25);
                GL11.glVertex3d(-0.5, 0.0, 0.25);
                GL11.glVertex3d(0.25, 0.0, -0.5);
                GL11.glVertex3d(0.25, 0.0, 0.5);
                GL11.glVertex3d(-0.25, 0.0, -0.5);
                GL11.glVertex3d(-0.25, 0.0, 0.5);
                GL11.glVertex3d(0.5, 0.0, -0.5);
                GL11.glVertex3d(-0.5, 0.0, -0.5);
                GL11.glVertex3d(0.5, 0.0, 0.5);
                GL11.glVertex3d(-0.5, 0.0, 0.5);
                GL11.glVertex3d(0.5, 0.0, -0.5);
                GL11.glVertex3d(0.5, 0.0, 0.5);
                GL11.glVertex3d(-0.5, 0.0, -0.5);
                GL11.glVertex3d(-0.5, 0.0, 0.5);
                GL11.glEnd();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                final Map<EnumFacing, Runnable> additionalRenders = new EnumMap<EnumFacing, Runnable>(EnumFacing.class);
                if (world.getBlockState(blockPos).getBlock() instanceof IWrenchable) {
                    final EnumFacing hoveredSpin = RotationUtil.rotateByRay(event.getTarget());
                    final IWrenchable block = (IWrenchable)world.getBlockState(blockPos).getBlock();
                    final List<EnhancedOverlay.Segment> skippedSegments = new ArrayList<EnhancedOverlay.Segment>();
                    for (final EnhancedOverlay.Segment segment : EnhancedOverlay.Segment.values()) {
                        EnumFacing spin = null;
                        switch (segment) {
                            case CENTRE: {
                                spin = side;
                                break;
                            }
                            case TOP: {
                                if (side.getAxis().isVertical()) {
                                    spin = EnumFacing.NORTH;
                                    break;
                                }
                                spin = EnumFacing.UP;
                                break;
                            }
                            case BOTTOM: {
                                if (side.getAxis().isVertical()) {
                                    spin = EnumFacing.SOUTH;
                                    break;
                                }
                                spin = EnumFacing.DOWN;
                                break;
                            }
                            case LEFT: {
                                if (side.getAxis().isVertical()) {
                                    spin = EnumFacing.WEST;
                                    break;
                                }
                                spin = side.rotateY();
                                break;
                            }
                            case RIGHT: {
                                if (side.getAxis().isVertical()) {
                                    spin = EnumFacing.EAST;
                                    break;
                                }
                                spin = side.rotateYCCW();
                                break;
                            }
                            case TOP_LEFT:
                            case TOP_RIGHT:
                            case BOTTOM_LEFT:
                            case BOTTOM_RIGHT: {
                                spin = side.getOpposite();
                                break;
                            }
                            default: {
                                throw new IllegalStateException("Unexpected segment: " + segment);
                            }
                        }
                        if (block.canSetFacing(world, blockPos, spin, event.getPlayer())) {
                            int red;
                            int blue;
                            int green;
                            if (hoveredSpin == spin) {
                                blue = (red = 0);
                                green = 255;
                            }
                            else {
                                green = (red = 0);
                                blue = 255;
                            }
                            EnhancedOverlay.forFace(side).drawArea(segment, Tessellator.getInstance().getBuffer(), red, green, blue);
                            if (hoveredSpin == spin) {
                                if (side.getOpposite() == spin) {
                                    EnumFacing[] edges = null;
                                    EnumFacing[] sides = null;
                                    switch (side.getAxis()) {
                                        case X: {
                                            edges = new EnumFacing[] { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH };
                                            break;
                                        }
                                        case Y: {
                                            sides = EnumFacing.Plane.HORIZONTAL.facings();
                                            break;
                                        }
                                        case Z: {
                                            sides = EnumFacing.Plane.VERTICAL.facings();
                                            edges = new EnumFacing[] { EnumFacing.WEST, EnumFacing.EAST };
                                            break;
                                        }
                                    }
                                    if (edges != null) {
                                        for (final EnumFacing face : edges) {
                                            additionalRenders.put(face, () -> {
                                                GlStateManager.color(red / 255.0f, green / 255.0f, blue / 255.0f, 0.5f);
                                                EnhancedOverlay.drawArea(face, EnhancedOverlay.Segment.TOP_LEFT, EnhancedOverlay.Segment.TOP, EnhancedOverlay.Segment.TOP_RIGHT, EnhancedOverlay.Segment.BOTTOM_LEFT, EnhancedOverlay.Segment.BOTTOM, EnhancedOverlay.Segment.BOTTOM_RIGHT);
                                                return;
                                            });
                                        }
                                    }
                                    if (sides != null) {
                                        for (final EnumFacing face : sides) {
                                            additionalRenders.put(face, () -> {
                                                GlStateManager.color(red / 255.0f, green / 255.0f, blue / 255.0f, 0.5f);
                                                EnhancedOverlay.drawArea(face, EnhancedOverlay.Segment.TOP_LEFT, EnhancedOverlay.Segment.LEFT, EnhancedOverlay.Segment.BOTTOM_LEFT, EnhancedOverlay.Segment.TOP_RIGHT, EnhancedOverlay.Segment.RIGHT, EnhancedOverlay.Segment.BOTTOM_RIGHT);
                                                return;
                                            });
                                        }
                                    }
                                }
                                else if (segment == EnhancedOverlay.Segment.CENTRE) {
                                    additionalRenders.put(spin, () -> {
                                        GlStateManager.color(red / 255.0f, green / 255.0f, blue / 255.0f, 0.5f);
                                        EnhancedOverlay.drawArea(spin, (EnhancedOverlay.Segment[])skippedSegments.toArray(new EnhancedOverlay.Segment[skippedSegments.size()]));
                                        return;
                                    });
                                }
                                else {
                                    additionalRenders.put(spin, () -> EnhancedOverlay.forFace(spin).drawSide(Tessellator.getInstance().getBuffer(), red, green, blue));
                                }
                            }
                        }
                        else {
                            skippedSegments.add(segment);
                        }
                    }
                }
                else {
                    EnhancedOverlay.forFace(side).drawArea(EnhancedOverlay.Segment.forRayTrace(event.getTarget()), Tessellator.getInstance().getBuffer(), 0, 0, 0);
                }
                final Runnable r = additionalRenders.remove(side);
                if (r != null) {
                    r.run();
                }
                GL11.glPopMatrix();
                for (final Map.Entry<EnumFacing, Runnable> entry : additionalRenders.entrySet()) {
                    GlStateManager.pushMatrix();
                    EnhancedOverlay.transformToFace((Entity)event.getPlayer(), blockPos, entry.getKey(), event.getPartialTicks());
                    entry.getValue().run();
                    GlStateManager.popMatrix();
                }
                GlStateManager.depthMask(true);
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
            }
        }
    }
    
    public static ResourceLocation getIdentifier(final String name) {
        return new ResourceLocation("ic2", name);
    }
    
    static {
        try {
            new BlockPos(1, 2, 3).add(2, 3, 4);
        }
        catch (final Throwable t) {
            throw new Error("IC2 is incompatible with this environment, use the normal IC2 version, not the dev one.", t);
        }
        IC2.instance = null;
        IC2.network = new SideGateway<NetworkManager>("ic2.core.network.NetworkManager", "ic2.core.network.NetworkManagerClient");
        IC2.random = new Random();
        IC2.suddenlyHoes = false;
        IC2.seasonal = false;
        IC2.initialized = false;
        IC2.version = ProfileManager.selected.style;
        tabIC2 = new CreativeTabIC2();
    }
}
