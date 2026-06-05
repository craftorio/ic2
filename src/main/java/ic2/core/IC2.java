package ic2.core;

import ic2.api.info.Info;
import ic2.api.item.ElectricItem;
import ic2.api.item.IEnhancedOverlayProvider;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.api.tile.ExplosionWhitelist;
import ic2.api.tile.IWrenchable;
import ic2.api.util.Keys;
import ic2.core.audio.AudioManager;
import ic2.core.block.BlockIC2Fluid;
import ic2.core.block.comp.Components;
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
import ic2.core.init.BlocksItems;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.init.OreValues;
import ic2.core.init.Rezepte;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.EntityIC2Boat;
import ic2.core.item.GatewayElectricItemManager;
import ic2.core.item.armor.jetpack.JetpackAttachmentRecipe;
import ic2.core.item.armor.jetpack.JetpackHandler;
import ic2.core.item.tfbp.Tfbp;
import ic2.core.item.type.CellType;
import ic2.core.item.type.CraftingItemType;
import ic2.core.network.NetworkManager;
import ic2.core.profile.ProfileManager;
import ic2.core.profile.Version;
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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.FogMode;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult.Type;
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
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import org.lwjgl.opengl.GL11;

@Mod(
   modid = "ic2",
   name = "IndustrialCraft 2",
   version = "2.8.222-ex112",
   acceptedMinecraftVersions = "[1.12]",
   useMetadata = true,
   certificateFingerprint = "de041f9f6187debbc77034a344134053277aa3b0",
   dependencies = "required-after:forge@[13.20.0.2206,)",
   guiFactory = "ic2.core.gui.Ic2GuiFactory"
)
public class IC2 implements IFuelHandler {
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
   public final PriorityExecutor threadPool = new PriorityExecutor(Math.max(Runtime.getRuntime().availableProcessors(), 2));

   public IC2() {
      instance = this;
      Info.ic2ModInstance = this;
   }

   public static IC2 getInstance() {
      return instance;
   }

   @Mod.EventHandler
   public void load(FMLPreInitializationEvent event) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.RuntimeException: Constructor net/minecraftforge/oredict/OreDictionary$OreRegisterEvent.<init>(Ljava/lang/String;Lnet/minecraft/item/ItemStack;)V not found
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.ExprUtil.getSyntheticParametersMask(ExprUtil.java:49)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.appendParamList(InvocationExprent.java:982)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.NewExprent.toJava(NewExprent.java:462)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.getCastedExprent(ExprProcessor.java:1054)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.appendParamList(InvocationExprent.java:1151)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.toJava(InvocationExprent.java:921)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.listToJava(ExprProcessor.java:925)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement.toJava(BasicBlockStatement.java:87)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:860)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.DoStatement.toJava(DoStatement.java:149)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:860)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.DoStatement.toJava(DoStatement.java:149)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:860)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement.toJava(SequenceStatement.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement.toJava(RootStatement.java:36)
      //   at org.jetbrains.java.decompiler.main.ClassWriter.writeMethod(ClassWriter.java:1351)
      //
      // Bytecode:
      // 000: invokestatic java/lang/System.nanoTime ()J
      // 003: lstore 2
      // 004: new ic2/core/util/Log
      // 007: dup
      // 008: aload 1
      // 009: invokevirtual net/minecraftforge/fml/common/event/FMLPreInitializationEvent.getModLog ()Lorg/apache/logging/log4j/Logger;
      // 00c: invokespecial ic2/core/util/Log.<init> (Lorg/apache/logging/log4j/Logger;)V
      // 00f: putstatic ic2/core/IC2.log Lic2/core/util/Log;
      // 012: getstatic ic2/core/IC2.log Lic2/core/util/Log;
      // 015: getstatic ic2/core/util/LogCategory.General Lic2/core/util/LogCategory;
      // 018: ldc "Starting pre-init."
      // 01a: invokevirtual ic2/core/util/Log.debug (Lic2/core/util/LogCategory;Ljava/lang/String;)V
      // 01d: invokestatic ic2/core/init/MainConfig.load ()V
      // 020: invokestatic ic2/core/profile/ProfileManager.init ()V
      // 023: aload 1
      // 024: invokevirtual net/minecraftforge/fml/common/event/FMLPreInitializationEvent.getSourceFile ()Ljava/io/File;
      // 027: invokestatic ic2/core/init/Localization.preInit (Ljava/io/File;)V
      // 02a: new ic2/core/TickHandler
      // 02d: dup
      // 02e: invokespecial ic2/core/TickHandler.<init> ()V
      // 031: putstatic ic2/core/IC2.tickHandler Lic2/core/TickHandler;
      // 034: getstatic ic2/core/IC2.audioManager Lic2/core/audio/AudioManager;
      // 037: invokevirtual ic2/core/audio/AudioManager.initialize ()V
      // 03a: new ic2/core/recipe/RecipeInputFactory
      // 03d: dup
      // 03e: invokespecial ic2/core/recipe/RecipeInputFactory.<init> ()V
      // 041: putstatic ic2/api/recipe/Recipes.inputFactory Lic2/api/recipe/IRecipeInputFactory;
      // 044: new ic2/core/item/GatewayElectricItemManager
      // 047: dup
      // 048: invokespecial ic2/core/item/GatewayElectricItemManager.<init> ()V
      // 04b: putstatic ic2/api/item/ElectricItem.manager Lic2/api/item/IElectricItemManager;
      // 04e: new ic2/core/item/ElectricItemManager
      // 051: dup
      // 052: invokespecial ic2/core/item/ElectricItemManager.<init> ()V
      // 055: putstatic ic2/api/item/ElectricItem.rawManager Lic2/api/item/IElectricItemManager;
      // 058: new ic2/core/util/ItemInfo
      // 05b: dup
      // 05c: invokespecial ic2/core/util/ItemInfo.<init> ()V
      // 05f: putstatic ic2/api/info/Info.itemInfo Lic2/api/info/IInfoProvider;
      // 062: getstatic ic2/core/IC2.keyboard Lic2/core/util/Keyboard;
      // 065: putstatic ic2/api/util/Keys.instance Lic2/api/util/IKeyboard;
      // 068: invokestatic ic2/core/block/comp/Components.init ()V
      // 06b: invokestatic ic2/core/init/BlocksItems.init ()V
      // 06e: getstatic net/minecraft/init/Blocks.OBSIDIAN Lnet/minecraft/block/Block;
      // 071: ldc_w 60.0
      // 074: invokevirtual net/minecraft/block/Block.setResistance (F)Lnet/minecraft/block/Block;
      // 077: pop
      // 078: getstatic net/minecraft/init/Blocks.ENCHANTING_TABLE Lnet/minecraft/block/Block;
      // 07b: ldc_w 60.0
      // 07e: invokevirtual net/minecraft/block/Block.setResistance (F)Lnet/minecraft/block/Block;
      // 081: pop
      // 082: getstatic net/minecraft/init/Blocks.ENDER_CHEST Lnet/minecraft/block/Block;
      // 085: ldc_w 60.0
      // 088: invokevirtual net/minecraft/block/Block.setResistance (F)Lnet/minecraft/block/Block;
      // 08b: pop
      // 08c: getstatic net/minecraft/init/Blocks.ANVIL Lnet/minecraft/block/Block;
      // 08f: ldc_w 60.0
      // 092: invokevirtual net/minecraft/block/Block.setResistance (F)Lnet/minecraft/block/Block;
      // 095: pop
      // 096: getstatic net/minecraft/init/Blocks.WATER Lnet/minecraft/block/BlockStaticLiquid;
      // 099: ldc_w 30.0
      // 09c: invokevirtual net/minecraft/block/BlockStaticLiquid.setResistance (F)Lnet/minecraft/block/Block;
      // 09f: pop
      // 0a0: getstatic net/minecraft/init/Blocks.FLOWING_WATER Lnet/minecraft/block/BlockDynamicLiquid;
      // 0a3: ldc_w 30.0
      // 0a6: invokevirtual net/minecraft/block/BlockDynamicLiquid.setResistance (F)Lnet/minecraft/block/Block;
      // 0a9: pop
      // 0aa: getstatic net/minecraft/init/Blocks.LAVA Lnet/minecraft/block/BlockStaticLiquid;
      // 0ad: ldc_w 30.0
      // 0b0: invokevirtual net/minecraft/block/BlockStaticLiquid.setResistance (F)Lnet/minecraft/block/Block;
      // 0b3: pop
      // 0b4: getstatic net/minecraft/init/Blocks.BEDROCK Lnet/minecraft/block/Block;
      // 0b7: invokestatic ic2/api/tile/ExplosionWhitelist.addWhitelistedBlock (Lnet/minecraft/block/Block;)V
      // 0ba: invokestatic ic2/core/recipe/ScrapboxRecipeManager.setup ()V
      // 0bd: invokestatic ic2/core/item/tfbp/Tfbp.init ()V
      // 0c0: invokestatic ic2/core/block/machine/tileentity/TileEntityCanner.init ()V
      // 0c3: invokestatic ic2/core/block/machine/tileentity/TileEntityCompressor.init ()V
      // 0c6: invokestatic ic2/core/block/machine/tileentity/TileEntityExtractor.init ()V
      // 0c9: invokestatic ic2/core/block/machine/tileentity/TileEntityMacerator.init ()V
      // 0cc: invokestatic ic2/core/block/machine/tileentity/TileEntityRecycler.init ()V
      // 0cf: invokestatic ic2/core/block/machine/tileentity/TileEntityCentrifuge.init ()V
      // 0d2: invokestatic ic2/core/block/machine/tileentity/TileEntityMatter.init ()V
      // 0d5: invokestatic ic2/core/block/machine/tileentity/TileEntityMetalFormer.init ()V
      // 0d8: invokestatic ic2/core/block/generator/tileentity/TileEntitySemifluidGenerator.init ()V
      // 0db: invokestatic ic2/core/block/machine/tileentity/TileEntityOreWashing.init ()V
      // 0de: invokestatic ic2/core/block/heatgenerator/tileentity/TileEntityFluidHeatGenerator.init ()V
      // 0e1: invokestatic ic2/core/block/machine/tileentity/TileEntityBlockCutter.init ()V
      // 0e4: invokestatic ic2/core/block/machine/tileentity/TileEntityBlastFurnace.init ()V
      // 0e7: invokestatic ic2/core/block/machine/tileentity/TileEntityLiquidHeatExchanger.init ()V
      // 0ea: invokestatic ic2/core/block/machine/tileentity/TileEntityFermenter.init ()V
      // 0ed: invokestatic ic2/core/block/machine/tileentity/TileEntityElectrolyzer.init ()V
      // 0f0: invokestatic ic2/core/block/steam/TileEntityCokeKiln.init ()V
      // 0f3: invokestatic ic2/core/init/Rezepte.registerRecipes ()V
      // 0f6: invokestatic ic2/core/item/EntityIC2Boat.init ()V
      // 0f9: getstatic net/minecraftforge/common/MinecraftForge.EVENT_BUS Lnet/minecraftforge/fml/common/eventhandler/EventBus;
      // 0fc: aload 0
      // 0fd: invokevirtual net/minecraftforge/fml/common/eventhandler/EventBus.register (Ljava/lang/Object;)V
      // 100: invokestatic ic2/core/init/Rezepte.registerWithSorter ()V
      // 103: invokestatic net/minecraftforge/oredict/OreDictionary.getOreNames ()[Ljava/lang/String;
      // 106: astore 4
      // 108: aload 4
      // 10a: arraylength
      // 10b: istore 5
      // 10d: bipush 0
      // 10e: istore 6
      // 110: iload 6
      // 112: iload 5
      // 114: if_icmpge 156
      // 117: aload 4
      // 119: iload 6
      // 11b: aaload
      // 11c: astore 7
      // 11e: aload 7
      // 120: invokestatic net/minecraftforge/oredict/OreDictionary.getOres (Ljava/lang/String;)Lnet/minecraft/util/NonNullList;
      // 123: invokevirtual net/minecraft/util/NonNullList.iterator ()Ljava/util/Iterator;
      // 126: astore 8
      // 128: aload 8
      // 12a: invokeinterface java/util/Iterator.hasNext ()Z 1
      // 12f: ifeq 150
      // 132: aload 8
      // 134: invokeinterface java/util/Iterator.next ()Ljava/lang/Object; 1
      // 139: checkcast net/minecraft/item/ItemStack
      // 13c: astore 9
      // 13e: aload 0
      // 13f: new net/minecraftforge/oredict/OreDictionary$OreRegisterEvent
      // 142: dup
      // 143: aload 7
      // 145: aload 9
      // 147: invokespecial net/minecraftforge/oredict/OreDictionary$OreRegisterEvent.<init> (Ljava/lang/String;Lnet/minecraft/item/ItemStack;)V
      // 14a: invokevirtual ic2/core/IC2.registerOre (Lnet/minecraftforge/oredict/OreDictionary$OreRegisterEvent;)V
      // 14d: goto 128
      // 150: iinc 6 1
      // 153: goto 110
      // 156: invokestatic ic2/core/recipe/OreDictionaryEntries.load ()V
      // 159: invokestatic ic2/core/energy/grid/EnergyNetGlobal.create ()Lic2/core/energy/grid/EnergyNetGlobal;
      // 15c: putstatic ic2/api/energy/EnergyNet.instance Lic2/api/energy/IEnergyNet;
      // 15f: invokestatic ic2/core/crop/IC2Crops.init ()V
      // 162: invokestatic ic2/core/IC2Potion.init ()V
      // 165: invokestatic ic2/core/apihelper/ApiHelper.preload ()V
      // 168: new ic2/core/IC2Achievements
      // 16b: dup
      // 16c: invokespecial ic2/core/IC2Achievements.<init> ()V
      // 16f: putstatic ic2/core/IC2.achievements Lic2/core/IC2Achievements;
      // 172: invokestatic ic2/core/init/Ic2Loot.init ()V
      // 175: ldc_w "mining_laser"
      // 178: invokestatic ic2/core/IC2.getIdentifier (Ljava/lang/String;)Lnet/minecraft/util/ResourceLocation;
      // 17b: ldc_w ic2/core/item/tool/EntityMiningLaser
      // 17e: ldc_w "MiningLaser"
      // 181: bipush 0
      // 182: aload 0
      // 183: sipush 160
      // 186: bipush 5
      // 187: bipush 1
      // 188: invokestatic net/minecraftforge/fml/common/registry/EntityRegistry.registerModEntity (Lnet/minecraft/util/ResourceLocation;Ljava/lang/Class;Ljava/lang/String;ILjava/lang/Object;IIZ)V
      // 18b: ldc_w "dynamite"
      // 18e: invokestatic ic2/core/IC2.getIdentifier (Ljava/lang/String;)Lnet/minecraft/util/ResourceLocation;
      // 191: ldc_w ic2/core/block/EntityDynamite
      // 194: ldc_w "Dynamite"
      // 197: bipush 1
      // 198: aload 0
      // 199: sipush 160
      // 19c: bipush 5
      // 19d: bipush 1
      // 19e: invokestatic net/minecraftforge/fml/common/registry/EntityRegistry.registerModEntity (Lnet/minecraft/util/ResourceLocation;Ljava/lang/Class;Ljava/lang/String;ILjava/lang/Object;IIZ)V
      // 1a1: ldc_w "sticky_dynamite"
      // 1a4: invokestatic ic2/core/IC2.getIdentifier (Ljava/lang/String;)Lnet/minecraft/util/ResourceLocation;
      // 1a7: ldc_w ic2/core/block/EntityStickyDynamite
      // 1aa: ldc_w "StickyDynamite"
      // 1ad: bipush 2
      // 1ae: aload 0
      // 1af: sipush 160
      // 1b2: bipush 5
      // 1b3: bipush 1
      // 1b4: invokestatic net/minecraftforge/fml/common/registry/EntityRegistry.registerModEntity (Lnet/minecraft/util/ResourceLocation;Ljava/lang/Class;Ljava/lang/String;ILjava/lang/Object;IIZ)V
      // 1b7: ldc_w "itnt"
      // 1ba: invokestatic ic2/core/IC2.getIdentifier (Ljava/lang/String;)Lnet/minecraft/util/ResourceLocation;
      // 1bd: ldc_w ic2/core/block/EntityItnt
      // 1c0: ldc_w "Itnt"
      // 1c3: bipush 3
      // 1c4: aload 0
      // 1c5: sipush 160
      // 1c8: bipush 5
      // 1c9: bipush 1
      // 1ca: invokestatic net/minecraftforge/fml/common/registry/EntityRegistry.registerModEntity (Lnet/minecraft/util/ResourceLocation;Ljava/lang/Class;Ljava/lang/String;ILjava/lang/Object;IIZ)V
      // 1cd: ldc_w "nuke"
      // 1d0: invokestatic ic2/core/IC2.getIdentifier (Ljava/lang/String;)Lnet/minecraft/util/ResourceLocation;
      // 1d3: ldc_w ic2/core/block/EntityNuke
      // 1d6: ldc_w "Nuke"
      // 1d9: bipush 4
      // 1da: aload 0
      // 1db: sipush 160
      // 1de: bipush 5
      // 1df: bipush 1
      // 1e0: invokestatic net/minecraftforge/fml/common/registry/EntityRegistry.registerModEntity (Lnet/minecraft/util/ResourceLocation;Ljava/lang/Class;Ljava/lang/String;ILjava/lang/Object;IIZ)V
      // 1e3: ldc_w "carbon_boat"
      // 1e6: invokestatic ic2/core/IC2.getIdentifier (Ljava/lang/String;)Lnet/minecraft/util/ResourceLocation;
      // 1e9: ldc_w ic2/core/item/EntityBoatCarbon
      // 1ec: ldc_w "BoatCarbon"
      // 1ef: bipush 5
      // 1f0: aload 0
      // 1f1: bipush 80
      // 1f3: bipush 3
      // 1f4: bipush 1
      // 1f5: invokestatic net/minecraftforge/fml/common/registry/EntityRegistry.registerModEntity (Lnet/minecraft/util/ResourceLocation;Ljava/lang/Class;Ljava/lang/String;ILjava/lang/Object;IIZ)V
      // 1f8: ldc_w "rubber_boat"
      // 1fb: invokestatic ic2/core/IC2.getIdentifier (Ljava/lang/String;)Lnet/minecraft/util/ResourceLocation;
      // 1fe: ldc_w ic2/core/item/EntityBoatRubber
      // 201: ldc_w "BoatRubber"
      // 204: bipush 6
      // 206: aload 0
      // 207: bipush 80
      // 209: bipush 3
      // 20a: bipush 1
      // 20b: invokestatic net/minecraftforge/fml/common/registry/EntityRegistry.registerModEntity (Lnet/minecraft/util/ResourceLocation;Ljava/lang/Class;Ljava/lang/String;ILjava/lang/Object;IIZ)V
      // 20e: ldc_w "electric_boat"
      // 211: invokestatic ic2/core/IC2.getIdentifier (Ljava/lang/String;)Lnet/minecraft/util/ResourceLocation;
      // 214: ldc_w ic2/core/item/EntityBoatElectric
      // 217: ldc_w "BoatElectric"
      // 21a: bipush 7
      // 21c: aload 0
      // 21d: bipush 80
      // 21f: bipush 3
      // 220: bipush 1
      // 221: invokestatic net/minecraftforge/fml/common/registry/EntityRegistry.registerModEntity (Lnet/minecraft/util/ResourceLocation;Ljava/lang/Class;Ljava/lang/String;ILjava/lang/Object;IIZ)V
      // 224: ldc_w "particle"
      // 227: invokestatic ic2/core/IC2.getIdentifier (Ljava/lang/String;)Lnet/minecraft/util/ResourceLocation;
      // 22a: ldc_w ic2/core/item/tool/EntityParticle
      // 22d: ldc_w "Particle"
      // 230: bipush 8
      // 232: aload 0
      // 233: sipush 160
      // 236: bipush 1
      // 237: bipush 1
      // 238: invokestatic net/minecraftforge/fml/common/registry/EntityRegistry.registerModEntity (Lnet/minecraft/util/ResourceLocation;Ljava/lang/Class;Ljava/lang/String;ILjava/lang/Object;IIZ)V
      // 23b: invokestatic ic2/core/util/Util.inDev ()Z
      // 23e: ifeq 258
      // 241: ldc_w "beam"
      // 244: invokestatic ic2/core/IC2.getIdentifier (Ljava/lang/String;)Lnet/minecraft/util/ResourceLocation;
      // 247: ldc_w ic2/core/block/beam/EntityParticle
      // 24a: ldc_w "Beam"
      // 24d: bipush 9
      // 24f: aload 0
      // 250: sipush 160
      // 253: bipush 1
      // 254: bipush 1
      // 255: invokestatic net/minecraftforge/fml/common/registry/EntityRegistry.registerModEntity (Lnet/minecraft/util/ResourceLocation;Ljava/lang/Class;Ljava/lang/String;ILjava/lang/Object;IIZ)V
      // 258: ldc_w "fireproof_item"
      // 25b: invokestatic ic2/core/IC2.getIdentifier (Ljava/lang/String;)Lnet/minecraft/util/ResourceLocation;
      // 25e: ldc ic2/core/item/ItemIC2Boat$FireproofItem
      // 260: ldc_w "FireproofItem"
      // 263: bipush 10
      // 265: aload 0
      // 266: bipush 80
      // 268: bipush 1
      // 269: bipush 0
      // 26a: invokestatic net/minecraftforge/fml/common/registry/EntityRegistry.registerModEntity (Lnet/minecraft/util/ResourceLocation;Ljava/lang/Class;Ljava/lang/String;ILjava/lang/Object;IIZ)V
      // 26d: new java/text/SimpleDateFormat
      // 270: dup
      // 271: ldc_w "Mdd"
      // 274: invokespecial java/text/SimpleDateFormat.<init> (Ljava/lang/String;)V
      // 277: new java/util/Date
      // 27a: dup
      // 27b: invokespecial java/util/Date.<init> ()V
      // 27e: invokevirtual java/text/SimpleDateFormat.format (Ljava/util/Date;)Ljava/lang/String;
      // 281: invokestatic java/lang/Integer.parseInt (Ljava/lang/String;)I
      // 284: istore 4
      // 286: iload 4
      // 288: i2d
      // 289: ldc2_w 6.4E7
      // 28c: invokestatic java/lang/Math.cbrt (D)D
      // 28f: dcmpl
      // 290: ifle 2a4
      // 293: iload 4
      // 295: i2d
      // 296: ldc2_w 6.5939264E7
      // 299: invokestatic java/lang/Math.cbrt (D)D
      // 29c: dcmpg
      // 29d: ifge 2a4
      // 2a0: bipush 1
      // 2a1: goto 2a5
      // 2a4: bipush 0
      // 2a5: putstatic ic2/core/IC2.suddenlyHoes Z
      // 2a8: iload 4
      // 2aa: i2d
      // 2ab: ldc2_w 1.089547389E9
      // 2ae: invokestatic java/lang/Math.cbrt (D)D
      // 2b1: dcmpl
      // 2b2: ifle 2c6
      // 2b5: iload 4
      // 2b7: i2d
      // 2b8: ldc2_w 1.338273208E9
      // 2bb: invokestatic java/lang/Math.cbrt (D)D
      // 2be: dcmpg
      // 2bf: ifge 2c6
      // 2c2: bipush 1
      // 2c3: goto 2c7
      // 2c6: bipush 0
      // 2c7: putstatic ic2/core/IC2.seasonal Z
      // 2ca: new ic2/core/Ic2WorldDecorator
      // 2cd: dup
      // 2ce: invokespecial ic2/core/Ic2WorldDecorator.<init> ()V
      // 2d1: bipush 0
      // 2d2: invokestatic net/minecraftforge/fml/common/registry/GameRegistry.registerWorldGenerator (Lnet/minecraftforge/fml/common/IWorldGenerator;I)V
      // 2d5: aload 0
      // 2d6: invokestatic net/minecraftforge/fml/common/registry/GameRegistry.registerFuelHandler (Lnet/minecraftforge/fml/common/IFuelHandler;)V
      // 2d9: getstatic net/minecraftforge/common/MinecraftForge.EVENT_BUS Lnet/minecraftforge/fml/common/eventhandler/EventBus;
      // 2dc: new ic2/core/IC2BucketHandler
      // 2df: dup
      // 2e0: invokespecial ic2/core/IC2BucketHandler.<init> ()V
      // 2e3: invokevirtual net/minecraftforge/fml/common/eventhandler/EventBus.register (Ljava/lang/Object;)V
      // 2e6: invokestatic ic2/core/ref/TeBlock.registerTeMappings ()V
      // 2e9: invokestatic ic2/core/block/comp/Obscuration$ObscurationComponentEventHandler.init ()V
      // 2ec: getstatic ic2/core/IC2.platform Lic2/core/Platform;
      // 2ef: invokevirtual ic2/core/Platform.preInit ()V
      // 2f2: bipush 1
      // 2f3: putstatic ic2/core/IC2.initialized Z
      // 2f6: getstatic ic2/core/IC2.log Lic2/core/util/Log;
      // 2f9: getstatic ic2/core/util/LogCategory.General Lic2/core/util/LogCategory;
      // 2fc: ldc_w "Finished pre-init after %d ms."
      // 2ff: bipush 1
      // 300: anewarray 4
      // 303: dup
      // 304: bipush 0
      // 305: invokestatic java/lang/System.nanoTime ()J
      // 308: lload 2
      // 309: lsub
      // 30a: ldc2_w 1000000
      // 30d: ldiv
      // 30e: invokestatic java/lang/Long.valueOf (J)Ljava/lang/Long;
      // 311: aastore
      // 312: invokevirtual ic2/core/util/Log.debug (Lic2/core/util/LogCategory;Ljava/lang/String;[Ljava/lang/Object;)V
      // 315: return
   }

   @Mod.EventHandler
   public void init(FMLInitializationEvent event) {
      long startTime = System.nanoTime();
      log.debug(LogCategory.General, "Starting init.");
      ScrapboxRecipeManager.load();
      new ChunkLoaderLogic();
      TeBlock.buildDummies();
      IC2Crops.ensureInit();
      log.debug(LogCategory.General, "Finished init after %d ms.", (System.nanoTime() - startTime) / 1000000L);
   }

   @Mod.EventHandler
   public void modsLoaded(FMLPostInitializationEvent event) {
      long startTime = System.nanoTime();
      log.debug(LogCategory.General, "Starting post-init.");
      if (!initialized) {
         platform.displayError("IndustrialCraft 2 has failed to initialize properly.");
      }

      Rezepte.loadFailedRecipes();

      for (IRecipeInput input : ConfigUtil.asRecipeInputList(MainConfig.get(), "misc/additionalValuableOres")) {
         for (ItemStack stack : input.getInputs()) {
            OreValues.add(stack, 1);
         }
      }

      if (loadSubModule("bcIntegration")) {
         log.debug(LogCategory.SubModule, "BuildCraft integration module loaded.");
      }

      List<IRecipeInput> purgedRecipes = new ArrayList<>();
      purgedRecipes.addAll(ConfigUtil.asRecipeInputList(MainConfig.get(), "recipes/purge"));
      if (ConfigUtil.getBool(MainConfig.get(), "balance/disableEnderChest")) {
         purgedRecipes.add(Recipes.inputFactory.forStack(new ItemStack(Blocks.ENDER_CHEST)));
      }

      List<IRecipe> recipesToPurge = new ArrayList<>();

      for (IRecipe recipe : ForgeRegistries.RECIPES) {
         ItemStack output = recipe.getRecipeOutput();
         if (!StackUtil.isEmpty(output) && recipe.getRegistryName().getResourceDomain() != "ic2") {
            for (IRecipeInput input : purgedRecipes) {
               if (input.matches(output)) {
                  recipesToPurge.add(recipe);
                  break;
               }
            }
         }
      }

      recipesToPurge.stream().map(IForgeRegistryEntry::getRegistryName).forEach(((IForgeRegistryModifiable)ForgeRegistries.RECIPES)::remove);
      if (ConfigUtil.getBool(MainConfig.get(), "recipes/smeltToIc2Items")) {
         Map<ItemStack, ItemStack> smeltingMap = FurnaceRecipes.instance().getSmeltingList();

         for (Entry<ItemStack, ItemStack> entry : smeltingMap.entrySet()) {
            ItemStack output = entry.getValue();
            if (!StackUtil.isEmpty(output)) {
               boolean found = false;

               for (int oreId : OreDictionary.getOreIDs(output)) {
                  String oreName = OreDictionary.getOreName(oreId);

                  for (ItemStack ore : OreDictionary.getOres(oreName)) {
                     if (ore.getItem() != null && Util.getName(ore.getItem()).getResourceDomain().equals("ic2")) {
                        entry.setValue(StackUtil.copyWithSize(ore, StackUtil.getSize(output)));
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
      }

      TileEntityRecycler.initLate();
      JetpackAttachmentRecipe.init();
      JetpackHandler.init();
      UuIndex.instance.init();
      UuIndex.instance.refresh(true);
      platform.onPostInit();
      log.debug(LogCategory.General, "Finished post-init after %d ms.", (System.nanoTime() - startTime) / 1000000L);
      log.info(LogCategory.General, "%s version %s loaded.", "ic2", "2.8.222-ex112");
   }

   @Mod.EventHandler
   public void finished(FMLLoadCompleteEvent event) {
   }

   private static boolean loadSubModule(String name) {
      log.debug(LogCategory.SubModule, "Loading %s submodule: %s.", "ic2", name);

      try {
         Class<?> subModuleClass = IC2.class.getClassLoader().loadClass("ic2." + name + ".SubModule");
         return (Boolean)subModuleClass.getMethod("init").invoke(null);
      } catch (Throwable t) {
         log.debug(LogCategory.SubModule, "Submodule %s not loaded.", name);
         return false;
      }
   }

   @Mod.EventHandler
   public void serverStart(FMLServerStartingEvent event) {
      event.registerServerCommand(new CommandIc2());
   }

   public int getBurnTime(ItemStack stack) {
      if (!BlockName.sapling.hasItemStack()) {
         return 0;
      }

      if (stack != null) {
         Item item = stack.getItem();
         if (StackUtil.checkItemEquality(stack, BlockName.sapling.getItemStack())) {
            return 80;
         }

         if (item == Items.REEDS) {
            return 50;
         }

         if (item == Item.getItemFromBlock(Blocks.CACTUS)) {
            return 50;
         }

         if (StackUtil.checkItemEquality(stack, ItemName.crafting.getItemStack(CraftingItemType.scrap))) {
            return 350;
         }

         if (StackUtil.checkItemEquality(stack, ItemName.crafting.getItemStack(CraftingItemType.scrap_box))) {
            return 3150;
         }

         if (item == ItemName.fluid_cell.getInstance()) {
            FluidStack fs = FluidUtil.getFluidContained(stack);
            if (fs != null && fs.getFluid() == FluidRegistry.LAVA) {
               int ret = TileEntityFurnace.getItemBurnTime(new ItemStack(Items.LAVA_BUCKET));
               return ret * fs.amount / 1000;
            }
         } else if (StackUtil.checkItemEquality(stack, ItemName.cell.getItemStack(CellType.lava))) {
            return TileEntityFurnace.getItemBurnTime(new ItemStack(Items.LAVA_BUCKET));
         }
      }

      return 0;
   }

   @SubscribeEvent
   public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
   }

   @SubscribeEvent
   public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
      if (platform.isSimulating()) {
         keyboard.removePlayerReferences(event.player);
      }
   }

   @SubscribeEvent
   public void onWorldUnload(WorldEvent.Unload event) {
      WorldData.onWorldUnload(event.getWorld());
   }

   public static void explodeMachineAt(World world, int x, int y, int z, boolean noDrop) {
      ExplosionIC2 explosion = new ExplosionIC2(world, null, 0.5 + x, 0.5 + y, 0.5 + z, 2.5F, 0.75F);
      explosion.destroy(x, y, z, noDrop);
      explosion.doExplosion();
   }

   public static int getSeaLevel(World world) {
      return world.provider.getAverageGroundLevel();
   }

   public static int getWorldHeight(World world) {
      return world.getHeight();
   }

   @SubscribeEvent
   public void registerOre(OreDictionary.OreRegisterEvent event) {
      String oreClass = event.getName();
      ItemStack ore = event.getOre();
      if (ore.getItem() instanceof ItemBlock) {
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
         } else if (oreClass.equals("oreIron")
            || oreClass.equals("oreGold")
            || oreClass.equals("oreRedstone")
            || oreClass.equals("oreLapis")
            || oreClass.equals("oreSilver")) {
            value = 3;
         } else if (oreClass.equals("oreUranium")
            || oreClass.equals("oreGemRuby")
            || oreClass.equals("oreGemGreenSapphire")
            || oreClass.equals("oreGemSapphire")
            || oreClass.equals("oreRuby")
            || oreClass.equals("oreGreenSapphire")
            || oreClass.equals("oreSapphire")) {
            value = 4;
         } else if (oreClass.equals("oreDiamond") || oreClass.equals("oreEmerald") || oreClass.equals("oreTungsten")) {
            value = 5;
         } else if (oreClass.startsWith("ore")) {
            value = 1;
         }

         if (value > 0 && multiplier >= 1) {
            OreValues.add(ore, value * multiplier);
         }
      }
   }

   @SubscribeEvent
   public void onLivingSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
      if (seasonal
         && (event.getEntityLiving() instanceof EntityZombie || event.getEntityLiving() instanceof EntitySkeleton)
         && event.getEntityLiving().getEntityWorld().rand.nextFloat() < 0.1F) {
         EntityLiving entity = (EntityLiving)event.getEntityLiving();

         for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            entity.setDropChance(slot, Float.NEGATIVE_INFINITY);
         }

         if (entity instanceof EntityZombie) {
            entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemName.nano_saber.getItemStack());
         }

         if (entity.getEntityWorld().rand.nextFloat() < 0.1F) {
            entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemName.quantum_helmet.getItemStack());
            entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, ItemName.quantum_chestplate.getItemStack());
            entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, ItemName.quantum_leggings.getItemStack());
            entity.setItemStackToSlot(EntityEquipmentSlot.FEET, ItemName.quantum_boots.getItemStack());
         } else {
            entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemName.nano_helmet.getItemStack());
            entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, ItemName.nano_chestplate.getItemStack());
            entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, ItemName.nano_leggings.getItemStack());
            entity.setItemStackToSlot(EntityEquipmentSlot.FEET, ItemName.nano_boots.getItemStack());
         }
      }
   }

   @SubscribeEvent
   @SideOnly(Side.CLIENT)
   public void onViewRenderFogDensity(EntityViewRenderEvent.FogDensity event) {
      if (event.getState().getBlock() instanceof BlockIC2Fluid) {
         event.setCanceled(true);
         Fluid fluid = ((BlockIC2Fluid)event.getState().getBlock()).getFluid();
         GlStateManager.setFog(FogMode.EXP);
         event.setDensity((float)Util.map(Math.abs(fluid.getDensity()), 20000.0, 2.0));
      }
   }

   @SubscribeEvent
   @SideOnly(Side.CLIENT)
   public void onViewRenderFogColors(EntityViewRenderEvent.FogColors event) {
      if (event.getState().getBlock() instanceof BlockIC2Fluid) {
         int color = ((BlockIC2Fluid)event.getState().getBlock()).getColor();
         event.setRed((color >>> 16 & 0xFF) / 255.0F);
         event.setGreen((color >>> 8 & 0xFF) / 255.0F);
         event.setBlue((color & 0xFF) / 255.0F);
      }
   }

   @SubscribeEvent
   @SideOnly(Side.CLIENT)
   public void renderEnhancedOverlay(DrawBlockHighlightEvent event) {
      ItemStack inHand = StackUtil.get(event.getPlayer(), EnumHand.MAIN_HAND);
      if (event.getSubID() == 0 && event.getTarget().typeOfHit == Type.BLOCK && inHand.getItem() instanceof IEnhancedOverlayProvider) {
         World world = event.getPlayer().world;
         BlockPos blockPos = event.getTarget().getBlockPos();
         EnumFacing side = event.getTarget().sideHit;
         if (((IEnhancedOverlayProvider)inHand.getItem()).providesEnhancedOverlay(world, blockPos, side, event.getPlayer(), inHand)) {
            GL11.glPushMatrix();
            EnhancedOverlay.transformToFace(event.getPlayer(), blockPos, side, event.getPartialTicks());
            GL11.glLineWidth(2.0F);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.5F);
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
            GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            Map<EnumFacing, Runnable> additionalRenders = new EnumMap<>(EnumFacing.class);
            if (!(world.getBlockState(blockPos).getBlock() instanceof IWrenchable)) {
               EnhancedOverlay.forFace(side)
                  .drawArea(EnhancedOverlay.Segment.forRayTrace(event.getTarget()), Tessellator.getInstance().getBuffer(), 0, 0, 0);
            } else {
               EnumFacing hoveredSpin = RotationUtil.rotateByRay(event.getTarget());
               IWrenchable block = (IWrenchable)world.getBlockState(blockPos).getBlock();
               List<EnhancedOverlay.Segment> skippedSegments = new ArrayList<>();

               for (EnhancedOverlay.Segment segment : EnhancedOverlay.Segment.values()) {
                  EnumFacing spin;
                  switch (segment) {
                     case CENTRE:
                        spin = side;
                        break;
                     case TOP:
                        if (side.getAxis().isVertical()) {
                           spin = EnumFacing.NORTH;
                        } else {
                           spin = EnumFacing.UP;
                        }
                        break;
                     case BOTTOM:
                        if (side.getAxis().isVertical()) {
                           spin = EnumFacing.SOUTH;
                        } else {
                           spin = EnumFacing.DOWN;
                        }
                        break;
                     case LEFT:
                        if (side.getAxis().isVertical()) {
                           spin = EnumFacing.WEST;
                        } else {
                           spin = side.rotateY();
                        }
                        break;
                     case RIGHT:
                        if (side.getAxis().isVertical()) {
                           spin = EnumFacing.EAST;
                        } else {
                           spin = side.rotateYCCW();
                        }
                        break;
                     case TOP_LEFT:
                     case TOP_RIGHT:
                     case BOTTOM_LEFT:
                     case BOTTOM_RIGHT:
                        spin = side.getOpposite();
                        break;
                     default:
                        throw new IllegalStateException("Unexpected segment: " + segment);
                  }

                  if (block.canSetFacing(world, blockPos, spin, event.getPlayer())) {
                     int red;
                     int green;
                     int blue;
                     if (hoveredSpin == spin) {
                        blue = 0;
                        red = 0;
                        green = 255;
                     } else {
                        green = 0;
                        red = 0;
                        blue = 255;
                     }

                     EnhancedOverlay.forFace(side).drawArea(segment, Tessellator.getInstance().getBuffer(), red, green, blue);
                     if (hoveredSpin == spin) {
                        if (side.getOpposite() == spin) {
                           EnumFacing[] edges = null;
                           EnumFacing[] sides = null;
                           switch (side.getAxis()) {
                              case X:
                                 edges = new EnumFacing[]{EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH};
                                 break;
                              case Y:
                                 sides = Plane.HORIZONTAL.facings();
                                 break;
                              case Z:
                                 sides = Plane.VERTICAL.facings();
                                 edges = new EnumFacing[]{EnumFacing.WEST, EnumFacing.EAST};
                           }

                           if (edges != null) {
                              for (EnumFacing face : edges) {
                                 additionalRenders.put(
                                    face,
                                    () -> {
                                       GlStateManager.color(red / 255.0F, green / 255.0F, blue / 255.0F, 0.5F);
                                       EnhancedOverlay.drawArea(
                                          face,
                                          EnhancedOverlay.Segment.TOP_LEFT,
                                          EnhancedOverlay.Segment.TOP,
                                          EnhancedOverlay.Segment.TOP_RIGHT,
                                          EnhancedOverlay.Segment.BOTTOM_LEFT,
                                          EnhancedOverlay.Segment.BOTTOM,
                                          EnhancedOverlay.Segment.BOTTOM_RIGHT
                                       );
                                    }
                                 );
                              }
                           }

                           if (sides != null) {
                              for (EnumFacing face : sides) {
                                 additionalRenders.put(
                                    face,
                                    () -> {
                                       GlStateManager.color(red / 255.0F, green / 255.0F, blue / 255.0F, 0.5F);
                                       EnhancedOverlay.drawArea(
                                          face,
                                          EnhancedOverlay.Segment.TOP_LEFT,
                                          EnhancedOverlay.Segment.LEFT,
                                          EnhancedOverlay.Segment.BOTTOM_LEFT,
                                          EnhancedOverlay.Segment.TOP_RIGHT,
                                          EnhancedOverlay.Segment.RIGHT,
                                          EnhancedOverlay.Segment.BOTTOM_RIGHT
                                       );
                                    }
                                 );
                              }
                           }
                        } else if (segment == EnhancedOverlay.Segment.CENTRE) {
                           additionalRenders.put(spin, () -> {
                              GlStateManager.color(red / 255.0F, green / 255.0F, blue / 255.0F, 0.5F);
                              EnhancedOverlay.drawArea(spin, skippedSegments.toArray(new EnhancedOverlay.Segment[skippedSegments.size()]));
                           });
                        } else {
                           additionalRenders.put(
                              spin, () -> EnhancedOverlay.forFace(spin).drawSide(Tessellator.getInstance().getBuffer(), red, green, blue)
                           );
                        }
                     }
                  } else {
                     skippedSegments.add(segment);
                  }
               }
            }

            Runnable r = additionalRenders.remove(side);
            if (r != null) {
               r.run();
            }

            GL11.glPopMatrix();

            for (Entry<EnumFacing, Runnable> entry : additionalRenders.entrySet()) {
               GlStateManager.pushMatrix();
               EnhancedOverlay.transformToFace(event.getPlayer(), blockPos, entry.getKey(), event.getPartialTicks());
               entry.getValue().run();
               GlStateManager.popMatrix();
            }

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
         }
      }
   }

   public static ResourceLocation getIdentifier(String name) {
      return new ResourceLocation("ic2", name);
   }

   static {
      try {
         new BlockPos(1, 2, 3).add(2, 3, 4);
      } catch (Throwable t) {
         throw new Error("IC2 is incompatible with this environment, use the normal IC2 version, not the dev one.", t);
      }

      instance = null;
      network = new SideGateway<>("ic2.core.network.NetworkManager", "ic2.core.network.NetworkManagerClient");
      random = new Random();
      suddenlyHoes = false;
      seasonal = false;
      initialized = false;
      version = ProfileManager.selected.style;
      tabIC2 = new CreativeTabIC2();
   }
}
