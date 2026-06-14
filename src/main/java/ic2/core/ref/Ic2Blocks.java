package ic2.core.ref;

import ic2.core.IC2;
import ic2.core.block.generator.tileentity.TileEntityCreativeGenerator;
import ic2.core.block.generator.tileentity.TileEntityGenerator;
import ic2.core.block.generator.tileentity.TileEntityGeoGenerator;
import ic2.core.block.generator.tileentity.TileEntityKineticGenerator;
import ic2.core.block.generator.tileentity.TileEntityRTGenerator;
import ic2.core.block.generator.tileentity.TileEntitySemifluidGenerator;
import ic2.core.block.generator.tileentity.TileEntitySolarGenerator;
import ic2.core.block.generator.tileentity.TileEntityStirlingGenerator;
import ic2.core.block.generator.tileentity.TileEntityWaterGenerator;
import ic2.core.block.generator.tileentity.TileEntityWindGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntityElectricHeatGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntityFluidHeatGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntityRTHeatGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntitySolidHeatGenerator;
import ic2.core.block.inherit.Ic2FenceBlock;
import ic2.core.block.inherit.Ic2GlassBlock;
import ic2.core.block.inherit.Ic2SheetBlock;
import ic2.core.block.inherit.Ic2SignBlock;
import ic2.core.block.inherit.Ic2WallSignBlock;
import ic2.core.block.kineticgenerator.tileentity.TileEntityElectricKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityManualKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntitySteamKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityStirlingKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWaterKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWindKineticGenerator;
import ic2.core.block.machine.MiningPipeBlock;
import ic2.core.block.machine.tileentity.TileEntityAdvMiner;
import ic2.core.block.machine.tileentity.TileEntityAssemblyBench;
import ic2.core.block.machine.tileentity.TileEntityBatchCrafter;
import ic2.core.block.machine.tileentity.TileEntityBlastFurnace;
import ic2.core.block.machine.tileentity.TileEntityBlockCutter;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.block.machine.tileentity.TileEntityCentrifuge;
import ic2.core.block.machine.tileentity.TileEntityChunkloader;
import ic2.core.block.machine.tileentity.TileEntityCompressor;
import ic2.core.block.machine.tileentity.TileEntityCondenser;
import ic2.core.block.machine.tileentity.TileEntityCropHarvester;
import ic2.core.block.machine.tileentity.TileEntityCropmatron;
import ic2.core.block.machine.tileentity.TileEntityElectricFurnace;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;
import ic2.core.block.machine.tileentity.TileEntityExtractor;
import ic2.core.block.machine.tileentity.TileEntityFermenter;
import ic2.core.block.machine.tileentity.TileEntityFluidBottler;
import ic2.core.block.machine.tileentity.TileEntityFluidDistributor;
import ic2.core.block.machine.tileentity.TileEntityFluidRegulator;
import ic2.core.block.machine.tileentity.TileEntityITnt;
import ic2.core.block.machine.tileentity.TileEntityInduction;
import ic2.core.block.machine.tileentity.TileEntityIndustrialWorkbench;
import ic2.core.block.machine.tileentity.TileEntityIronFurnace;
import ic2.core.block.machine.tileentity.TileEntityItemBuffer;
import ic2.core.block.machine.tileentity.TileEntityLiquidHeatExchanger;
import ic2.core.block.machine.tileentity.TileEntityMacerator;
import ic2.core.block.machine.tileentity.TileEntityMagnetizer;
import ic2.core.block.machine.tileentity.TileEntityMassFabricator;
import ic2.core.block.machine.tileentity.TileEntityMatter;
import ic2.core.block.machine.tileentity.TileEntityMetalFormer;
import ic2.core.block.machine.tileentity.TileEntityMiner;
import ic2.core.block.machine.tileentity.TileEntityNuke;
import ic2.core.block.machine.tileentity.TileEntityOreWashing;
import ic2.core.block.machine.tileentity.TileEntityPatternStorage;
import ic2.core.block.machine.tileentity.TileEntityPump;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import ic2.core.block.machine.tileentity.TileEntityReplicator;
import ic2.core.block.machine.tileentity.TileEntityScanner;
import ic2.core.block.machine.tileentity.TileEntitySolarDistiller;
import ic2.core.block.machine.tileentity.TileEntitySolidCanner;
import ic2.core.block.machine.tileentity.TileEntitySortingMachine;
import ic2.core.block.machine.tileentity.TileEntitySteamGenerator;
import ic2.core.block.machine.tileentity.TileEntitySteamRepressurizer;
import ic2.core.block.machine.tileentity.TileEntityTank;
import ic2.core.block.machine.tileentity.TileEntityTeleporter;
import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.block.machine.tileentity.TileEntityTesla;
import ic2.core.block.machine.tileentity.TileEntityWeightedFluidDistributor;
import ic2.core.block.machine.tileentity.TileEntityWeightedItemDistributor;
import ic2.core.block.misc.FoamBlock;
import ic2.core.block.misc.RubberLogBlock;
import ic2.core.block.misc.RubberWoodBlock;
import ic2.core.block.misc.ScaffoldBlock;
import ic2.core.block.misc.WallBlock;
import ic2.core.block.personal.TileEntityEnergyOMat;
import ic2.core.block.personal.TileEntityPersonalChest;
import ic2.core.block.personal.TileEntityTradeOMat;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.block.reactor.tileentity.TileEntityRCI_LZH;
import ic2.core.block.reactor.tileentity.TileEntityRCI_RSH;
import ic2.core.block.reactor.tileentity.TileEntityReactorAccessHatch;
import ic2.core.block.reactor.tileentity.TileEntityReactorChamberElectric;
import ic2.core.block.reactor.tileentity.TileEntityReactorFluidPort;
import ic2.core.block.reactor.tileentity.TileEntityReactorRedstonePort;
import ic2.core.block.storage.box.TileEntityBronzeStorageBox;
import ic2.core.block.storage.box.TileEntityIridiumStorageBox;
import ic2.core.block.storage.box.TileEntityIronStorageBox;
import ic2.core.block.storage.box.TileEntitySteelStorageBox;
import ic2.core.block.storage.box.TileEntityWoodenStorageBox;
import ic2.core.block.storage.tank.TileEntityBronzeTank;
import ic2.core.block.storage.tank.TileEntityIridiumTank;
import ic2.core.block.storage.tank.TileEntityIronTank;
import ic2.core.block.storage.tank.TileEntitySteelTank;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.block.tileentity.TileEntityWall;
import ic2.core.block.wiring.CableBlock;
import ic2.core.block.wiring.CableType;
import ic2.core.block.wiring.DetectorCableBlock;
import ic2.core.block.wiring.DetectorFoamCableBlock;
import ic2.core.block.wiring.FoamCableBlock;
import ic2.core.block.wiring.SplitterCableBlock;
import ic2.core.block.wiring.SplitterFoamCableBlock;
import ic2.core.block.wiring.tileentity.TileEntityChargepadBatBox;
import ic2.core.block.wiring.tileentity.TileEntityLuminator;
import ic2.core.block.wiring.tileentity.TileEntityChargepadCESU;
import ic2.core.block.wiring.tileentity.TileEntityChargepadMFE;
import ic2.core.block.wiring.tileentity.TileEntityChargepadMFSU;
import ic2.core.block.wiring.tileentity.TileEntityElectricBatBox;
import ic2.core.block.wiring.tileentity.TileEntityElectricCESU;
import ic2.core.block.wiring.tileentity.TileEntityElectricMFE;
import ic2.core.block.wiring.tileentity.TileEntityElectricMFSU;
import ic2.core.block.wiring.tileentity.TileEntityTransformerEV;
import ic2.core.block.wiring.tileentity.TileEntityTransformerHV;
import ic2.core.block.wiring.tileentity.TileEntityTransformerLV;
import ic2.core.block.wiring.tileentity.TileEntityTransformerMV;
import ic2.core.crop.CropStickBlock;
import ic2.core.crop.Ic2CropType;
import ic2.core.crop.TileEntityCrop;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;

import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.PressurePlateBlock.Sensitivity;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;


import net.minecraft.world.level.material.MapColor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public final class Ic2Blocks
{
	public static final Block LEAD_ORE = register("lead_ore", new Block(Properties.of().strength(2.0F, 4.0F).requiresCorrectToolForDrops()));
	public static final Block TIN_ORE = register("tin_ore", new Block(Properties.of().strength(3.0F, 5.0F).requiresCorrectToolForDrops()));
	public static final Block URANIUM_ORE = register("uranium_ore", new Block(Properties.of().strength(4.0F, 6.0F).requiresCorrectToolForDrops()));
	public static final Block DEEPSLATE_LEAD_ORE = register("deepslate_lead_ore", new Block(Properties.of().mapColor(MapColor.DEEPSLATE).strength(3.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));
	public static final Block DEEPSLATE_TIN_ORE = register("deepslate_tin_ore", new Block(Properties.of().mapColor(MapColor.DEEPSLATE).strength(4.0F, 7.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));
	public static final Block DEEPSLATE_URANIUM_ORE = register("deepslate_uranium_ore", new Block(Properties.of().mapColor(MapColor.DEEPSLATE).strength(5.0F, 8.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));
	public static final Block RAW_LEAD_BLOCK = register("raw_lead_block", new Block(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(3.0F, 11.0F).requiresCorrectToolForDrops()));
	public static final Block RAW_TIN_BLOCK = register("raw_tin_block", new Block(Properties.of().mapColor(MapColor.SNOW).strength(4.0F, 12.0F).requiresCorrectToolForDrops()));
	public static final Block RAW_URANIUM_BLOCK = register("raw_uranium_block", new Block(Properties.of().mapColor(MapColor.COLOR_GREEN).strength(5.0F, 13.0F).requiresCorrectToolForDrops()));
	public static final Block BRONZE_BLOCK = register("bronze_block", new Block(Properties.of().strength(5.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final Block LEAD_BLOCK = register("lead_block", new Block(Properties.of().strength(4.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final Block STEEL_BLOCK = register("steel_block", new Block(Properties.of().strength(8.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final Block TIN_BLOCK = register("tin_block", new Block(Properties.of().strength(4.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final Block URANIUM_BLOCK = register("uranium_block", new Block(Properties.of().strength(6.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final Block REINFORCED_STONE = register("reinforced_stone", new Block(Properties.of().strength(80.0F, 180.0F).requiresCorrectToolForDrops()));
	public static final Block MACHINE = register("machine", new Block(Properties.of().strength(5.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final Block ADVANCED_MACHINE = register("advanced_machine", new Block(Properties.of().strength(8.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final Block REACTOR_VESSEL = register("reactor_vessel", new Block(Properties.of().strength(40.0F, 90.0F).requiresCorrectToolForDrops()));
	public static final Block SILVER_BLOCK = register("silver_block", new Block(Properties.of().strength(4.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final LeavesBlock RUBBER_LEAVES = register("rubber_leaves", new LeavesBlock(Properties.of().strength(0.2F).randomTicks().sound(SoundType.GRASS).noOcclusion().isValidSpawn(Ic2Blocks::canSpawnOnLeaves).isSuffocating(Ic2Blocks::never).isViewBlocking(Ic2Blocks::never)));
	public static final RubberLogBlock RUBBER_LOG = register("rubber_log", new RubberLogBlock(Properties.of().mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Axis.Y ? MapColor.PODZOL : MapColor.COLOR_BROWN).randomTicks().strength(1.0F).sound(SoundType.WOOD)));
	public static final RotatedPillarBlock STRIPPED_RUBBER_LOG = register("stripped_rubber_log", new RotatedPillarBlock(Properties.of().mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Axis.Y ? MapColor.PODZOL : MapColor.COLOR_BROWN).strength(1.0F).sound(SoundType.WOOD)));
	public static final RubberWoodBlock RUBBER_WOOD = register("rubber_wood", new RubberWoodBlock(Properties.of().mapColor(MapColor.COLOR_BROWN).strength(1.0F).sound(SoundType.WOOD)));
	public static final Block STRIPPED_RUBBER_WOOD = register("stripped_rubber_wood", new Block(Properties.of().mapColor(MapColor.PODZOL).strength(1.0F).sound(SoundType.WOOD)));
	public static final Block RUBBER_SAPLING = register("rubber_sapling", new SaplingBlock(new AbstractTreeGrower()
	{
		protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(@NotNull RandomSource random, boolean bees)
		{
			return ResourceKey.create(Registries.CONFIGURED_FEATURE, IC2.getIdentifier("rubber_tree"));
		}
	}, Properties.of().noCollission().randomTicks().instabreak().sound(SoundType.GRASS))
	{
	});
	public static final Block RUBBER_PLANKS = register("rubber_planks", new Block(Properties.of().mapColor(MapColor.PODZOL).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final Block RUBBER_BUTTON = register("rubber_button", new ButtonBlock(Properties.of().noCollission().strength(0.5F).sound(SoundType.WOOD), BlockSetType.OAK, 30, false)
	{
	});
	public static final Block RUBBER_DOOR = register("rubber_door", new DoorBlock(Properties.of().mapColor(RUBBER_PLANKS.defaultMapColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), BlockSetType.OAK)
	{
	});
	public static final Block RUBBER_FENCE = register("rubber_fence", new FenceBlock(Properties.of().mapColor(RUBBER_PLANKS.defaultMapColor()).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final Block RUBBER_FENCE_GATE = register("rubber_fence_gate", new FenceGateBlock(Properties.of().mapColor(RUBBER_PLANKS.defaultMapColor()).strength(2.0F, 3.0F).sound(SoundType.WOOD), WoodType.OAK));
	public static final Block RUBBER_PRESSURE_PLATE = register("rubber_pressure_plate", new PressurePlateBlock(Sensitivity.EVERYTHING, Properties.of().mapColor(RUBBER_PLANKS.defaultMapColor()).noCollission().strength(0.5F).sound(SoundType.WOOD), BlockSetType.OAK)
	{
	});
	public static final Block RUBBER_SIGN = register("rubber_sign", new Ic2SignBlock(Properties.of().mapColor(RUBBER_LOG.defaultMapColor()).noCollission().strength(1.0F).sound(SoundType.WOOD), Ic2SignType.RUBBER));
	public static final Block RUBBER_SLAB = register("rubber_slab", new SlabBlock(Properties.of().mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final Block RUBBER_STAIRS = register("rubber_stairs", new StairBlock(RUBBER_PLANKS::defaultBlockState, Properties.of().mapColor(MapColor.PODZOL).strength(2.0F, 3.0F).sound(SoundType.WOOD))
	{
	});
	public static final Block RUBBER_TRAPDOOR = register("rubber_trapdoor", new TrapDoorBlock(Properties.of().mapColor(MapColor.PODZOL).strength(3.0F).sound(SoundType.WOOD).noOcclusion().isValidSpawn(Ic2Blocks::never), BlockSetType.OAK)
	{
	});
	public static final Block RUBBER_WALL_SIGN = register("rubber_wall_sign", new Ic2WallSignBlock(Properties.of().mapColor(RUBBER_LOG.defaultMapColor()).noCollission().strength(1.0F).sound(SoundType.WOOD).lootFrom(ForgeRegistries.BLOCKS.getDelegateOrThrow(RUBBER_SIGN)), Ic2SignType.RUBBER));
	public static final Block WOODEN_SCAFFOLD = register("wooden_scaffold", new ScaffoldBlock(Properties.of().noOcclusion().strength(0.5F, 0.12F).randomTicks().sound(SoundType.WOOD), 2));
	public static final Block REINFORCED_WOODEN_SCAFFOLD = register("reinforced_wooden_scaffold", new ScaffoldBlock(Properties.of().noOcclusion().strength(0.6F, 0.24F).randomTicks().sound(SoundType.WOOD), 5));
	public static final Block IRON_SCAFFOLD = register("iron_scaffold", new ScaffoldBlock(Properties.of().noOcclusion().strength(0.8F, 6.0F).randomTicks().sound(SoundType.METAL), 5));
	public static final Block REINFORCED_IRON_SCAFFOLD = register("reinforced_iron_scaffold", new ScaffoldBlock(Properties.of().noOcclusion().strength(1.0F, 8.0F).randomTicks().sound(SoundType.METAL), 12));
	public static final Block IRON_FENCE = register("iron_fence", new Ic2FenceBlock(Properties.of().strength(5.0F, 10.0F), true));
	public static final Block RESIN_SHEET = register("resin_sheet", new Ic2SheetBlock(Properties.of().strength(1.6F, 0.5F)));
	public static final Block RUBBER_SHEET = register("rubber_sheet", new Ic2SheetBlock(Properties.of().strength(0.8F, 2.0F)));
	public static final Block WOOL_SHEET = register("wool_sheet", new Ic2SheetBlock(Properties.of().strength(0.8F, 0.8F)));
	public static final Block REINFORCED_GLASS = register("reinforced_glass", new Ic2GlassBlock(Properties.of().noOcclusion().strength(5.0F, 180.0F).sound(SoundType.GLASS).isValidSpawn((state, world, pos, type) -> false)));
	public static final Block FOAM = register("foam", new FoamBlock(Properties.of().noOcclusion().strength(0.01F, 10.0F).randomTicks().sound(SoundType.WOOL)));
	private static final Properties wallSettings = Properties.of().strength(3.0F, 30.0F).requiresCorrectToolForDrops().sound(SoundType.STONE);
	public static final Block WHITE_WALL = register("white_wall", new WallBlock(wallSettings, DyeColor.WHITE));
	public static final Block ORANGE_WALL = register("orange_wall", new WallBlock(wallSettings, DyeColor.ORANGE));
	public static final Block MAGENTA_WALL = register("magenta_wall", new WallBlock(wallSettings, DyeColor.MAGENTA));
	public static final Block LIGHT_BLUE_WALL = register("light_blue_wall", new WallBlock(wallSettings, DyeColor.LIGHT_BLUE));
	public static final Block YELLOW_WALL = register("yellow_wall", new WallBlock(wallSettings, DyeColor.YELLOW));
	public static final Block LIME_WALL = register("lime_wall", new WallBlock(wallSettings, DyeColor.LIME));
	public static final Block PINK_WALL = register("pink_wall", new WallBlock(wallSettings, DyeColor.PINK));
	public static final Block GRAY_WALL = register("gray_wall", new WallBlock(wallSettings, DyeColor.GRAY));
	public static final Block LIGHT_GRAY_WALL = register("light_gray_wall", new WallBlock(wallSettings, DyeColor.LIGHT_GRAY));
	public static final Block CYAN_WALL = register("cyan_wall", new WallBlock(wallSettings, DyeColor.CYAN));
	public static final Block PURPLE_WALL = register("purple_wall", new WallBlock(wallSettings, DyeColor.PURPLE));
	public static final Block BLUE_WALL = register("blue_wall", new WallBlock(wallSettings, DyeColor.BLUE));
	public static final Block BROWN_WALL = register("brown_wall", new WallBlock(wallSettings, DyeColor.BROWN));
	public static final Block GREEN_WALL = register("green_wall", new WallBlock(wallSettings, DyeColor.GREEN));
	public static final Block RED_WALL = register("red_wall", new WallBlock(wallSettings, DyeColor.RED));
	public static final Block BLACK_WALL = register("black_wall", new WallBlock(wallSettings, DyeColor.BLACK));
	public static final Block OBSCURED_WALL = register("obscured_wall", Ic2TileEntityBlock.create(wallSettings, TileEntityWall.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final Block MINING_PIPE = register("mining_pipe", new MiningPipeBlock(Properties.of().strength(6.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final Block MINING_PIPE_TIP = register("mining_pipe_tip", new Block(Properties.of().strength(6.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL)));
	public static final Block REINFORCED_DOOR = register("reinforced_door", new DoorBlock(Properties.of().strength(50.0F, 150.0F).sound(SoundType.METAL), BlockSetType.IRON)
	{
	});
	public static final Block ITNT = register("itnt", Ic2TileEntityBlock.create(Properties.of().strength(0.0F, 0.0F).sound(SoundType.GRASS), TileEntityITnt.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final Block NUKE = register("nuke", Ic2TileEntityBlock.create(Properties.of().strength(0.0F, 0.0F).sound(SoundType.GRASS), TileEntityNuke.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final Block GENERATOR = register("generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final Block GEO_GENERATOR = register("geo_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityGeoGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Generator, Util.horizontalFacings, true));
	public static final Block KINETIC_GENERATOR = register("kinetic_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityKineticGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Generator, Util.allFacings, true));
	public static final Block RT_GENERATOR = register("rt_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityRTGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Generator, Util.noFacings, false));
	public static final Block SEMIFLUID_GENERATOR = register("semifluid_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySemifluidGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Generator, Util.horizontalFacings, true));
	public static final Block SOLAR_GENERATOR = register("solar_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySolarGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Generator, Util.horizontalFacings, true));
	public static final Block STIRLING_GENERATOR = register("stirling_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityStirlingGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Generator, Util.allFacings, true));
	public static final Block WATER_GENERATOR = register("water_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityWaterGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Generator, Util.horizontalFacings, true));
	public static final Block WIND_GENERATOR = register("wind_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityWindGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Generator, Util.horizontalFacings, true));
	public static final Block ELECTRIC_HEAT_GENERATOR = register("electric_heat_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityElectricHeatGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final Block FLUID_HEAT_GENERATOR = register("fluid_heat_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityFluidHeatGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final Block RT_HEAT_GENERATOR = register("rt_heat_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityRTHeatGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final Block SOLID_HEAT_GENERATOR = register("solid_heat_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySolidHeatGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final Block ELECTRIC_KINETIC_GENERATOR = register("electric_kinetic_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityElectricKineticGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final Block MANUAL_KINETIC_GENERATOR = register("manual_kinetic_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityManualKineticGenerator.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final Block STEAM_KINETIC_GENERATOR = register("steam_kinetic_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySteamKineticGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final Block STIRLING_KINETIC_GENERATOR = register("stirling_kinetic_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityStirlingKineticGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final Block WATER_KINETIC_GENERATOR = register("water_kinetic_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityWaterKineticGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final Block WIND_KINETIC_GENERATOR = register("wind_kinetic_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityWindKineticGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final Block NUCLEAR_REACTOR = register("nuclear_reactor", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityNuclearReactorElectric.class, true, Ic2TileEntityBlock.DefaultDrop.Generator, Util.horizontalFacings, true));
	public static final Block REACTOR_ACCESS_HATCH = register("reactor_access_hatch", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(40.0F, 90.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityReactorAccessHatch.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.onlyNorth, false));
	public static final Block REACTOR_CHAMBER = register("reactor_chamber", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityReactorChamberElectric.class, false, Ic2TileEntityBlock.DefaultDrop.Machine, Util.onlyNorth, true));
	public static final Block REACTOR_FLUID_PORT = register("reactor_fluid_port", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(40.0F, 90.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityReactorFluidPort.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.onlyNorth, false));
	public static final Block REACTOR_REDSTONE_PORT = register("reactor_redstone_port", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(40.0F, 90.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityReactorRedstonePort.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.onlyNorth, false));
	public static final Block CONDENSER = register("condenser", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityCondenser.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final Block FLUID_BOTTLER = register("fluid_bottler", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityFluidBottler.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final Block FLUID_DISTRIBUTOR = register("fluid_distributor", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityFluidDistributor.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final Block FLUID_REGULATOR = register("fluid_regulator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityFluidRegulator.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final Block LIQUID_HEAT_EXCHANGER = register("liquid_heat_exchanger", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityLiquidHeatExchanger.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final Block PUMP = register("pump", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityPump.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final Block SOLAR_DISTILLER = register("solar_distiller", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySolarDistiller.class, false, Ic2TileEntityBlock.DefaultDrop.Machine, Util.noFacings, false));
	public static final Block STEAM_GENERATOR = register("steam_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySteamGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final Block ITEM_BUFFER = register("item_buffer", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityItemBuffer.class, false, Ic2TileEntityBlock.DefaultDrop.Machine, Util.noFacings, false));
	public static final Block MAGNETIZER = register("magnetizer", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityMagnetizer.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final Block SORTING_MACHINE = register("sorting_machine", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySortingMachine.class, false, Ic2TileEntityBlock.DefaultDrop.Machine, Util.onlyNorth, false));
	public static final Block TELEPORTER = register("teleporter", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTeleporter.class, true, Ic2TileEntityBlock.DefaultDrop.AdvMachine, Util.noFacings, false));
	public static final Block TERRAFORMER = register("terraformer", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTerra.class, true, Ic2TileEntityBlock.DefaultDrop.AdvMachine, Util.noFacings, false));
	public static final Block TESLA_COIL = register("tesla_coil", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTesla.class, false, Ic2TileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final Block LUMINATOR_FLAT = register("luminator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(5.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion(), TileEntityLuminator.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.allFacings, false));
	public static final Block CANNER = register("canner", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityCanner.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final Ic2TileEntityBlock COMPRESSOR = register("compressor", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityCompressor.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final Block ELECTRIC_FURNACE = register("electric_furnace", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityElectricFurnace.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final Ic2TileEntityBlock EXTRACTOR = register("extractor", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityExtractor.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final Block IRON_FURNACE = register("iron_furnace", Ic2TileEntityBlock.create(Properties.of().strength(5.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityIronFurnace.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, false));
	public static final Ic2TileEntityBlock MACERATOR = register("macerator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityMacerator.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final Block RECYCLER = register("recycler", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityRecycler.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final Block SOLID_CANNER = register("solid_canner", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySolidCanner.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final Ic2TileEntityBlock BLAST_FURNACE = register("blast_furnace", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityBlastFurnace.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final Ic2TileEntityBlock BLOCK_CUTTER = register("block_cutter", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityBlockCutter.class, true, Ic2TileEntityBlock.DefaultDrop.AdvMachine, Util.horizontalFacings, true));
	public static final Ic2TileEntityBlock CENTRIFUGE = register("centrifuge", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityCentrifuge.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final Block FERMENTER = register("fermenter", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityFermenter.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final Block INDUCTION_FURNACE = register("induction_furnace", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityInduction.class, true, Ic2TileEntityBlock.DefaultDrop.AdvMachine, Util.horizontalFacings, true));
	public static final Ic2TileEntityBlock METAL_FORMER = register("metal_former", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityMetalFormer.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final Ic2TileEntityBlock ORE_WASHING_PLANT = register("ore_washing_plant", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityOreWashing.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final Block ADVANCED_MINER = register("advanced_miner", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityAdvMiner.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final Block CROP_HARVESTER = register("crop_harvester", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityCropHarvester.class, false, Ic2TileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final Block CROPMATRON = register("cropmatron", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityCropmatron.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final Block MINER = register("miner", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityMiner.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final Block MASS_FABRICATOR = register("mass_fabricator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityMassFabricator.class, true, Ic2TileEntityBlock.DefaultDrop.AdvMachine, Util.horizontalFacings, true));
	public static final Block UU_ASSEMBLY_BENCH = register("uu_assembly_bench", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityAssemblyBench.class, true, Ic2TileEntityBlock.DefaultDrop.AdvMachine, Util.horizontalFacings, true));
	public static final Block MATTER_GENERATOR = register("matter_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityMatter.class, true, Ic2TileEntityBlock.DefaultDrop.AdvMachine, Util.horizontalFacings, true));
	public static final Block PATTERN_STORAGE = register("pattern_storage", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityPatternStorage.class, false, Ic2TileEntityBlock.DefaultDrop.AdvMachine, Util.horizontalFacings, true));
	public static final Block REPLICATOR = register("replicator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityReplicator.class, true, Ic2TileEntityBlock.DefaultDrop.AdvMachine, Util.horizontalFacings, true));
	public static final Block UU_SCANNER = register("uu_scanner", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityScanner.class, true, Ic2TileEntityBlock.DefaultDrop.AdvMachine, Util.horizontalFacings, true));
	public static final Block ENERGY_O_MAT = register("energy_o_mat", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(-1.0F, 3600000.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityEnergyOMat.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.allFacings, false));
	public static final Block PERSONAL_CHEST = register("personal_chest", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(-1.0F, 3600000.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityPersonalChest.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, false));
	public static final Block TRADE_O_MAT = register("trade_o_mat", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(-1.0F, 3600000.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTradeOMat.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, false));
	private static final Properties cableSettings = Properties.of().strength(0.5F, 5.0F).sound(SoundType.METAL);
	private static final Properties insulatedCableSettings = Properties.of().strength(0.5F, 5.0F).sound(SoundType.WOOL);
	public static final FoamCableBlock COPPER_FOAM_CABLE = register("copper_foam_cable", FoamCableBlock.create(cableSettings, CableType.copper, 0));
	public static final Block COPPER_CABLE = register("copper_cable", CableBlock.create(cableSettings, CableType.copper, 0, COPPER_FOAM_CABLE));
	public static final FoamCableBlock INSULATED_COPPER_FOAM_CABLE = register("insulated_copper_foam_cable", FoamCableBlock.create(insulatedCableSettings, CableType.copper, 1));
	public static final Block INSULATED_COPPER_CABLE = register("insulated_copper_cable", CableBlock.create(insulatedCableSettings, CableType.copper, 1, INSULATED_COPPER_FOAM_CABLE));
	public static final FoamCableBlock GLASS_FIBRE_FOAM_CABLE = register("glass_fibre_foam_cable", FoamCableBlock.create(Properties.of().strength(0.5F, 5.0F).sound(SoundType.GLASS), CableType.glass, 0));
	public static final Block GLASS_FIBRE_CABLE = register("glass_fibre_cable", CableBlock.create(Properties.of().strength(0.5F, 5.0F).sound(SoundType.GLASS), CableType.glass, 0, GLASS_FIBRE_FOAM_CABLE));
	public static final FoamCableBlock GOLD_FOAM_CABLE = register("gold_foam_cable", FoamCableBlock.create(cableSettings, CableType.gold, 0));
	public static final Block GOLD_CABLE = register("gold_cable", CableBlock.create(cableSettings, CableType.gold, 0, GOLD_FOAM_CABLE));
	public static final FoamCableBlock INSULATED_GOLD_FOAM_CABLE = register("insulated_gold_foam_cable", FoamCableBlock.create(insulatedCableSettings, CableType.gold, 1));
	public static final Block INSULATED_GOLD_CABLE = register("insulated_gold_cable", CableBlock.create(insulatedCableSettings, CableType.gold, 1, INSULATED_GOLD_FOAM_CABLE));
	public static final FoamCableBlock DOUBLE_INSULATED_GOLD_FOAM_CABLE = register("double_insulated_gold_foam_cable", FoamCableBlock.create(insulatedCableSettings, CableType.gold, 2));
	public static final Block DOUBLE_INSULATED_GOLD_CABLE = register("double_insulated_gold_cable", CableBlock.create(insulatedCableSettings, CableType.gold, 2, DOUBLE_INSULATED_GOLD_FOAM_CABLE));
	public static final FoamCableBlock IRON_FOAM_CABLE = register("iron_foam_cable", FoamCableBlock.create(cableSettings, CableType.iron, 0));
	public static final Block IRON_CABLE = register("iron_cable", CableBlock.create(cableSettings, CableType.iron, 0, IRON_FOAM_CABLE));
	public static final FoamCableBlock INSULATED_IRON_FOAM_CABLE = register("insulated_iron_foam_cable", FoamCableBlock.create(insulatedCableSettings, CableType.iron, 1));
	public static final Block INSULATED_IRON_CABLE = register("insulated_iron_cable", CableBlock.create(insulatedCableSettings, CableType.iron, 1, INSULATED_IRON_FOAM_CABLE));
	public static final FoamCableBlock DOUBLE_INSULATED_IRON_FOAM_CABLE = register("double_insulated_iron_foam_cable", FoamCableBlock.create(insulatedCableSettings, CableType.iron, 2));
	public static final Block DOUBLE_INSULATED_IRON_CABLE = register("double_insulated_iron_cable", CableBlock.create(insulatedCableSettings, CableType.iron, 2, DOUBLE_INSULATED_IRON_FOAM_CABLE));
	public static final FoamCableBlock TRIPLE_INSULATED_IRON_FOAM_CABLE = register("triple_insulated_iron_foam_cable", FoamCableBlock.create(insulatedCableSettings, CableType.iron, 3));
	public static final Block TRIPLE_INSULATED_IRON_CABLE = register("triple_insulated_iron_cable", CableBlock.create(insulatedCableSettings, CableType.iron, 3, TRIPLE_INSULATED_IRON_FOAM_CABLE));
	public static final FoamCableBlock TIN_FOAM_CABLE = register("tin_foam_cable", FoamCableBlock.create(cableSettings, CableType.tin, 0));
	public static final Block TIN_CABLE = register("tin_cable", CableBlock.create(cableSettings, CableType.tin, 0, TIN_FOAM_CABLE));
	public static final FoamCableBlock INSULATED_TIN_FOAM_CABLE = register("insulated_tin_foam_cable", FoamCableBlock.create(insulatedCableSettings, CableType.tin, 1));
	public static final Block INSULATED_TIN_CABLE = register("insulated_tin_cable", CableBlock.create(insulatedCableSettings, CableType.tin, 1, INSULATED_TIN_FOAM_CABLE));
	public static final DetectorFoamCableBlock DETECTOR_FOAM_CABLE = register("detector_foam_cable", DetectorFoamCableBlock.create(cableSettings));
	public static final Block DETECTOR_CABLE = register("detector_cable", DetectorCableBlock.create(cableSettings, DETECTOR_FOAM_CABLE));
	public static final SplitterFoamCableBlock SPLITTER_FOAM_CABLE = register("splitter_foam_cable", SplitterFoamCableBlock.create(cableSettings));
	public static final Block SPLITTER_CABLE = register("splitter_cable", SplitterCableBlock.create(cableSettings, SPLITTER_FOAM_CABLE));
	public static final Block BATBOX_CHARGEPAD = register("batbox_chargepad", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion(), TileEntityChargepadBatBox.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.downSideFacings, true));
	public static final Block CESU_CHARGEPAD = register("cesu_chargepad", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion(), TileEntityChargepadCESU.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.downSideFacings, true));
	public static final Block MFE_CHARGEPAD = register("mfe_chargepad", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion(), TileEntityChargepadMFE.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.downSideFacings, true));
	public static final Block MFSU_CHARGEPAD = register("mfsu_chargepad", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion(), TileEntityChargepadMFSU.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.downSideFacings, true));
	public static final Block BATBOX = register("batbox", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityElectricBatBox.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final Block CESU = register("cesu", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityElectricCESU.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final Block MFE = register("mfe", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityElectricMFE.class, false, Ic2TileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final Block MFSU = register("mfsu", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityElectricMFSU.class, false, Ic2TileEntityBlock.DefaultDrop.AdvMachine, Util.allFacings, true));
	public static final Block ELECTROLYZER = register("electrolyzer", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityElectrolyzer.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.horizontalFacings, true));
	public static final Block LV_TRANSFORMER = register("lv_transformer", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTransformerLV.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.allFacings, true));
	public static final Block MV_TRANSFORMER = register("mv_transformer", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTransformerMV.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final Block HV_TRANSFORMER = register("hv_transformer", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTransformerHV.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final Block EV_TRANSFORMER = register("ev_transformer", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTransformerEV.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final Block TANK = register("tank", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityTank.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final Block CHUNK_LOADER = register("chunk_loader", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityChunkloader.class, true, Ic2TileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final Block CREATIVE_GENERATOR = register("creative_generator", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(-1.0F, Float.POSITIVE_INFINITY).sound(SoundType.METAL), TileEntityCreativeGenerator.class, true, Ic2TileEntityBlock.DefaultDrop.None, Util.noFacings, false));
	public static final Block STEAM_REPRESSURIZER = register("steam_repressurizer", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntitySteamRepressurizer.class, true, Ic2TileEntityBlock.DefaultDrop.Machine, Util.noFacings, true));
	public static final Block WEIGHTED_FLUID_DISTRIBUTOR = register("weighted_fluid_distributor", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityWeightedFluidDistributor.class, false, Ic2TileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final Block WEIGHTED_ITEM_DISTRIBUTOR = register("weighted_item_distributor", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityWeightedItemDistributor.class, false, Ic2TileEntityBlock.DefaultDrop.Machine, Util.allFacings, true));
	public static final Block RCI_RSH = register("rci_rsh", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityRCI_RSH.class, true, Ic2TileEntityBlock.DefaultDrop.AdvMachine, Util.allFacings, true));
	public static final Block RCI_LZH = register("rci_lzh", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityRCI_LZH.class, true, Ic2TileEntityBlock.DefaultDrop.AdvMachine, Util.allFacings, true));
	private static final Properties cropSettings = Properties.of().strength(0.8F, 0.2F).sound(SoundType.CROP).noCollission();
	public static final Block CROP_STICK = register("crop_stick", new CropStickBlock(cropSettings));
	public static final Block WEED_CROP = register("weed_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.weed));
	public static final Block WHEAT_CROP = register("wheat_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.wheat));
	public static final Block CARROTS_CROP = register("carrots_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.carrots));
	public static final Block POTATO_CROP = register("potato_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.potato));
	public static final Block BEETROOTS_CROP = register("beetroots_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.beetroots));
	public static final Block PUMPKIN_CROP = register("pumpkin_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.pumpkin));
	public static final Block MELON_CROP = register("melon_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.melon));
	public static final Block DANDELION_CROP = register("dandelion_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.dandelion));
	public static final Block POPPY_CROP = register("poppy_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.poppy));
	public static final Block BLACKTHORN_CROP = register("blackthorn_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.blackthorn));
	public static final Block TULIP_CROP = register("tulip_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.tulip));
	public static final Block CYAZINT_CROP = register("cyazint_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.cyazint));
	public static final Block VENOMILIA_CROP = register("venomilia_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.venomilia));
	public static final Block REED_CROP = register("reed_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.reed));
	public static final Block STICKY_REED_CROP = register("sticky_reed_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.stickyReed));
	public static final Block COCOA_CROP = register("cocoa_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.cocoa));
	public static final Block FLAX_CROP = register("flax_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.flax));
	public static final Block RED_MUSHROOM_CROP = register("red_mushroom_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.redMushroom));
	public static final Block BROWN_MUSHROOM_CROP = register("brown_mushroom_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.brownMushroom));
	public static final Block NETHER_WART_CROP = register("nether_wart_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.netherWart));
	public static final Block TERRA_WART_CROP = register("terra_wart_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.terraWart));
	public static final Block OAK_SAPLING_CROP = register("oak_sapling_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.oakSapling));
	public static final Block SPRUCE_SAPLING_CROP = register("spruce_sapling_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.spruceSapling));
	public static final Block BIRCH_SAPLING_CROP = register("birch_sapling_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.birchSapling));
	public static final Block JUNGLE_SAPLING_CROP = register("jungle_sapling_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.jungleSapling));
	public static final Block ACACIA_SAPLING_CROP = register("acacia_sapling_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.acaciaSapling));
	public static final Block DARK_OAK_SAPLING_CROP = register("dark_oak_sapling_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.darkOakSapling));
	public static final Block FERRU_CROP = register("ferru_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.ferru));
	public static final Block CYPRIUM_CROP = register("cyprium_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.cyprium));
	public static final Block STAGNIUM_CROP = register("stagnium_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.stagnium));
	public static final Block PLUMBISCUS_CROP = register("plumbiscus_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.plumbiscus));
	public static final Block AURELIA_CROP = register("aurelia_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.aurelia));
	public static final Block SHINING_CROP = register("shining_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.shining));
	public static final Block RED_WHEAT_CROP = register("red_wheat_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.redWheat));
	public static final Block COFFEE_CROP = register("coffee_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.coffee));
	public static final Block HOPS_CROP = register("hops_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.hops));
	public static final Block EATING_PLANT_CROP = register("eating_plant_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.eatingPlant));
	public static final Block BLAZEREED_CROP = register("blazereed_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.blazereed));
	public static final Block BOBS_YER_UNCLE_RANKS_BERRIES_CROP = register("bobs_yer_uncle_ranks_berries_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.bobsYerUncleRanksBerries));
	public static final Block CORIUM_CROP = register("corium_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.corium));
	public static final Block CORPSE_PLANT_CROP = register("corpse_plant_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.corpse_plant));
	public static final Block CREEPER_WEED_CROP = register("creeper_weed_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.creeper_weed));
	public static final Block DIAREED_CROP = register("diareed_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.diareed));
	public static final Block EGG_PLANT_CROP = register("egg_plant_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.egg_plant));
	public static final Block ENDER_BLOSSOM_CROP = register("ender_blossom_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.ender_blossom));
	public static final Block MEAT_ROSE_CROP = register("meat_rose_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.meat_rose));
	public static final Block MILK_WART_CROP = register("milk_wart_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.milk_wart));
	public static final Block OIL_BERRIES_CROP = register("oil_berries_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.oil_berries));
	public static final Block SLIME_PLANT_CROP = register("slime_plant_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.slime_plant));
	public static final Block SPIDERNIP_CROP = register("spidernip_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.spidernip));
	public static final Block TEARSTALKS_CROP = register("tearstalks_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.tearstalks));
	public static final Block WITHEREED_CROP = register("withereed_crop", Ic2TileEntityBlock.create(cropSettings, TileEntityCrop.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false, Ic2CropType.withereed));
	public static final Block INDUSTRIAL_WORKBENCH = register("industrial_workbench", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityIndustrialWorkbench.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.horizontalFacings, true));
	public static final Block BATCH_CRAFTER = register("batch_crafter", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 10.0F).requiresCorrectToolForDrops().sound(SoundType.METAL), TileEntityBatchCrafter.class, true, Ic2TileEntityBlock.DefaultDrop.AdvMachine, Util.horizontalFacings, true));
	public static final Block WOODEN_STORAGE_BOX = register("wooden_storage_box", Ic2TileEntityBlock.create(Properties.of().strength(1.0F, 10.0F).sound(SoundType.WOOD), TileEntityWoodenStorageBox.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final Block IRON_STORAGE_BOX = register("iron_storage_box", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(1.0F, 15.0F).sound(SoundType.METAL), TileEntityIronStorageBox.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final Block BRONZE_STORAGE_BOX = register("bronze_storage_box", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(1.0F, 15.0F).sound(SoundType.METAL), TileEntityBronzeStorageBox.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final Block STEEL_STORAGE_BOX = register("steel_storage_box", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 20.0F).sound(SoundType.METAL), TileEntitySteelStorageBox.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final Block IRIDIUM_STORAGE_BOX = register("iridium_storage_box", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(3.0F, 100.0F).sound(SoundType.METAL), TileEntityIridiumStorageBox.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final Block BRONZE_TANK = register("bronze_tank", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(3.0F, 15.0F).sound(SoundType.METAL), TileEntityBronzeTank.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final Block IRON_TANK = register("iron_tank", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(3.0F, 15.0F).sound(SoundType.METAL), TileEntityIronTank.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final Block STEEL_TANK = register("steel_tank", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(4.0F, 20.0F).sound(SoundType.METAL), TileEntitySteelTank.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false));
	public static final Block IRIDIUM_TANK = register("iridium_tank", Ic2TileEntityBlock.create(Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(5.0F, 100.0F).sound(SoundType.METAL), TileEntityIridiumTank.class, false, Ic2TileEntityBlock.DefaultDrop.Self, Util.noFacings, false));

	public static void init()
	{
	}

	private static <T extends Block> T register(String name, T block)
	{
		IC2.envProxy.registerBlock(IC2.getIdentifier(name), block);
		return block;
	}

	private static boolean never(BlockState state, BlockGetter world, BlockPos pos)
	{
		return false;
	}

	private static Boolean never(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type)
	{
		return false;
	}

	private static Boolean canSpawnOnLeaves(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type)
	{
		return type == EntityType.OCELOT || type == EntityType.PARROT;
	}

	static
	{
		IC2.envProxy.registerFlammableBlock(RUBBER_LEAVES, 20, 30);
		IC2.envProxy.registerFlammableBlock(RUBBER_LOG, 20, 4);
		IC2.envProxy.registerFlammableBlock(WOODEN_SCAFFOLD, 8, 20);
		IC2.envProxy.registerFlammableBlock(REINFORCED_WOODEN_SCAFFOLD, 8, 20);
	}
}
