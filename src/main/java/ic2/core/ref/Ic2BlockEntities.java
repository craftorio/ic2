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
import ic2.core.block.kineticgenerator.tileentity.TileEntityElectricKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityManualKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntitySteamKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityStirlingKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWaterKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWindKineticGenerator;
import ic2.core.block.machine.tileentity.TileEntityAdvMiner;
import ic2.core.block.machine.tileentity.TileEntityAssemblyBench;
import ic2.core.block.machine.tileentity.TileEntityBatchCrafter;
import ic2.core.block.machine.tileentity.TileEntityBlastFurnace;
import ic2.core.block.machine.tileentity.TileEntityBlockCutter;
import ic2.core.block.machine.tileentity.TileEntityBridgeNuke;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.block.machine.tileentity.TileEntityCentrifuge;
import ic2.core.block.machine.tileentity.TileEntityChunkloader;
import ic2.core.block.machine.tileentity.TileEntityClassicCanner;
import ic2.core.block.machine.tileentity.TileEntityClassicCropmatron;
import ic2.core.block.machine.tileentity.TileEntityClassicElectrolyzer;
import ic2.core.block.machine.tileentity.TileEntityClassicMassFabricator;
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
import ic2.core.block.machine.tileentity.TileEntitySolarDestiller;
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
import ic2.core.block.tileentity.Ic2SignBlockEntity;
import ic2.core.block.tileentity.TileEntityWall;
import ic2.core.block.wiring.tileentity.TileEntityChargepadBatBox;
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
import ic2.core.crop.TileEntityCrop;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class Ic2BlockEntities
{
	public static final BlockEntityType<Ic2SignBlockEntity> SIGN = register("sign", Ic2SignBlockEntity::new, Ic2Blocks.RUBBER_SIGN, Ic2Blocks.RUBBER_WALL_SIGN);
	public static final BlockEntityType<TileEntityWall> WALL = register("wall", TileEntityWall::new, Ic2Blocks.OBSCURED_WALL);
	public static final BlockEntityType<TileEntityITnt> ITNT = register("itnt", TileEntityITnt::new, Ic2Blocks.ITNT);
	public static final BlockEntityType<TileEntityNuke> NUKE = register("nuke", TileEntityNuke::new, Ic2Blocks.NUKE);
	public static final BlockEntityType<TileEntityBridgeNuke.TileEntityClassicNuke> CLASSIC_NUKE = register(
		"classic_nuke", TileEntityBridgeNuke.TileEntityClassicNuke::new, Ic2Blocks.CLASSIC_NUKE
	);
	public static final BlockEntityType<TileEntityGenerator> GENERATOR = register("generator", TileEntityGenerator::new, Ic2Blocks.GENERATOR);
	public static final BlockEntityType<TileEntityGeoGenerator> GEO_GENERATOR = register("geo_generator", TileEntityGeoGenerator::new, Ic2Blocks.GEO_GENERATOR);
	public static final BlockEntityType<TileEntityKineticGenerator> KINETIC_GENERATOR = register(
		"kinetic_generator", TileEntityKineticGenerator::new, Ic2Blocks.KINETIC_GENERATOR
	);
	public static final BlockEntityType<TileEntityRTGenerator> RT_GENERATOR = register("rt_generator", TileEntityRTGenerator::new, Ic2Blocks.RT_GENERATOR);
	public static final BlockEntityType<TileEntitySemifluidGenerator> SEMIFLUID_GENERATOR = register(
		"semifluid_generator", TileEntitySemifluidGenerator::new, Ic2Blocks.SEMIFLUID_GENERATOR
	);
	public static final BlockEntityType<TileEntitySolarGenerator> SOLAR_GENERATOR = register(
		"solar_generator", TileEntitySolarGenerator::new, Ic2Blocks.SOLAR_GENERATOR
	);
	public static final BlockEntityType<TileEntityStirlingGenerator> STIRLING_GENERATOR = register(
		"stirling_generator", TileEntityStirlingGenerator::new, Ic2Blocks.STIRLING_GENERATOR
	);
	public static final BlockEntityType<TileEntityWaterGenerator> WATER_GENERATOR = register(
		"water_generator", TileEntityWaterGenerator::new, Ic2Blocks.WATER_GENERATOR
	);
	public static final BlockEntityType<TileEntityWindGenerator> WIND_GENERATOR = register(
		"wind_generator", TileEntityWindGenerator::new, Ic2Blocks.WIND_GENERATOR
	);
	public static final BlockEntityType<TileEntityElectricHeatGenerator> ELECTRIC_HEAT_GENERATOR = register(
		"electric_heat_generator", TileEntityElectricHeatGenerator::new, Ic2Blocks.ELECTRIC_HEAT_GENERATOR
	);
	public static final BlockEntityType<TileEntityFluidHeatGenerator> FLUID_HEAT_GENERATOR = register(
		"fluid_heat_generator", TileEntityFluidHeatGenerator::new, Ic2Blocks.FLUID_HEAT_GENERATOR
	);
	public static final BlockEntityType<TileEntityRTHeatGenerator> RT_HEAT_GENERATOR = register(
		"rt_heat_generator", TileEntityRTHeatGenerator::new, Ic2Blocks.RT_HEAT_GENERATOR
	);
	public static final BlockEntityType<TileEntitySolidHeatGenerator> SOLID_HEAT_GENERATOR = register(
		"solid_heat_generator", TileEntitySolidHeatGenerator::new, Ic2Blocks.SOLID_HEAT_GENERATOR
	);
	public static final BlockEntityType<TileEntityElectricKineticGenerator> ELECTRIC_KINETIC_GENERATOR = register(
		"electric_kinetic_generator", TileEntityElectricKineticGenerator::new, Ic2Blocks.ELECTRIC_KINETIC_GENERATOR
	);
	public static final BlockEntityType<TileEntityManualKineticGenerator> MANUAL_KINETIC_GENERATOR = register(
		"manual_kinetic_generator", TileEntityManualKineticGenerator::new, Ic2Blocks.MANUAL_KINETIC_GENERATOR
	);
	public static final BlockEntityType<TileEntitySteamKineticGenerator> STEAM_KINETIC_GENERATOR = register(
		"steam_kinetic_generator", TileEntitySteamKineticGenerator::new, Ic2Blocks.STEAM_KINETIC_GENERATOR
	);
	public static final BlockEntityType<TileEntityStirlingKineticGenerator> STIRLING_KINETIC_GENERATOR = register(
		"stirling_kinetic_generator", TileEntityStirlingKineticGenerator::new, Ic2Blocks.STIRLING_KINETIC_GENERATOR
	);
	public static final BlockEntityType<TileEntityWaterKineticGenerator> WATER_KINETIC_GENERATOR = register(
		"water_kinetic_generator", TileEntityWaterKineticGenerator::new, Ic2Blocks.WATER_KINETIC_GENERATOR
	);
	public static final BlockEntityType<TileEntityWindKineticGenerator> WIND_KINETIC_GENERATOR = register(
		"wind_kinetic_generator", TileEntityWindKineticGenerator::new, Ic2Blocks.WIND_KINETIC_GENERATOR
	);
	public static final BlockEntityType<TileEntityNuclearReactorElectric> NUCLEAR_REACTOR = register(
		"nuclear_reactor", TileEntityNuclearReactorElectric::new, Ic2Blocks.NUCLEAR_REACTOR
	);
	public static final BlockEntityType<TileEntityReactorAccessHatch> REACTOR_ACCESS_HATCH = register(
		"reactor_access_hatch", TileEntityReactorAccessHatch::new, Ic2Blocks.REACTOR_ACCESS_HATCH
	);
	public static final BlockEntityType<TileEntityReactorChamberElectric> REACTOR_CHAMBER = register(
		"reactor_chamber", TileEntityReactorChamberElectric::new, Ic2Blocks.REACTOR_CHAMBER
	);
	public static final BlockEntityType<TileEntityReactorFluidPort> REACTOR_FLUID_PORT = register(
		"reactor_fluid_port", TileEntityReactorFluidPort::new, Ic2Blocks.REACTOR_FLUID_PORT
	);
	public static final BlockEntityType<TileEntityReactorRedstonePort> REACTOR_REDSTONE_PORT = register(
		"reactor_redstone_port", TileEntityReactorRedstonePort::new, Ic2Blocks.REACTOR_REDSTONE_PORT
	);
	public static final BlockEntityType<TileEntityCondenser> CONDENSER = register("condenser", TileEntityCondenser::new, Ic2Blocks.CONDENSER);
	public static final BlockEntityType<TileEntityFluidBottler> FLUID_BOTTLER = register("fluid_bottler", TileEntityFluidBottler::new, Ic2Blocks.FLUID_BOTTLER);
	public static final BlockEntityType<TileEntityFluidDistributor> FLUID_DISTRIBUTOR = register(
		"fluid_distributor", TileEntityFluidDistributor::new, Ic2Blocks.FLUID_DISTRIBUTOR
	);
	public static final BlockEntityType<TileEntityFluidRegulator> FLUID_REGULATOR = register(
		"fluid_regulator", TileEntityFluidRegulator::new, Ic2Blocks.FLUID_REGULATOR
	);
	public static final BlockEntityType<TileEntityLiquidHeatExchanger> LIQUID_HEAT_EXCHANGER = register(
		"liquid_heat_exchanger", TileEntityLiquidHeatExchanger::new, Ic2Blocks.LIQUID_HEAT_EXCHANGER
	);
	public static final BlockEntityType<TileEntityPump> PUMP = register("pump", TileEntityPump::new, Ic2Blocks.PUMP);
	public static final BlockEntityType<TileEntitySolarDestiller> SOLAR_DISTILLER = register(
		"solar_distiller", TileEntitySolarDestiller::new, Ic2Blocks.SOLAR_DISTILLER
	);
	public static final BlockEntityType<TileEntitySteamGenerator> STEAM_GENERATOR = register(
		"steam_generator", TileEntitySteamGenerator::new, Ic2Blocks.STEAM_GENERATOR
	);
	public static final BlockEntityType<TileEntityItemBuffer> ITEM_BUFFER = register("item_buffer", TileEntityItemBuffer::new, Ic2Blocks.ITEM_BUFFER);
	public static final BlockEntityType<TileEntityMagnetizer> MAGNETIZER = register("magnetizer", TileEntityMagnetizer::new, Ic2Blocks.MAGNETIZER);
	public static final BlockEntityType<TileEntitySortingMachine> SORTING_MACHINE = register(
		"sorting_machine", TileEntitySortingMachine::new, Ic2Blocks.SORTING_MACHINE
	);
	public static final BlockEntityType<TileEntityTeleporter> TELEPORTER = register("teleporter", TileEntityTeleporter::new, Ic2Blocks.TELEPORTER);
	public static final BlockEntityType<TileEntityTerra> TERRAFORMER = register("terraformer", TileEntityTerra::new, Ic2Blocks.TERRAFORMER);
	public static final BlockEntityType<TileEntityTesla> TESLA_COIL = register("tesla_coil", TileEntityTesla::new, Ic2Blocks.TESLA_COIL);
	public static final BlockEntityType<TileEntityCanner> CANNER = register("canner", TileEntityCanner::new, Ic2Blocks.CANNER);
	public static final BlockEntityType<TileEntityClassicCanner> CLASSIC_CANNER = register(
		"classic_canner", TileEntityClassicCanner::new, Ic2Blocks.CLASSIC_CANNER
	);
	public static final BlockEntityType<TileEntityCompressor> COMPRESSOR = register("compressor", TileEntityCompressor::new, Ic2Blocks.COMPRESSOR);
	public static final BlockEntityType<TileEntityElectricFurnace> ELECTRIC_FURNACE = register(
		"electric_furnace", TileEntityElectricFurnace::new, Ic2Blocks.ELECTRIC_FURNACE
	);
	public static final BlockEntityType<TileEntityExtractor> EXTRACTOR = register("extractor", TileEntityExtractor::new, Ic2Blocks.EXTRACTOR);
	public static final BlockEntityType<TileEntityIronFurnace> IRON_FURNACE = register("iron_furnace", TileEntityIronFurnace::new, Ic2Blocks.IRON_FURNACE);
	public static final BlockEntityType<TileEntityMacerator> MACERATOR = register("macerator", TileEntityMacerator::new, Ic2Blocks.MACERATOR);
	public static final BlockEntityType<TileEntityRecycler> RECYCLER = register("recycler", TileEntityRecycler::new, Ic2Blocks.RECYCLER);
	public static final BlockEntityType<TileEntitySolidCanner> SOLID_CANNER = register("solid_canner", TileEntitySolidCanner::new, Ic2Blocks.SOLID_CANNER);
	public static final BlockEntityType<TileEntityBlastFurnace> BLAST_FURNACE = register("blast_furnace", TileEntityBlastFurnace::new, Ic2Blocks.BLAST_FURNACE);
	public static final BlockEntityType<TileEntityBlockCutter> BLOCK_CUTTER = register("block_cutter", TileEntityBlockCutter::new, Ic2Blocks.BLOCK_CUTTER);
	public static final BlockEntityType<TileEntityCentrifuge> CENTRIFUGE = register("centrifuge", TileEntityCentrifuge::new, Ic2Blocks.CENTRIFUGE);
	public static final BlockEntityType<TileEntityFermenter> FERMENTER = register("fermenter", TileEntityFermenter::new, Ic2Blocks.FERMENTER);
	public static final BlockEntityType<TileEntityInduction> INDUCTION_FURNACE = register(
		"induction_furnace", TileEntityInduction::new, Ic2Blocks.INDUCTION_FURNACE
	);
	public static final BlockEntityType<TileEntityMetalFormer> METAL_FORMER = register("metal_former", TileEntityMetalFormer::new, Ic2Blocks.METAL_FORMER);
	public static final BlockEntityType<TileEntityOreWashing> ORE_WASHING_PLANT = register(
		"ore_washing_plant", TileEntityOreWashing::new, Ic2Blocks.ORE_WASHING_PLANT
	);
	public static final BlockEntityType<TileEntityAdvMiner> ADVANCED_MINER = register("advanced_miner", TileEntityAdvMiner::new, Ic2Blocks.ADVANCED_MINER);
	public static final BlockEntityType<TileEntityCropHarvester> CROP_HARVESTER = register(
		"crop_harvester", TileEntityCropHarvester::new, Ic2Blocks.CROP_HARVESTER
	);
	public static final BlockEntityType<TileEntityCropmatron> CROPMATRON = register("cropmatron", TileEntityCropmatron::new, Ic2Blocks.CROPMATRON);
	public static final BlockEntityType<TileEntityClassicCropmatron> CLASSIC_CROPMATRON = register(
		"classic_cropmatron", TileEntityClassicCropmatron::new, Ic2Blocks.CLASSIC_CROPMATRON
	);
	public static final BlockEntityType<TileEntityMiner> MINER = register("miner", TileEntityMiner::new, Ic2Blocks.MINER);
	public static final BlockEntityType<TileEntityMassFabricator> MASS_FABRICATOR = register(
		"mass_fabricator", TileEntityMassFabricator::new, Ic2Blocks.MASS_FABRICATOR
	);
	public static final BlockEntityType<TileEntityClassicMassFabricator> CLASSIC_MASS_FABRICATOR = register(
		"classic_mass_fabricator", TileEntityClassicMassFabricator::new, Ic2Blocks.CLASSIC_MASS_FABRICATOR
	);
	public static final BlockEntityType<TileEntityAssemblyBench> UU_ASSEMBLY_BENCH = register(
		"uu_assembly_bench", TileEntityAssemblyBench::new, Ic2Blocks.UU_ASSEMBLY_BENCH
	);
	public static final BlockEntityType<TileEntityMatter> MATTER_GENERATOR = register("matter_generator", TileEntityMatter::new, Ic2Blocks.MATTER_GENERATOR);
	public static final BlockEntityType<TileEntityPatternStorage> PATTERN_STORAGE = register(
		"pattern_storage", TileEntityPatternStorage::new, Ic2Blocks.PATTERN_STORAGE
	);
	public static final BlockEntityType<TileEntityReplicator> REPLICATOR = register("replicator", TileEntityReplicator::new, Ic2Blocks.REPLICATOR);
	public static final BlockEntityType<TileEntityScanner> UU_SCANNER = register("uu_scanner", TileEntityScanner::new, Ic2Blocks.UU_SCANNER);
	public static final BlockEntityType<TileEntityEnergyOMat> ENERGY_O_MAT = register("energy_o_mat", TileEntityEnergyOMat::new, Ic2Blocks.ENERGY_O_MAT);
	public static final BlockEntityType<TileEntityPersonalChest> PERSONAL_CHEST = register(
		"personal_chest", TileEntityPersonalChest::new, Ic2Blocks.PERSONAL_CHEST
	);
	public static final BlockEntityType<TileEntityTradeOMat> TRADE_O_MAT = register("trade_o_mat", TileEntityTradeOMat::new, Ic2Blocks.TRADE_O_MAT);
	public static final BlockEntityType<TileEntityChargepadBatBox> BATBOX_CHARGEPAD = register(
		"batbox_chargepad", TileEntityChargepadBatBox::new, Ic2Blocks.BATBOX_CHARGEPAD
	);
	public static final BlockEntityType<TileEntityChargepadCESU> CESU_CHARGEPAD = register(
		"cesu_chargepad", TileEntityChargepadCESU::new, Ic2Blocks.CESU_CHARGEPAD
	);
	public static final BlockEntityType<TileEntityChargepadMFE> MFE_CHARGEPAD = register("mfe_chargepad", TileEntityChargepadMFE::new, Ic2Blocks.MFE_CHARGEPAD);
	public static final BlockEntityType<TileEntityChargepadMFSU> MFSU_CHARGEPAD = register(
		"mfsu_chargepad", TileEntityChargepadMFSU::new, Ic2Blocks.MFSU_CHARGEPAD
	);
	public static final BlockEntityType<TileEntityElectricBatBox> BATBOX = register("batbox", TileEntityElectricBatBox::new, Ic2Blocks.BATBOX);
	public static final BlockEntityType<TileEntityElectricCESU> CESU = register("cesu", TileEntityElectricCESU::new, Ic2Blocks.CESU);
	public static final BlockEntityType<TileEntityElectricMFE> MFE = register("mfe", TileEntityElectricMFE::new, Ic2Blocks.MFE);
	public static final BlockEntityType<TileEntityElectricMFE.TileEntityElectricClassicMFE> CLASSIC_MFE = register(
		"classic_mfe", TileEntityElectricMFE.TileEntityElectricClassicMFE::new, Ic2Blocks.CLASSIC_MFE
	);
	public static final BlockEntityType<TileEntityElectricMFSU> MFSU = register("mfsu", TileEntityElectricMFSU::new, Ic2Blocks.MFSU);
	public static final BlockEntityType<TileEntityElectricMFSU.TileEntityElectricClassicMFSU> CLASSIC_MFSU = register(
		"classic_mfsu", TileEntityElectricMFSU.TileEntityElectricClassicMFSU::new, Ic2Blocks.CLASSIC_MFSU
	);
	public static final BlockEntityType<TileEntityElectrolyzer> ELECTROLYZER = register("electrolyzer", TileEntityElectrolyzer::new, Ic2Blocks.ELECTROLYZER);
	public static final BlockEntityType<TileEntityClassicElectrolyzer> CLASSIC_ELECTROLYZER = register(
		"classic_electrolyzer", TileEntityClassicElectrolyzer::new, Ic2Blocks.CLASSIC_ELECTROLYZER
	);
	public static final BlockEntityType<TileEntityTransformerLV> LV_TRANSFORMER = register(
		"lv_transformer", TileEntityTransformerLV::new, Ic2Blocks.LV_TRANSFORMER
	);
	public static final BlockEntityType<TileEntityTransformerMV> MV_TRANSFORMER = register(
		"mv_transformer", TileEntityTransformerMV::new, Ic2Blocks.MV_TRANSFORMER
	);
	public static final BlockEntityType<TileEntityTransformerHV> HV_TRANSFORMER = register(
		"hv_transformer", TileEntityTransformerHV::new, Ic2Blocks.HV_TRANSFORMER
	);
	public static final BlockEntityType<TileEntityTransformerEV> EV_TRANSFORMER = register(
		"ev_transformer", TileEntityTransformerEV::new, Ic2Blocks.EV_TRANSFORMER
	);
	public static final BlockEntityType<TileEntityTank> TANK = register("tank", TileEntityTank::new, Ic2Blocks.TANK);
	public static final BlockEntityType<TileEntityChunkloader> CHUNK_LOADER = register("chunk_loader", TileEntityChunkloader::new, Ic2Blocks.CHUNK_LOADER);
	public static final BlockEntityType<TileEntityCreativeGenerator> CREATIVE_GENERATOR = register(
		"creative_generator", TileEntityCreativeGenerator::new, Ic2Blocks.CREATIVE_GENERATOR
	);
	public static final BlockEntityType<TileEntitySteamRepressurizer> STEAM_REPRESSURIZER = register(
		"steam_repressurizer", TileEntitySteamRepressurizer::new, Ic2Blocks.STEAM_REPRESSURIZER
	);
	public static final BlockEntityType<TileEntityWeightedFluidDistributor> WEIGHTED_FLUID_DISTRIBUTOR = register(
		"weighted_fluid_distributor", TileEntityWeightedFluidDistributor::new, Ic2Blocks.WEIGHTED_FLUID_DISTRIBUTOR
	);
	public static final BlockEntityType<TileEntityWeightedItemDistributor> WEIGHTED_ITEM_DISTRIBUTOR = register(
		"weighted_item_distributor", TileEntityWeightedItemDistributor::new, Ic2Blocks.WEIGHTED_ITEM_DISTRIBUTOR
	);
	public static final BlockEntityType<TileEntityRCI_RSH> RCI_RSH = register("rci_rsh", TileEntityRCI_RSH::new, Ic2Blocks.RCI_RSH);
	public static final BlockEntityType<TileEntityRCI_LZH> RCI_LZH = register("rci_lzh", TileEntityRCI_LZH::new, Ic2Blocks.RCI_LZH);
	public static final BlockEntityType<TileEntityCrop> CROP_STICK = register("crop_stick", TileEntityCrop::new, Ic2Blocks.CROP_STICK);
	public static final BlockEntityType<TileEntityCrop> WEED_CROP = register("weed_crop", TileEntityCrop::new, Ic2Blocks.WEED_CROP);
	public static final BlockEntityType<TileEntityCrop> WHEAT_CROP = register("wheat_crop", TileEntityCrop::new, Ic2Blocks.WHEAT_CROP);
	public static final BlockEntityType<TileEntityCrop> CARROTS_CROP = register("carrots_crop", TileEntityCrop::new, Ic2Blocks.CARROTS_CROP);
	public static final BlockEntityType<TileEntityCrop> POTATO_CROP = register("potato_crop", TileEntityCrop::new, Ic2Blocks.POTATO_CROP);
	public static final BlockEntityType<TileEntityCrop> BEETROOTS_CROP = register("beetroots_crop", TileEntityCrop::new, Ic2Blocks.BEETROOTS_CROP);
	public static final BlockEntityType<TileEntityCrop> PUMPKIN_CROP = register("pumpkin_crop", TileEntityCrop::new, Ic2Blocks.PUMPKIN_CROP);
	public static final BlockEntityType<TileEntityCrop> MELON_CROP = register("melon_crop", TileEntityCrop::new, Ic2Blocks.MELON_CROP);
	public static final BlockEntityType<TileEntityCrop> DANDELION_CROP = register("dandelion_crop", TileEntityCrop::new, Ic2Blocks.DANDELION_CROP);
	public static final BlockEntityType<TileEntityCrop> POPPY_CROP = register("poppy_crop", TileEntityCrop::new, Ic2Blocks.POPPY_CROP);
	public static final BlockEntityType<TileEntityCrop> BLACKTHORN_CROP = register("blackthorn_crop", TileEntityCrop::new, Ic2Blocks.BLACKTHORN_CROP);
	public static final BlockEntityType<TileEntityCrop> TULIP_CROP = register("tulip_crop", TileEntityCrop::new, Ic2Blocks.TULIP_CROP);
	public static final BlockEntityType<TileEntityCrop> CYAZINT_CROP = register("cyazint_crop", TileEntityCrop::new, Ic2Blocks.CYAZINT_CROP);
	public static final BlockEntityType<TileEntityCrop> VENOMILIA_CROP = register("venomilia_crop", TileEntityCrop::new, Ic2Blocks.VENOMILIA_CROP);
	public static final BlockEntityType<TileEntityCrop> REED_CROP = register("reed_crop", TileEntityCrop::new, Ic2Blocks.REED_CROP);
	public static final BlockEntityType<TileEntityCrop> STICKY_REED_CROP = register("sticky_reed_crop", TileEntityCrop::new, Ic2Blocks.STICKY_REED_CROP);
	public static final BlockEntityType<TileEntityCrop> COCOA_CROP = register("cocoa_crop", TileEntityCrop::new, Ic2Blocks.COCOA_CROP);
	public static final BlockEntityType<TileEntityCrop> FLAX_CROP = register("flax_crop", TileEntityCrop::new, Ic2Blocks.FLAX_CROP);
	public static final BlockEntityType<TileEntityCrop> RED_MUSHROOM_CROP = register("red_mushroom_crop", TileEntityCrop::new, Ic2Blocks.RED_MUSHROOM_CROP);
	public static final BlockEntityType<TileEntityCrop> BROWN_MUSHROOM_CROP = register("brown_mushroom_crop", TileEntityCrop::new, Ic2Blocks.BROWN_MUSHROOM_CROP);
	public static final BlockEntityType<TileEntityCrop> NETHER_WART_CROP = register("nether_wart_crop", TileEntityCrop::new, Ic2Blocks.NETHER_WART_CROP);
	public static final BlockEntityType<TileEntityCrop> TERRA_WART_CROP = register("terra_wart_crop", TileEntityCrop::new, Ic2Blocks.TERRA_WART_CROP);
	public static final BlockEntityType<TileEntityCrop> OAK_SAPLING_CROP = register("oak_sapling_crop", TileEntityCrop::new, Ic2Blocks.OAK_SAPLING_CROP);
	public static final BlockEntityType<TileEntityCrop> SPRUCE_SAPLING_CROP = register("spruce_sapling_crop", TileEntityCrop::new, Ic2Blocks.SPRUCE_SAPLING_CROP);
	public static final BlockEntityType<TileEntityCrop> BIRCH_SAPLING_CROP = register("birch_sapling_crop", TileEntityCrop::new, Ic2Blocks.BIRCH_SAPLING_CROP);
	public static final BlockEntityType<TileEntityCrop> JUNGLE_SAPLING_CROP = register("jungle_sapling_crop", TileEntityCrop::new, Ic2Blocks.JUNGLE_SAPLING_CROP);
	public static final BlockEntityType<TileEntityCrop> ACACIA_SAPLING_CROP = register("acacia_sapling_crop", TileEntityCrop::new, Ic2Blocks.ACACIA_SAPLING_CROP);
	public static final BlockEntityType<TileEntityCrop> DARK_OAK_SAPLING_CROP = register(
		"dark_oak_sapling_crop", TileEntityCrop::new, Ic2Blocks.DARK_OAK_SAPLING_CROP
	);
	public static final BlockEntityType<TileEntityCrop> FERRU_CROP = register("ferru_crop", TileEntityCrop::new, Ic2Blocks.FERRU_CROP);
	public static final BlockEntityType<TileEntityCrop> CYPRIUM_CROP = register("cyprium_crop", TileEntityCrop::new, Ic2Blocks.CYPRIUM_CROP);
	public static final BlockEntityType<TileEntityCrop> STAGNIUM_CROP = register("stagnium_crop", TileEntityCrop::new, Ic2Blocks.STAGNIUM_CROP);
	public static final BlockEntityType<TileEntityCrop> PLUMBISCUS_CROP = register("plumbiscus_crop", TileEntityCrop::new, Ic2Blocks.PLUMBISCUS_CROP);
	public static final BlockEntityType<TileEntityCrop> AURELIA_CROP = register("aurelia_crop", TileEntityCrop::new, Ic2Blocks.AURELIA_CROP);
	public static final BlockEntityType<TileEntityCrop> SHINING_CROP = register("shining_crop", TileEntityCrop::new, Ic2Blocks.SHINING_CROP);
	public static final BlockEntityType<TileEntityCrop> RED_WHEAT_CROP = register("red_wheat_crop", TileEntityCrop::new, Ic2Blocks.RED_WHEAT_CROP);
	public static final BlockEntityType<TileEntityCrop> COFFEE_CROP = register("coffee_crop", TileEntityCrop::new, Ic2Blocks.COFFEE_CROP);
	public static final BlockEntityType<TileEntityCrop> HOPS_CROP = register("hops_crop", TileEntityCrop::new, Ic2Blocks.HOPS_CROP);
	public static final BlockEntityType<TileEntityCrop> EATING_PLANT_CROP = register("eating_plant_crop", TileEntityCrop::new, Ic2Blocks.EATING_PLANT_CROP);
	public static final BlockEntityType<TileEntityIndustrialWorkbench> INDUSTRIAL_WORKBENCH = register(
		"industrial_workbench", TileEntityIndustrialWorkbench::new, Ic2Blocks.INDUSTRIAL_WORKBENCH
	);
	public static final BlockEntityType<TileEntityBatchCrafter> BATCH_CRAFTER = register("batch_crafter", TileEntityBatchCrafter::new, Ic2Blocks.BATCH_CRAFTER);
	public static final BlockEntityType<TileEntityWoodenStorageBox> WOODEN_STORAGE_BOX = register(
		"wooden_storage_box", TileEntityWoodenStorageBox::new, Ic2Blocks.WOODEN_STORAGE_BOX
	);
	public static final BlockEntityType<TileEntityIronStorageBox> IRON_STORAGE_BOX = register(
		"iron_storage_box", TileEntityIronStorageBox::new, Ic2Blocks.IRON_STORAGE_BOX
	);
	public static final BlockEntityType<TileEntityBronzeStorageBox> BRONZE_STORAGE_BOX = register(
		"bronze_storage_box", TileEntityBronzeStorageBox::new, Ic2Blocks.BRONZE_STORAGE_BOX
	);
	public static final BlockEntityType<TileEntitySteelStorageBox> STEEL_STORAGE_BOX = register(
		"steel_storage_box", TileEntitySteelStorageBox::new, Ic2Blocks.STEEL_STORAGE_BOX
	);
	public static final BlockEntityType<TileEntityIridiumStorageBox> IRIDIUM_STORAGE_BOX = register(
		"iridium_storage_box", TileEntityIridiumStorageBox::new, Ic2Blocks.IRIDIUM_STORAGE_BOX
	);
	public static final BlockEntityType<TileEntityBronzeTank> BRONZE_TANK = register("bronze_tank", TileEntityBronzeTank::new, Ic2Blocks.BRONZE_TANK);
	public static final BlockEntityType<TileEntityIronTank> IRON_TANK = register("iron_tank", TileEntityIronTank::new, Ic2Blocks.IRON_TANK);
	public static final BlockEntityType<TileEntitySteelTank> STEEL_TANK = register("steel_tank", TileEntitySteelTank::new, Ic2Blocks.STEEL_TANK);
	public static final BlockEntityType<TileEntityIridiumTank> IRIDIUM_TANK = register("iridium_tank", TileEntityIridiumTank::new, Ic2Blocks.IRIDIUM_TANK);
	private static Map<String, BlockEntityType<?>> blockEntityTypeMap;

	public static void init()
	{
	}

	private static <T extends BlockEntity> BlockEntityType<T> register(String name, BiFunction<BlockPos, BlockState, T> factory, Block... blocks)
	{
		if (blockEntityTypeMap == null)
		{
			blockEntityTypeMap = new HashMap<>();
		}

		ResourceLocation identifier = IC2.getIdentifier(name);
		BlockEntityType<T> blockEntityType = IC2.envProxy.registerBlockEntity(identifier, factory, blocks);
		blockEntityTypeMap.put(identifier.toString(), blockEntityType);
		return blockEntityType;
	}

	public static BlockEntityType<?> get(ResourceLocation identifier)
	{
		return blockEntityTypeMap.get(identifier.toString());
	}
}
