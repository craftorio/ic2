// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.ref;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ic2.core.block.storage.tank.TileEntityIridiumTank;
import ic2.core.block.storage.tank.TileEntitySteelTank;
import ic2.core.block.storage.tank.TileEntityIronTank;
import ic2.core.block.storage.tank.TileEntityBronzeTank;
import ic2.core.block.storage.box.TileEntityIridiumStorageBox;
import ic2.core.block.storage.box.TileEntitySteelStorageBox;
import ic2.core.block.storage.box.TileEntityBronzeStorageBox;
import ic2.core.block.storage.box.TileEntityIronStorageBox;
import ic2.core.block.storage.box.TileEntityWoodenStorageBox;
import ic2.core.block.steam.TileEntityCokeKilnGrate;
import ic2.core.block.steam.TileEntityCokeKilnHatch;
import ic2.core.block.steam.TileEntityCokeKiln;
import ic2.core.block.transport.TileEntityFluidPipe;
import ic2.core.block.machine.tileentity.TileEntityBatchCrafter;
import ic2.core.block.machine.tileentity.TileEntityIndustrialWorkbench;
import ic2.core.crop.TileEntityCrop;
import ic2.core.block.reactor.tileentity.TileEntityRCI_LZH;
import ic2.core.block.reactor.tileentity.TileEntityRCI_RSH;
import ic2.core.block.machine.tileentity.TileEntityWeightedItemDistributor;
import ic2.core.block.machine.tileentity.TileEntityWeightedFluidDistributor;
import ic2.core.block.machine.tileentity.TileEntitySteamRepressurizer;
import ic2.core.block.generator.tileentity.TileEntityCreativeGenerator;
import ic2.core.block.machine.tileentity.TileEntityBetterItemBuffer;
import ic2.core.block.machine.tileentity.TileEntityChunkloader;
import ic2.core.block.machine.tileentity.TileEntityTank;
import ic2.core.block.wiring.TileEntityTransformerEV;
import ic2.core.block.wiring.TileEntityTransformerHV;
import ic2.core.block.wiring.TileEntityTransformerMV;
import ic2.core.block.wiring.TileEntityTransformerLV;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;
import ic2.core.block.wiring.TileEntityElectricMFSU;
import ic2.core.block.wiring.TileEntityElectricMFE;
import ic2.core.block.wiring.TileEntityElectricCESU;
import ic2.core.block.wiring.TileEntityElectricBatBox;
import ic2.core.block.wiring.TileEntityChargepadMFSU;
import ic2.core.block.wiring.TileEntityChargepadMFE;
import ic2.core.block.wiring.TileEntityChargepadCESU;
import ic2.core.block.wiring.TileEntityChargepadBatBox;
import ic2.core.block.wiring.TileEntityCableSplitter;
import ic2.core.block.wiring.TileEntityCableDetector;
import ic2.core.block.wiring.TileEntityCable;
import ic2.core.block.personal.TileEntityTradingTerminal;
import ic2.core.block.personal.TileEntityTradeOMat;
import ic2.core.block.personal.TileEntityPersonalChest;
import ic2.core.block.personal.TileEntityEnergyOMat;
import ic2.core.block.machine.tileentity.TileEntityScanner;
import ic2.core.block.machine.tileentity.TileEntityReplicator;
import ic2.core.block.machine.tileentity.TileEntityPatternStorage;
import ic2.core.block.machine.tileentity.TileEntityMatter;
import ic2.core.block.machine.tileentity.TileEntityAssemblyBench;
import ic2.core.block.machine.tileentity.TileEntityMassFabricator;
import ic2.core.block.machine.tileentity.TileEntityMiner;
import ic2.core.block.machine.tileentity.TileEntityCropmatron;
import ic2.core.block.machine.tileentity.TileEntityCropHarvester;
import ic2.core.block.machine.tileentity.TileEntityAdvMiner;
import ic2.core.block.machine.tileentity.TileEntityOreWashing;
import ic2.core.block.machine.tileentity.TileEntityMetalFormer;
import ic2.core.block.machine.tileentity.TileEntityInduction;
import ic2.core.block.machine.tileentity.TileEntityFermenter;
import ic2.core.block.machine.tileentity.TileEntityCentrifuge;
import ic2.core.block.machine.tileentity.TileEntityBlockCutter;
import ic2.core.block.machine.tileentity.TileEntityBlastFurnace;
import ic2.core.block.machine.tileentity.TileEntitySolidCanner;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import ic2.core.block.machine.tileentity.TileEntityMacerator;
import ic2.core.block.machine.tileentity.TileEntityIronFurnace;
import ic2.core.block.machine.tileentity.TileEntityExtractor;
import ic2.core.block.machine.tileentity.TileEntityElectricFurnace;
import ic2.core.block.machine.tileentity.TileEntityCompressor;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.block.machine.tileentity.TileEntityTesla;
import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.block.machine.tileentity.TileEntityTeleporter;
import ic2.core.block.machine.tileentity.TileEntitySortingMachine;
import ic2.core.block.machine.tileentity.TileEntityMagnetizer;
import ic2.core.block.wiring.TileEntityLuminator;
import ic2.core.block.machine.tileentity.TileEntityItemBuffer;
import ic2.core.block.machine.tileentity.TileEntitySteamGenerator;
import ic2.core.block.machine.tileentity.TileEntitySolarDestiller;
import ic2.core.block.machine.tileentity.TileEntityPump;
import ic2.core.block.machine.tileentity.TileEntityLiquidHeatExchanger;
import ic2.core.block.machine.tileentity.TileEntityFluidRegulator;
import ic2.core.block.machine.tileentity.TileEntityFluidDistributor;
import ic2.core.block.machine.tileentity.TileEntityFluidBottler;
import ic2.core.block.machine.tileentity.TileEntityCondenser;
import ic2.core.block.reactor.tileentity.TileEntityReactorRedstonePort;
import ic2.core.block.reactor.tileentity.TileEntityReactorFluidPort;
import ic2.core.block.reactor.tileentity.TileEntityReactorChamberElectric;
import ic2.core.block.reactor.tileentity.TileEntityReactorAccessHatch;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWindKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWaterKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityStirlingKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntitySteamKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityManualKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityElectricKineticGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntitySolidHeatGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntityRTHeatGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntityFluidHeatGenerator;
import ic2.core.block.heatgenerator.tileentity.TileEntityElectricHeatGenerator;
import ic2.core.block.generator.tileentity.TileEntityWindGenerator;
import ic2.core.block.generator.tileentity.TileEntityWaterGenerator;
import ic2.core.block.generator.tileentity.TileEntityStirlingGenerator;
import ic2.core.block.generator.tileentity.TileEntitySolarGenerator;
import ic2.core.block.generator.tileentity.TileEntitySemifluidGenerator;
import ic2.core.block.generator.tileentity.TileEntityRTGenerator;
import ic2.core.block.generator.tileentity.TileEntityKineticGenerator;
import ic2.core.block.generator.tileentity.TileEntityGeoGenerator;
import ic2.core.block.generator.tileentity.TileEntityGenerator;
import ic2.core.block.machine.tileentity.TileEntityNuke;
import ic2.core.block.machine.tileentity.ITnt;
import ic2.core.block.TileEntityWall;
import ic2.core.block.TileEntityBarrel;
import ic2.core.block.TeBlockRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import java.lang.annotation.Annotation;
import net.minecraftforge.fml.common.ModContainer;
import ic2.core.util.Util;
import net.minecraftforge.fml.common.Loader;
import ic2.core.util.StackUtil;
import ic2.api.tile.IEnergyStorage;
import java.lang.reflect.AnnotatedElement;
import ic2.core.profile.Version;
import ic2.core.IC2;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.item.block.ItemBlockTileEntity;
import ic2.core.block.BlockTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.block.material.Material;
import net.minecraft.item.EnumRarity;
import net.minecraft.util.EnumFacing;
import java.util.Set;
import ic2.core.block.TileEntityBlock;
import net.minecraft.util.ResourceLocation;
import ic2.core.block.ITeBlock;

public enum TeBlock implements ITeBlock, ITeBlockCreativeRegisterer
{
    invalid((Class<? extends TileEntityBlock>)null, 0, false, Util.noFacings, false, HarvestTool.None, DefaultDrop.None, 5.0f, 10.0f, EnumRarity.COMMON), 
    barrel((Class<? extends TileEntityBlock>)TileEntityBarrel.class, -1, true, Util.horizontalFacings, false, HarvestTool.Axe, DefaultDrop.None, 2.0f, 6.0f, EnumRarity.COMMON), 
    wall((Class<? extends TileEntityBlock>)TileEntityWall.class, -1, false, Util.noFacings, false, HarvestTool.Pickaxe, DefaultDrop.Self, 3.0f, 30.0f, EnumRarity.COMMON), 
    itnt((Class<? extends TileEntityBlock>)ITnt.class, 1, false, Util.horizontalFacings, false, HarvestTool.None, DefaultDrop.Self, 0.0f, 0.0f, EnumRarity.COMMON), 
    nuke((Class<? extends TileEntityBlock>)TileEntityNuke.delegate(), 2, false, Util.horizontalFacings, false, HarvestTool.None, DefaultDrop.Self, 0.0f, 0.0f, EnumRarity.UNCOMMON), 
    generator((Class<? extends TileEntityBlock>)TileEntityGenerator.class, 3, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    geo_generator((Class<? extends TileEntityBlock>)TileEntityGeoGenerator.class, 4, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Generator, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    kinetic_generator((Class<? extends TileEntityBlock>)TileEntityKineticGenerator.class, 5, true, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Generator, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    rt_generator((Class<? extends TileEntityBlock>)TileEntityRTGenerator.class, 6, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Generator, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    semifluid_generator((Class<? extends TileEntityBlock>)TileEntitySemifluidGenerator.class, 7, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Generator, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    solar_generator((Class<? extends TileEntityBlock>)TileEntitySolarGenerator.class, 8, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Generator, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    stirling_generator((Class<? extends TileEntityBlock>)TileEntityStirlingGenerator.class, 9, true, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Generator, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    water_generator((Class<? extends TileEntityBlock>)TileEntityWaterGenerator.class, 10, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    wind_generator((Class<? extends TileEntityBlock>)TileEntityWindGenerator.class, 11, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Generator, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    electric_heat_generator((Class<? extends TileEntityBlock>)TileEntityElectricHeatGenerator.class, 12, true, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    fluid_heat_generator((Class<? extends TileEntityBlock>)TileEntityFluidHeatGenerator.class, 13, true, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    rt_heat_generator((Class<? extends TileEntityBlock>)TileEntityRTHeatGenerator.class, 14, true, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    solid_heat_generator((Class<? extends TileEntityBlock>)TileEntitySolidHeatGenerator.class, 15, true, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    electric_kinetic_generator((Class<? extends TileEntityBlock>)TileEntityElectricKineticGenerator.class, 16, true, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    manual_kinetic_generator((Class<? extends TileEntityBlock>)TileEntityManualKineticGenerator.class, 17, false, Util.allFacings, true, HarvestTool.Pickaxe, DefaultDrop.Self, 5.0f, 10.0f, EnumRarity.COMMON), 
    steam_kinetic_generator((Class<? extends TileEntityBlock>)TileEntitySteamKineticGenerator.class, 18, true, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    stirling_kinetic_generator((Class<? extends TileEntityBlock>)TileEntityStirlingKineticGenerator.class, 19, true, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    water_kinetic_generator((Class<? extends TileEntityBlock>)TileEntityWaterKineticGenerator.class, 20, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    wind_kinetic_generator((Class<? extends TileEntityBlock>)TileEntityWindKineticGenerator.class, 21, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    nuclear_reactor((Class<? extends TileEntityBlock>)TileEntityNuclearReactorElectric.class, 22, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Generator, 2.0f, 10.0f, EnumRarity.UNCOMMON, (Material)IC2Material.MACHINE, false), 
    reactor_access_hatch((Class<? extends TileEntityBlock>)TileEntityReactorAccessHatch.class, 23, false, Util.onlyNorth, false, HarvestTool.Pickaxe, DefaultDrop.Self, 40.0f, 90.0f, EnumRarity.UNCOMMON), 
    reactor_chamber((Class<? extends TileEntityBlock>)TileEntityReactorChamberElectric.class, 24, false, Util.onlyNorth, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.UNCOMMON, (Material)IC2Material.MACHINE, false), 
    reactor_fluid_port((Class<? extends TileEntityBlock>)TileEntityReactorFluidPort.class, 25, false, Util.onlyNorth, false, HarvestTool.Pickaxe, DefaultDrop.Self, 40.0f, 90.0f, EnumRarity.UNCOMMON), 
    reactor_redstone_port((Class<? extends TileEntityBlock>)TileEntityReactorRedstonePort.class, 26, false, Util.onlyNorth, false, HarvestTool.Pickaxe, DefaultDrop.Self, 40.0f, 90.0f, EnumRarity.UNCOMMON), 
    condenser((Class<? extends TileEntityBlock>)TileEntityCondenser.class, 27, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    fluid_bottler((Class<? extends TileEntityBlock>)TileEntityFluidBottler.class, 28, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    fluid_distributor((Class<? extends TileEntityBlock>)TileEntityFluidDistributor.class, 29, true, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    fluid_regulator((Class<? extends TileEntityBlock>)TileEntityFluidRegulator.class, 30, true, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    liquid_heat_exchanger((Class<? extends TileEntityBlock>)TileEntityLiquidHeatExchanger.class, 31, true, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    pump((Class<? extends TileEntityBlock>)TileEntityPump.class, 32, true, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    solar_distiller((Class<? extends TileEntityBlock>)TileEntitySolarDestiller.class, 33, false, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    steam_generator((Class<? extends TileEntityBlock>)TileEntitySteamGenerator.class, 34, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    item_buffer((Class<? extends TileEntityBlock>)TileEntityItemBuffer.class, 35, false, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    luminator_flat((Class<? extends TileEntityBlock>)TileEntityLuminator.class, 36, true, Util.allFacings, false, HarvestTool.Pickaxe, DefaultDrop.Self, 5.0f, 10.0f, EnumRarity.COMMON), 
    magnetizer((Class<? extends TileEntityBlock>)TileEntityMagnetizer.class, 37, false, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    sorting_machine((Class<? extends TileEntityBlock>)TileEntitySortingMachine.class, 38, false, Util.onlyNorth, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    teleporter((Class<? extends TileEntityBlock>)TileEntityTeleporter.class, 39, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.AdvMachine, 2.0f, 10.0f, EnumRarity.RARE, (Material)IC2Material.MACHINE, false), 
    terraformer((Class<? extends TileEntityBlock>)TileEntityTerra.class, 40, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.AdvMachine, 2.0f, 10.0f, EnumRarity.UNCOMMON, (Material)IC2Material.MACHINE, false), 
    tesla_coil((Class<? extends TileEntityBlock>)TileEntityTesla.class, 41, false, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    canner((Class<? extends TileEntityBlock>)TileEntityCanner.delegate(), 42, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    compressor((Class<? extends TileEntityBlock>)TileEntityCompressor.class, 43, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    electric_furnace((Class<? extends TileEntityBlock>)TileEntityElectricFurnace.class, 44, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    extractor((Class<? extends TileEntityBlock>)TileEntityExtractor.class, 45, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    iron_furnace((Class<? extends TileEntityBlock>)TileEntityIronFurnace.class, 46, true, Util.horizontalFacings, false, HarvestTool.Pickaxe, DefaultDrop.Self, 5.0f, 10.0f, EnumRarity.COMMON), 
    macerator((Class<? extends TileEntityBlock>)TileEntityMacerator.class, 47, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    recycler((Class<? extends TileEntityBlock>)TileEntityRecycler.class, 48, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    solid_canner((Class<? extends TileEntityBlock>)TileEntitySolidCanner.class, 49, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    blast_furnace((Class<? extends TileEntityBlock>)TileEntityBlastFurnace.class, 50, true, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    block_cutter((Class<? extends TileEntityBlock>)TileEntityBlockCutter.class, 51, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.AdvMachine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    centrifuge((Class<? extends TileEntityBlock>)TileEntityCentrifuge.class, 52, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    fermenter((Class<? extends TileEntityBlock>)TileEntityFermenter.class, 53, true, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    induction_furnace((Class<? extends TileEntityBlock>)TileEntityInduction.class, 54, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.AdvMachine, 2.0f, 10.0f, EnumRarity.UNCOMMON, (Material)IC2Material.MACHINE, false), 
    metal_former((Class<? extends TileEntityBlock>)TileEntityMetalFormer.class, 55, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    ore_washing_plant((Class<? extends TileEntityBlock>)TileEntityOreWashing.class, 56, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    advanced_miner((Class<? extends TileEntityBlock>)TileEntityAdvMiner.class, 57, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    crop_harvester((Class<? extends TileEntityBlock>)TileEntityCropHarvester.class, 58, false, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    cropmatron((Class<? extends TileEntityBlock>)TileEntityCropmatron.delegate(), 59, false, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    miner((Class<? extends TileEntityBlock>)TileEntityMiner.class, 60, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    mass_fabricator((Class<? extends TileEntityBlock>)TileEntityMassFabricator.delegate(), 92, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.AdvMachine, 2.0f, 10.0f, EnumRarity.RARE, (Material)IC2Material.MACHINE, false), 
    uu_assembly_bench((Class<? extends TileEntityBlock>)TileEntityAssemblyBench.class, 93, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.AdvMachine, 2.0f, 10.0f, EnumRarity.UNCOMMON, (Material)IC2Material.MACHINE, false), 
    matter_generator((Class<? extends TileEntityBlock>)TileEntityMatter.class, 61, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.AdvMachine, 2.0f, 10.0f, EnumRarity.RARE, (Material)IC2Material.MACHINE, false), 
    pattern_storage((Class<? extends TileEntityBlock>)TileEntityPatternStorage.class, 62, false, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.AdvMachine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    replicator((Class<? extends TileEntityBlock>)TileEntityReplicator.class, 63, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.AdvMachine, 2.0f, 10.0f, EnumRarity.UNCOMMON, (Material)IC2Material.MACHINE, false), 
    scanner((Class<? extends TileEntityBlock>)TileEntityScanner.class, 64, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.AdvMachine, 2.0f, 10.0f, EnumRarity.UNCOMMON, (Material)IC2Material.MACHINE, false), 
    energy_o_mat((Class<? extends TileEntityBlock>)TileEntityEnergyOMat.class, 65, false, Util.allFacings, false, HarvestTool.Pickaxe, DefaultDrop.Self, -1.0f, 3600000.0f, EnumRarity.COMMON), 
    personal_chest((Class<? extends TileEntityBlock>)TileEntityPersonalChest.class, 66, false, Util.horizontalFacings, false, HarvestTool.Pickaxe, DefaultDrop.Self, -1.0f, 3600000.0f, EnumRarity.UNCOMMON), 
    trade_o_mat((Class<? extends TileEntityBlock>)TileEntityTradeOMat.class, 67, true, Util.horizontalFacings, false, HarvestTool.Pickaxe, DefaultDrop.Self, -1.0f, 3600000.0f, EnumRarity.COMMON), 
    trading_terminal((Class<? extends TileEntityBlock>)TileEntityTradingTerminal.class, 94, false, Util.horizontalFacings, false, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.UNCOMMON, (Material)IC2Material.MACHINE, false), 
    cable((Class<? extends TileEntityBlock>)TileEntityCable.delegate(), -1, false, Util.noFacings, false, HarvestTool.Pickaxe, DefaultDrop.Self, 0.5f, 5.0f, EnumRarity.COMMON, Material.CLOTH, true), 
    detector_cable((Class<? extends TileEntityBlock>)TileEntityCableDetector.delegate(), -1, false, Util.noFacings, false, HarvestTool.Pickaxe, DefaultDrop.Self, 0.5f, 5.0f, EnumRarity.COMMON, Material.CLOTH, false), 
    splitter_cable((Class<? extends TileEntityBlock>)TileEntityCableSplitter.delegate(), -1, false, Util.noFacings, false, HarvestTool.Pickaxe, DefaultDrop.Self, 0.5f, 5.0f, EnumRarity.COMMON, Material.CLOTH, false), 
    chargepad_batbox((Class<? extends TileEntityBlock>)TileEntityChargepadBatBox.class, 68, true, Util.downSideFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    chargepad_cesu((Class<? extends TileEntityBlock>)TileEntityChargepadCESU.class, 69, true, Util.downSideFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    chargepad_mfe((Class<? extends TileEntityBlock>)TileEntityChargepadMFE.class, 70, true, Util.downSideFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    chargepad_mfsu((Class<? extends TileEntityBlock>)TileEntityChargepadMFSU.class, 71, true, Util.downSideFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.UNCOMMON, (Material)IC2Material.MACHINE, false), 
    batbox((Class<? extends TileEntityBlock>)TileEntityElectricBatBox.class, 72, false, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    cesu((Class<? extends TileEntityBlock>)TileEntityElectricCESU.class, 73, false, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    mfe((Class<? extends TileEntityBlock>)TileEntityElectricMFE.delegate(), 74, false, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    mfsu((Class<? extends TileEntityBlock>)TileEntityElectricMFSU.delegate(), 75, false, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.AdvMachine, 2.0f, 10.0f, EnumRarity.UNCOMMON, (Material)IC2Material.MACHINE, false), 
    electrolyzer((Class<? extends TileEntityBlock>)TileEntityElectrolyzer.delegate(), 76, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    lv_transformer((Class<? extends TileEntityBlock>)TileEntityTransformerLV.class, 77, false, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    mv_transformer((Class<? extends TileEntityBlock>)TileEntityTransformerMV.class, 78, false, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    hv_transformer((Class<? extends TileEntityBlock>)TileEntityTransformerHV.class, 79, false, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.UNCOMMON, (Material)IC2Material.MACHINE, false), 
    ev_transformer((Class<? extends TileEntityBlock>)TileEntityTransformerEV.class, 80, false, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.UNCOMMON, (Material)IC2Material.MACHINE, false), 
    tank((Class<? extends TileEntityBlock>)TileEntityTank.class, 81, false, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    chunk_loader((Class<? extends TileEntityBlock>)TileEntityChunkloader.class, 82, true, Util.downSideFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.UNCOMMON, (Material)IC2Material.MACHINE, false), 
    item_buffer_2((Class<? extends TileEntityBlock>)TileEntityBetterItemBuffer.class, 83, false, Util.noFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    creative_generator((Class<? extends TileEntityBlock>)TileEntityCreativeGenerator.class, 86, true, Util.noFacings, false, HarvestTool.None, DefaultDrop.None, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, EnumRarity.COMMON), 
    steam_repressurizer((Class<? extends TileEntityBlock>)TileEntitySteamRepressurizer.class, 87, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    weighted_fluid_distributor((Class<? extends TileEntityBlock>)TileEntityWeightedFluidDistributor.class, 90, false, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    weighted_item_distributor((Class<? extends TileEntityBlock>)TileEntityWeightedItemDistributor.class, 91, false, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.Machine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    rci_rsh((Class<? extends TileEntityBlock>)TileEntityRCI_RSH.class, 84, true, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.AdvMachine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    rci_lzh((Class<? extends TileEntityBlock>)TileEntityRCI_LZH.class, 85, true, Util.allFacings, true, HarvestTool.Wrench, DefaultDrop.AdvMachine, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    crop((Class<? extends TileEntityBlock>)TileEntityCrop.class, -1, false, Util.noFacings, false, HarvestTool.Axe, DefaultDrop.Self, 0.8f, 0.2f, EnumRarity.COMMON, Material.PLANTS, true), 
    industrial_workbench((Class<? extends TileEntityBlock>)TileEntityIndustrialWorkbench.class, 88, false, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    batch_crafter((Class<? extends TileEntityBlock>)TileEntityBatchCrafter.class, 89, true, Util.horizontalFacings, true, HarvestTool.Wrench, DefaultDrop.AdvMachine, 2.0f, 10.0f, EnumRarity.UNCOMMON, (Material)IC2Material.MACHINE, false), 
    fluid_pipe((Class<? extends TileEntityBlock>)TileEntityFluidPipe.class, -1, false, Util.allFacings, false, HarvestTool.Wrench, DefaultDrop.Self, 1.0f, 5.0f, EnumRarity.COMMON, (Material)IC2Material.PIPE, true), 
    coke_kiln((Class<? extends TileEntityBlock>)TileEntityCokeKiln.class, 100, true, Util.horizontalFacings, true, HarvestTool.Pickaxe, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, Material.ROCK, false), 
    coke_kiln_hatch((Class<? extends TileEntityBlock>)TileEntityCokeKilnHatch.class, 101, false, Util.allFacings, true, HarvestTool.Pickaxe, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, Material.ROCK, false), 
    coke_kiln_grate((Class<? extends TileEntityBlock>)TileEntityCokeKilnGrate.class, 102, false, Util.allFacings, true, HarvestTool.Pickaxe, DefaultDrop.Self, 2.0f, 10.0f, EnumRarity.COMMON, Material.ROCK, false), 
    wooden_storage_box((Class<? extends TileEntityBlock>)TileEntityWoodenStorageBox.class, 111, false, Util.noFacings, false, HarvestTool.Axe, DefaultDrop.Self, 1.0f, 10.0f, EnumRarity.COMMON, Material.WOOD, false), 
    iron_storage_box((Class<? extends TileEntityBlock>)TileEntityIronStorageBox.class, 112, false, Util.noFacings, false, HarvestTool.Wrench, DefaultDrop.Self, 1.0f, 15.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    bronze_storage_box((Class<? extends TileEntityBlock>)TileEntityBronzeStorageBox.class, 113, false, Util.noFacings, false, HarvestTool.Wrench, DefaultDrop.Self, 1.0f, 15.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, false), 
    steel_storage_box((Class<? extends TileEntityBlock>)TileEntitySteelStorageBox.class, 114, false, Util.noFacings, false, HarvestTool.Wrench, DefaultDrop.Self, 2.0f, 20.0f, EnumRarity.UNCOMMON, (Material)IC2Material.MACHINE, false), 
    iridium_storage_box((Class<? extends TileEntityBlock>)TileEntityIridiumStorageBox.class, 115, false, Util.noFacings, false, HarvestTool.Wrench, DefaultDrop.Self, 3.0f, 100.0f, EnumRarity.EPIC, (Material)IC2Material.MACHINE, false), 
    bronze_tank((Class<? extends TileEntityBlock>)TileEntityBronzeTank.class, 131, false, Util.horizontalFacings, false, HarvestTool.Wrench, DefaultDrop.Self, 3.0f, 15.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, true), 
    iron_tank((Class<? extends TileEntityBlock>)TileEntityIronTank.class, 132, false, Util.horizontalFacings, false, HarvestTool.Wrench, DefaultDrop.Self, 3.0f, 15.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, true), 
    steel_tank((Class<? extends TileEntityBlock>)TileEntitySteelTank.class, 133, false, Util.horizontalFacings, false, HarvestTool.Wrench, DefaultDrop.Self, 4.0f, 20.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, true), 
    iridium_tank((Class<? extends TileEntityBlock>)TileEntityIridiumTank.class, 134, false, Util.horizontalFacings, false, HarvestTool.Wrench, DefaultDrop.Self, 5.0f, 100.0f, EnumRarity.COMMON, (Material)IC2Material.MACHINE, true);
    
    private static final ResourceLocation loc;
    private final Class<? extends TileEntityBlock> teClass;
    private final int itemMeta;
    private final boolean hasActive;
    private final Set<EnumFacing> supportedFacings;
    private final boolean allowWrenchRotating;
    private final HarvestTool harvestTool;
    private final DefaultDrop defaultDrop;
    private final float hardness;
    private final float explosionResistance;
    private final EnumRarity rarity;
    private final Material material;
    private final boolean transparent;
    private TileEntityBlock dummyTe;
    private ITePlaceHandler placeHandler;
    public static final TeBlock[] values;
    private static final String teIdPrefix = "ic2:";
    private static final String teClassicPrefix = "Old";
    
    private TeBlock(final Class<? extends TileEntityBlock> teClass, final int itemMeta, final boolean hasActive, final Set<EnumFacing> supportedFacings, final boolean allowWrenchRotating, final HarvestTool harvestTool, final DefaultDrop defaultDrop, final float hardness, final float explosionResistance, final EnumRarity rarity) {
        this(teClass, itemMeta, hasActive, supportedFacings, allowWrenchRotating, harvestTool, defaultDrop, hardness, explosionResistance, rarity, Material.IRON, false);
    }
    
    private TeBlock(final Class<? extends TileEntityBlock> teClass, final int itemMeta, final boolean hasActive, final Set<EnumFacing> supportedFacings, final boolean allowWrenchRotating, final HarvestTool harvestTool, final DefaultDrop defaultDrop, final float hardness, final float explosionResistance, final EnumRarity rarity, final Material material, final boolean transparent) {
        this.teClass = teClass;
        this.itemMeta = itemMeta;
        this.hasActive = hasActive;
        this.supportedFacings = supportedFacings;
        this.allowWrenchRotating = allowWrenchRotating;
        this.harvestTool = harvestTool;
        this.defaultDrop = defaultDrop;
        this.hardness = hardness;
        this.explosionResistance = explosionResistance;
        this.rarity = rarity;
        this.material = material;
        this.transparent = transparent;
    }
    
    @Override
    public boolean hasItem() {
        return this.teClass != null && this.itemMeta != -1;
    }
    
    @Override
    public String getName() {
        return this.name();
    }
    
    @Override
    public ResourceLocation getIdentifier() {
        return TeBlock.loc;
    }
    
    @Override
    public Class<? extends TileEntityBlock> getTeClass() {
        return this.teClass;
    }
    
    @Override
    public boolean hasActive() {
        return this.hasActive;
    }
    
    @Override
    public int getId() {
        return this.itemMeta;
    }
    
    @Override
    public float getHardness() {
        return this.hardness;
    }
    
    @Override
    public HarvestTool getHarvestTool() {
        return this.harvestTool;
    }
    
    @Override
    public DefaultDrop getDefaultDrop() {
        return this.defaultDrop;
    }
    
    @Override
    public float getExplosionResistance() {
        return this.explosionResistance;
    }
    
    @Override
    public boolean allowWrenchRotating() {
        return this.allowWrenchRotating;
    }
    
    @Override
    public Set<EnumFacing> getSupportedFacings() {
        return this.supportedFacings;
    }
    
    @Override
    public EnumRarity getRarity() {
        return this.rarity;
    }
    
    @Override
    public Material getMaterial() {
        return this.material;
    }
    
    @Override
    public boolean isTransparent() {
        return this.transparent;
    }
    
    @Override
    public void addSubBlocks(final NonNullList<ItemStack> list, final BlockTileEntity block, final ItemBlockTileEntity item, final CreativeTabs tab) {
        if (tab == IC2.tabIC2 || tab == CreativeTabs.SEARCH) {
            for (final TeBlock type : TeBlock.values) {
                if (type.hasItem() && Version.shouldEnable(type.teClass)) {
                    list.add((Object)block.getItemStack(type));
                    if (type.getDummyTe() instanceof IEnergyStorage) {
                        final ItemStack filled = block.getItemStack(type);
                        StackUtil.getOrCreateNbtData(filled).setDouble("energy", (double)((IEnergyStorage)type.getDummyTe()).getCapacity());
                        list.add((Object)filled);
                    }
                }
            }
        }
    }
    
    @Override
    public void setPlaceHandler(final ITePlaceHandler handler) {
        if (this.placeHandler != null) {
            throw new RuntimeException("duplicate place handler");
        }
        this.placeHandler = handler;
    }
    
    @Override
    public ITePlaceHandler getPlaceHandler() {
        return this.placeHandler;
    }
    
    public static void buildDummies() {
        final ModContainer mc = Loader.instance().activeModContainer();
        if (mc == null || !"ic2".equals(mc.getModId())) {
            throw new IllegalAccessError("Don't mess with this please.");
        }
        for (final TeBlock block : TeBlock.values) {
            if (block.teClass != null) {
                try {
                    block.dummyTe = (TileEntityBlock)block.teClass.newInstance();
                }
                catch (final Exception e) {
                    if (Util.inDev()) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    @Deprecated
    @Override
    public TileEntityBlock getDummyTe() {
        return this.dummyTe;
    }
    
    public static void registerTeMappings() {
        for (final TeBlock block : TeBlock.values) {
            if (block.teClass != null) {
                if (block.teClass.isAnnotationPresent(Delegated.class)) {
                    final Delegated delegation = block.teClass.getAnnotation(Delegated.class);
                    assert delegation != null;
                    GameRegistry.registerTileEntity((Class)delegation.current(), "ic2:" + block.getName());
                    GameRegistry.registerTileEntity((Class)delegation.old(), "ic2:Old" + block.getName());
                    TeBlockRegistry.ensureMapping(block, delegation.current());
                    TeBlockRegistry.ensureMapping(block, delegation.old());
                }
                else {
                    GameRegistry.registerTileEntity((Class)block.teClass, "ic2:" + block.getName());
                }
            }
        }
    }
    
    static {
        loc = new ResourceLocation("ic2", "te");
        values = values();
    }
    
    public enum HarvestTool
    {
        None((String)null, -1), 
        Pickaxe("pickaxe", 0), 
        Shovel("shovel", 0), 
        Axe("axe", 0), 
        Wrench("wrench", 0);
        
        public final String toolClass;
        public final int level;
        
        private HarvestTool(final String toolClass, final int level) {
            this.toolClass = toolClass;
            this.level = level;
        }
    }
    
    public enum DefaultDrop
    {
        Self, 
        None, 
        Generator, 
        Machine, 
        AdvMachine;
    }
    
    public interface ITePlaceHandler
    {
        boolean canReplace(final World p0, final BlockPos p1, final EnumFacing p2, final ItemStack p3);
    }
    
    @Inherited
    @Documented
    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Delegated {
        Class<? extends TileEntityBlock> old();
        
        Class<? extends TileEntityBlock> current();
    }
}
