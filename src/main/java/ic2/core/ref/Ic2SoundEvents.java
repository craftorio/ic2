package ic2.core.ref;

import ic2.core.IC2;
import net.minecraft.sounds.SoundEvent;

public class Ic2SoundEvents
{
	public static final SoundEvent ITEM_TREETAP_USE = register("item.treetap.use");
	public static final SoundEvent ITEM_WRENCH_USE = register("item.wrench.use");
	public static final SoundEvent ITEM_CUTTER_USE = register("item.cutter.use");
	public static final SoundEvent ITEM_PAINTER_USE = register("item.painter.use");
	public static final SoundEvent ITEM_CROWBAR_USE = register("item.crowbar.use");
	public static final SoundEvent ITEM_ELECTRIC_SHUTDOWN = register("item.electric.shutdown");
	public static final SoundEvent ITEM_BATTERY_USE = register("item.battery.use");
	public static final SoundEvent ITEM_TREETAP_ELECTRIC_USE = register("item.treetap.electric.use");
	public static final SoundEvent ITEM_CHAINSAW_IDLE = register("item.chainsaw.idle");
	public static final SoundEvent ITEM_CHAINSAW_STOP = register("item.chainsaw.stop");
	public static final SoundEvent ITEM_CHAINSAW_USE1 = register("item.chainsaw.use1");
	public static final SoundEvent ITEM_CHAINSAW_USE2 = register("item.chainsaw.use2");
	public static final SoundEvent ITEM_DRILL_IDLE = register("item.drill.idle");
	public static final SoundEvent ITEM_DRILL_HARD = register("item.drill.hard");
	public static final SoundEvent ITEM_DRILL_SOFT = register("item.drill.soft");
	public static final SoundEvent ITEM_LASER_SHOOT = register("item.laser.shoot");
	public static final SoundEvent ITEM_LASER_EXPLOSIVE = register("item.laser.explosive");
	public static final SoundEvent ITEM_LASER_LONG_RANGE = register("item.laser.long_range");
	public static final SoundEvent ITEM_LASER_LOW_FOCUS = register("item.laser.low_focus");
	public static final SoundEvent ITEM_LASER_SCATTER = register("item.laser.scatter");
	public static final SoundEvent ITEM_NANOSABER_IDLE = register("item.nanosaber.idle");
	public static final SoundEvent ITEM_NANOSABER_POWER_UP = register("item.nanosaber.power_up");
	public static final SoundEvent ITEM_NANOSABER_SWING1 = register("item.nanosaber.swing1");
	public static final SoundEvent ITEM_NANOSABER_SWING2 = register("item.nanosaber.swing2");
	public static final SoundEvent ITEM_NANOSABER_SWING3 = register("item.nanosaber.swing3");
	public static final SoundEvent ITEM_SCANNER_USE = register("item.scanner.use");
	public static final SoundEvent GENERATOR_GENERATOR_LOOP = register("generator.generator.loop");
	public static final SoundEvent GENERATOR_GEOTHERMAL_LOOP = register("generator.geothermal.loop");
	public static final SoundEvent GENERATOR_WATER_LOOP = register("generator.water.loop");
	public static final SoundEvent GENERATOR_WIND_LOOP = register("generator.wind.loop");
	public static final SoundEvent GENERATOR_NUCLEAR_LOOP = register("generator.nuclear.loop");
	public static final SoundEvent GENERATOR_NUCLEAR_LOW_POWER = register("generator.nuclear.power.low");
	public static final SoundEvent GENERATOR_NUCLEAR_MEDIUM_POWER = register("generator.nuclear.power.medium");
	public static final SoundEvent GENERATOR_NUCLEAR_HIGH_POWER = register("generator.nuclear.power.high");
	public static final SoundEvent MACHINE_OVERLOAD = register("machine.overload");
	public static final SoundEvent MACHINE_INTERRUPT1 = register("machine.interrupt1");
	public static final SoundEvent MACHINE_CANNER_OPERATE = register("machine.canner.operate");
	public static final SoundEvent MACHINE_CANNER_REVERSE = register("machine.canner.reverse");
	public static final SoundEvent MACHINE_COMPRESSOR_OPERATE = register("machine.compressor.operate");
	public static final SoundEvent MACHINE_ELECTROLYZER_LOOP = register("machine.electrolyzer.loop");
	public static final SoundEvent MACHINE_EXTRACTOR_OPERATE = register("machine.extractor.operate");
	public static final SoundEvent MACHINE_FABRICATOR_LOOP = register("machine.fabricator.loop");
	public static final SoundEvent MACHINE_FABRICATOR_SCRAP = register("machine.fabricator.scrap");
	public static final SoundEvent MACHINE_FURNACE_ELECTRIC_START = register("machine.furnace.electric.start");
	public static final SoundEvent MACHINE_FURNACE_ELECTRIC_STOP = register("machine.furnace.electric.stop");
	public static final SoundEvent MACHINE_FURNACE_ELECTRIC_LOOP = register("machine.furnace.electric.loop");
	public static final SoundEvent MACHINE_FURNACE_INDUCTION_START = register("machine.furnace.induction.start");
	public static final SoundEvent MACHINE_FURNACE_INDUCTION_STOP = register("machine.furnace.induction.stop");
	public static final SoundEvent MACHINE_FURNACE_INDUCTION_LOOP = register("machine.furnace.induction.loop");
	public static final SoundEvent MACHINE_FURNACE_IRON_OPERATE = register("machine.furnace.iron.operate");
	public static final SoundEvent MACHINE_MACERATOR_OPERATE = register("machine.macerator.operate");
	public static final SoundEvent MACHINE_MINER_OPERATE = register("machine.miner.operate");
	public static final SoundEvent MACHINE_OMAT_OPERATE = register("machine.o_mat.operate");
	public static final SoundEvent MACHINE_PUMP_OPERATE = register("machine.pump.operate");
	public static final SoundEvent MACHINE_RECYCLER_OPERATE = register("machine.recycler.operate");
	public static final SoundEvent MACHINE_TELEPORTER_USE = register("machine.teleporter.use");
	public static final SoundEvent MACHINE_TELEPORTER_CHARGE = register("machine.teleporter.charge");
	public static final SoundEvent MACHINE_TERRAFORMER_LOOP = register("machine.terraformer.loop");
	public static final SoundEvent BLOCK_NUKE_EXPLODE = register("block.nuke.explode");

	public static void init()
	{
	}

	private static SoundEvent register(String id)
	{
		return IC2.envProxy.registerSoundEvent(id);
	}
}
