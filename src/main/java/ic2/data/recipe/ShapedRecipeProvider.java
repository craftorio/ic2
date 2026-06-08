package ic2.data.recipe;

import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2Items;
import ic2.data.recipe.helper.AdvShapedRecipeGenerator;
import ic2.data.recipe.helper.Ic2RecipeProvider;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

public class ShapedRecipeProvider extends Ic2RecipeProvider
{
	public ShapedRecipeProvider(DataGenerator generator)
	{
		super(generator);
	}

	@Override
	protected void generate(Consumer<FinishedRecipe> consumer)
	{
		AdvShapedRecipeGenerator gen = new AdvShapedRecipeGenerator(consumer);
		gen.start(Ic2Items.REACTOR_VESSEL, 4, "PSP", "SPS", "PSP").key('P', Ic2ItemTags.LEAD_PLATES).key('S', Items.STONE).finish("reactor_vessel");
		gen.start(Ic2Items.REACTOR_ACCESS_HATCH, "VVV", "VHV", "VVV")
			.key('V', Ic2Items.REACTOR_VESSEL)
			.key('H', ItemTags.TRAPDOORS)
			.finish("reactor_access_hatch");
		gen.start(Ic2Items.REACTOR_FLUID_PORT, "VVV", "VCV", "VVV")
			.key('V', Ic2Items.REACTOR_VESSEL)
			.key('C', Ic2Items.EMPTY_CELL)
			.consuming()
			.finish("reactor_fluid_port");
		gen.start(Ic2Items.REACTOR_REDSTONE_PORT, "VVV", "VRV", "VVV").key('V', Ic2Items.REACTOR_VESSEL).key('R', Items.REDSTONE).finish("reactor_redstone_port");
		gen.start(Ic2Items.REINFORCED_GLASS, 7, "GAG", "GGG", "GAG").key('G', Items.GLASS).key('A', Ic2Items.ALLOY).finish("reinforced_glass");
		gen.start(Ic2Items.REINFORCED_GLASS, 7, "GGG", "AGA", "GGG").key('G', Items.GLASS).key('A', Ic2Items.ALLOY).finish("reinforced_glass_2");
		gen.start(Ic2Items.CROP_STICK, 2, "S S", "S S").key('S', Items.STICK).finish("crop_stick");
		gen.start(Ic2Items.BATBOX, "PCP", "BBB", "PPP")
			.key('P', ItemTags.PLANKS)
			.key('C', Ic2Items.INSULATED_TIN_CABLE)
			.key('B', Ic2Items.RE_BATTERY)
			.finish("batbox");
		gen.start(Ic2Items.MFE, "cCc", "CMC", "cCc")
			.key('M', Ic2Items.MACHINE)
			.key('c', Ic2Items.DOUBLE_INSULATED_GOLD_CABLE)
			.key('C', Ic2Items.ENERGY_CRYSTAL)
			.finish("mfe");
		gen.start(Ic2Items.MFSU, "LCL", "LML", "LAL")
			.key('M', Ic2Items.MFE)
			.key('A', Ic2Items.ADVANCED_MACHINE)
			.key('C', Ic2Items.ADVANCED_CIRCUIT)
			.key('L', Ic2Items.LAPOTRON_CRYSTAL)
			.finish("mfsu");
		gen.start(Ic2Items.LV_TRANSFORMER, "PCP", "PcP", "PCP")
			.key('P', ItemTags.PLANKS)
			.key('C', Ic2Items.INSULATED_TIN_CABLE)
			.key('c', Ic2Items.COIL)
			.finish("lv_transformer");
		gen.start(Ic2Items.MV_TRANSFORMER, "C", "M", "C").key('M', Ic2Items.MACHINE).key('C', Ic2Items.INSULATED_COPPER_CABLE).finish("mv_transformer");
		gen.start(Ic2Items.HV_TRANSFORMER, " c ", "CEB", " c ")
			.key('E', Ic2Items.MV_TRANSFORMER)
			.key('c', Ic2Items.DOUBLE_INSULATED_GOLD_CABLE)
			.key('B', Ic2Items.ADVANCED_RE_BATTERY)
			.key('C', Ic2Items.CIRCUIT)
			.finish("hv_transformer");
		gen.start(Ic2Items.EV_TRANSFORMER, " c ", "CED", " c ")
			.key('E', Ic2Items.HV_TRANSFORMER)
			.key('c', Ic2Items.TRIPLE_INSULATED_IRON_CABLE)
			.key('D', Ic2Items.LAPOTRON_CRYSTAL)
			.key('C', Ic2Items.ADVANCED_CIRCUIT)
			.finish("ev_transformer");
		gen.start(Ic2Items.CESU, "PCP", "BBB", "PPP")
			.key('P', Ic2ItemTags.BRONZE_PLATES)
			.key('C', Ic2Items.INSULATED_COPPER_CABLE)
			.key('B', Ic2Items.ADVANCED_RE_BATTERY)
			.finish("cesu");
		gen.start(Ic2Items.BATBOX_CHARGEPAD, "CPC", "RBR")
			.key('B', Ic2Items.BATBOX)
			.key('R', Ic2Items.RUBBER)
			.key('C', Ic2Items.CIRCUIT)
			.key('P', Items.STONE_PRESSURE_PLATE)
			.finish("batbox_chargepad");
		gen.start(Ic2Items.CESU_CHARGEPAD, "CPC", "RBR")
			.key('B', Ic2Items.CESU)
			.key('R', Ic2Items.RUBBER)
			.key('C', Ic2Items.CIRCUIT)
			.key('P', Items.STONE_PRESSURE_PLATE)
			.finish("cesu_chargepad");
		gen.start(Ic2Items.MFE_CHARGEPAD, "CPC", "RBR")
			.key('B', Ic2Items.MFE)
			.key('R', Ic2Items.RUBBER)
			.key('C', Ic2Items.CIRCUIT)
			.key('P', Items.STONE_PRESSURE_PLATE)
			.finish("mfe_chargepad");
		gen.start(Ic2Items.MFSU_CHARGEPAD, "CPC", "RBR")
			.key('B', Ic2Items.MFSU)
			.key('R', Ic2Items.RUBBER)
			.key('C', Ic2Items.CIRCUIT)
			.key('P', Items.STONE_PRESSURE_PLATE)
			.finish("mfsu_chargepad");
		gen.start(Ic2Items.GENERATOR, " B ", "III", " F ")
			.key('B', Ic2Items.RE_BATTERY)
			.key('F', Ic2Items.IRON_FURNACE)
			.key('I', Ic2ItemTags.IRON_PLATES)
			.finish("generator_from_iron_furnace");
		gen.start(Ic2Items.GENERATOR, "B", "M", "F")
			.key('B', Ic2Items.RE_BATTERY)
			.key('F', Items.FURNACE)
			.key('M', Ic2Items.MACHINE)
			.finish("generator_from_furnace");
		gen.start(Ic2Items.GEO_GENERATOR, "gCg", "gCg", "IGI")
			.key('G', Ic2Items.GENERATOR)
			.key('C', Ic2Items.EMPTY_CELL)
			.key('g', Items.GLASS)
			.key('I', Ic2Items.IRON_CASING)
			.consuming()
			.finish("geo_generator");
		gen.start(Ic2Items.WATER_GENERATOR, 2, "SPS", "PGP", "SPS")
			.key('S', Items.STICK)
			.key('P', ItemTags.PLANKS)
			.key('G', Ic2Items.GENERATOR)
			.finish("water_generator");
		gen.start(Ic2Items.SOLAR_GENERATOR, "CgC", "gCg", "cGc")
			.key('G', Ic2Items.GENERATOR)
			.key('C', Ic2ItemTags.COAL_DUSTS)
			.key('g', Items.GLASS)
			.key('c', Ic2Items.CIRCUIT)
			.finish("solar_generator");
		gen.start(Ic2Items.WIND_GENERATOR, "I I", " G ", "I I").key('I', Items.IRON_INGOT).key('G', Ic2Items.GENERATOR).finish("wind_generator");
		gen.start(Ic2Items.NUCLEAR_REACTOR, "PcP", "CCC", "PGP")
			.key('C', Ic2Items.REACTOR_CHAMBER)
			.key('c', Ic2Items.ADVANCED_CIRCUIT)
			.key('G', Ic2Items.GENERATOR)
			.key('P', Ic2Items.DENSE_LEAD_PLATE)
			.finish("nuclear_reactor");
		gen.start(Ic2Items.RCI_RSH, "ECE", "CMC", "EBE")
			.key('E', Ic2Items.EJECTOR_UPGRADE)
			.key('C', Ic2Items.RSH_CONDENSATOR)
			.key('M', Ic2Items.ADVANCED_MACHINE)
			.key('B', Ic2Items.ITEM_BUFFER)
			.finish("rci_rsh");
		gen.start(Ic2Items.RCI_LZH, "ECE", "CMC", "EBE")
			.key('E', Ic2Items.EJECTOR_UPGRADE)
			.key('C', Ic2Items.LZH_CONDENSATOR)
			.key('M', Ic2Items.ADVANCED_MACHINE)
			.key('B', Ic2Items.ITEM_BUFFER)
			.finish("rci_lzh");
		gen.start(Ic2Items.RT_GENERATOR, "III", "ICI", "IGI")
			.key('I', Ic2Items.IRON_CASING)
			.key('C', Ic2Items.REACTOR_CHAMBER)
			.key('G', Ic2Items.GENERATOR)
			.finish("rt_generator");
		gen.start(Ic2Items.SEMIFLUID_GENERATOR, "ICI", "CGC", "ICI")
			.key('G', Ic2Items.GEO_GENERATOR)
			.key('C', Ic2Items.EMPTY_CELL)
			.key('I', Ic2Items.IRON_CASING)
			.consuming()
			.finish("semifluid_generator");
		gen.start(Ic2Items.STIRLING_GENERATOR, "IHI", "ICI", "III")
			.key('C', Ic2Items.GENERATOR)
			.key('H', Ic2Items.HEAT_CONDUCTOR)
			.key('I', Ic2Items.IRON_CASING)
			.finish("stirling_generator");
		gen.start(Ic2Items.KINETIC_GENERATOR, "III", "MES", "III")
			.key('M', Ic2Items.GENERATOR)
			.key('S', Ic2Items.IRON_SHAFT)
			.key('E', Ic2Items.ELECTRIC_MOTOR)
			.key('I', Ic2Items.IRON_CASING)
			.finish("kinetic_generator");
		gen.start(Ic2Items.SOLID_HEAT_GENERATOR, " B ", "III", " F ")
			.key('B', Ic2Items.HEAT_CONDUCTOR)
			.key('F', Ic2Items.IRON_FURNACE)
			.key('I', Ic2ItemTags.IRON_PLATES)
			.finish("solid_heat_generator_from_iron_furnace");
		gen.start(Ic2Items.SOLID_HEAT_GENERATOR, "B", "M", "F")
			.key('B', Ic2Items.HEAT_CONDUCTOR)
			.key('F', Items.FURNACE)
			.key('M', Ic2Items.MACHINE)
			.finish("solid_heat_generator_from_furnace");
		gen.start(Ic2Items.FLUID_HEAT_GENERATOR, "ICI", "CGC", "ICI")
			.key('G', Ic2Items.HEAT_CONDUCTOR)
			.key('C', Ic2Items.EMPTY_CELL)
			.key('I', Ic2Items.IRON_CASING)
			.consuming()
			.finish("fluid_heat_generator");
		gen.start(Ic2Items.RT_HEAT_GENERATOR, "III", "ICI", "IGI")
			.key('I', Ic2Items.IRON_CASING)
			.key('C', Ic2Items.REACTOR_CHAMBER)
			.key('G', Ic2Items.HEAT_CONDUCTOR)
			.finish("rt_heat_generator");
		gen.start(Ic2Items.ELECTRIC_HEAT_GENERATOR, "IBI", "ICI", "IGI")
			.key('I', Ic2Items.IRON_CASING)
			.key('B', Ic2Items.RE_BATTERY)
			.key('G', Ic2Items.HEAT_CONDUCTOR)
			.key('C', Ic2Items.CIRCUIT)
			.finish("electric_heat_generator");
		gen.start(Ic2Items.WIND_KINETIC_GENERATOR, "SMS").key('M', Ic2Items.MACHINE).key('S', Ic2Items.IRON_SHAFT).finish("wind_kinetic_generator");
		gen.start(Ic2Items.STEAM_KINETIC_GENERATOR, "CCC", "BSS", "ECC")
			.key('S', Ic2Items.IRON_SHAFT)
			.key('B', Ic2Items.COPPER_BOILER)
			.key('C', Ic2Items.STEEL_CASING)
			.key('E', Ic2Items.EMPTY_CELL)
			.consuming()
			.finish("steam_kinetic_generator");
		gen.start(Ic2Items.ELECTRIC_KINETIC_GENERATOR, "IBI", "ISI", "IMI")
			.key('I', Ic2Items.IRON_CASING)
			.key('B', Ic2Items.RE_BATTERY)
			.key('M', Ic2Items.ELECTRIC_MOTOR)
			.key('S', Ic2Items.IRON_SHAFT)
			.finish("electric_kinetic_generator");
		gen.start(Ic2Items.WATER_KINETIC_GENERATOR, "S S", " M ", "S S")
			.key('S', Ic2Items.IRON_SHAFT)
			.key('M', Ic2Items.MACHINE)
			.finish("water_kinetic_generator");
		gen.start(Ic2Items.STIRLING_KINETIC_GENERATOR, "GPG", "GMG", "GPG")
			.key('G', Items.GLASS_BOTTLE)
			.key('P', Items.PISTON)
			.key('M', Ic2Items.MACHINE)
			.finish("stirling_kinetic_generator");
		gen.start(Ic2Items.ITNT, 4, "FFF", "TTT", "FFF").key('F', Items.FLINT).key('T', Items.TNT).finish("itnt");
		gen.start(Ic2Items.ITNT, 4, "FTF", "FTF", "FTF").key('F', Items.FLINT).key('T', Items.TNT).finish("itnt_vertical");
		gen.start(Ic2Items.IRON_SCAFFOLD, 16, "PPP", "sss", "PPP").key('P', Ic2ItemTags.IRON_PLATES).key('s', Ic2Items.IRON_FENCE).finish("iron_scaffold");
		gen.start(Ic2Items.MACHINE, "III", "I I", "III").key('I', Ic2ItemTags.IRON_PLATES).finish("machine");
		gen.start(Ic2Items.TESLA_COIL, "RRR", "RMR", "ICI")
			.key('M', Ic2Items.MV_TRANSFORMER)
			.key('R', Items.REDSTONE)
			.key('C', Ic2Items.CIRCUIT)
			.key('I', Ic2Items.IRON_CASING)
			.finish("tesla_coil");
		gen.start(Ic2Items.FLUID_BOTTLER, " T ", " T ", "CMC")
			.key('T', Ic2Items.EMPTY_CELL)
			.key('M', Ic2Items.MACHINE)
			.key('C', Ic2Items.CIRCUIT)
			.consuming()
			.finish("fluid_bottler");
		gen.start(Ic2Items.ADVANCED_MINER, "CBC", "MAT", "CBC")
			.key('A', Ic2Items.ADVANCED_MACHINE)
			.key('B', Ic2Items.MINER)
			.key('C', Ic2Items.ALLOY)
			.key('T', Ic2Items.TELEPORTER)
			.key('M', Ic2Items.MFE)
			.finish("advanced_miner");
		gen.start(Ic2Items.LIQUID_HEAT_EXCHANGER, "gCg", "gCg", "IGI")
			.key('G', Ic2Items.HEAT_CONDUCTOR)
			.key('C', Ic2Items.EMPTY_CELL)
			.key('g', Items.GLASS)
			.key('I', Ic2Items.IRON_CASING)
			.consuming()
			.finish("liquid_heat_exchanger");
		gen.start(Ic2Items.FERMENTER, "III", "CCC", "IGI")
			.key('C', Ic2Items.EMPTY_CELL)
			.key('G', Ic2Items.HEAT_CONDUCTOR)
			.key('I', Ic2Items.IRON_CASING)
			.consuming()
			.finish("fermenter");
		gen.start(Ic2Items.FLUID_REGULATOR, "III", "CGC", "IBI")
			.key('I', Ic2Items.IRON_CASING)
			.key('C', Ic2Items.EMPTY_CELL)
			.key('G', Ic2Items.ELECTRIC_MOTOR)
			.key('B', Ic2Items.CIRCUIT)
			.consuming()
			.finish("fluid_regulator");
		gen.start(Ic2Items.CONDENSER, "CIC", "CAC", "IBI")
			.key('C', Ic2Items.EMPTY_CELL)
			.key('I', Ic2Items.IRON_CASING)
			.key('B', Ic2Items.CIRCUIT)
			.key('A', Ic2Items.MACHINE)
			.consuming()
			.finish("condenser");
		gen.start(Ic2Items.STEAM_GENERATOR, "III", "IBI", "IGI")
			.key('G', Ic2Items.HEAT_CONDUCTOR)
			.key('B', Ic2Items.COPPER_BOILER)
			.key('I', Ic2Items.IRON_CASING)
			.finish("steam_generator");
		gen.start(Ic2Items.BLAST_FURNACE, "III", "IBI", "IGI")
			.key('G', Ic2Items.HEAT_CONDUCTOR)
			.key('B', Ic2Items.MACHINE)
			.key('I', Ic2Items.IRON_CASING)
			.finish("blast_furnace");
		gen.start(Ic2Items.BLOCK_CUTTER, "C", "B", "M")
			.key('B', Ic2Items.MACHINE)
			.key('M', Ic2Items.ELECTRIC_MOTOR)
			.key('C', Ic2Items.CIRCUIT)
			.finish("block_cutter");
		gen.start(Ic2Items.SOLAR_DISTILLER, "GGG", "G G", "CMC")
			.key('C', Ic2Items.EMPTY_CELL)
			.key('M', Ic2Items.MACHINE)
			.key('G', Items.GLASS)
			.consuming()
			.finish("solar_distiller");
		gen.start(Ic2Items.FLUID_DISTRIBUTOR, "UUU", "UMU", "CCC")
			.key('M', Ic2Items.MACHINE)
			.key('U', Ic2Items.FLUID_EJECTOR_UPGRADE)
			.key('C', Ic2Items.EMPTY_CELL)
			.consuming()
			.finish("fluid_distributor");
		gen.start(Ic2Items.SORTING_MACHINE, "UBU", "UMU", "UCU")
			.key('M', Ic2Items.MACHINE)
			.key('U', Ic2Items.EJECTOR_UPGRADE)
			.key('B', Ic2Items.CIRCUIT)
			.key('C', Ic2ItemTags.WOODEN_CHESTS)
			.finish("sorting_machine");
		gen.start(Ic2Items.ITEM_BUFFER, "III", "CMC", "III")
			.key('M', Ic2Items.MACHINE)
			.key('C', Ic2ItemTags.WOODEN_CHESTS)
			.key('I', Ic2Items.IRON_CASING)
			.finish("item_buffer");
		gen.start(Ic2Items.CROPMATRON, "cBc", "UMU", "CCC")
			.key('M', Ic2Items.MACHINE)
			.key('C', Ic2Items.CROP_STICK)
			.key('c', Ic2Items.CIRCUIT)
			.key('B', Ic2ItemTags.WOODEN_CHESTS)
			.key('U', Ic2Items.EMPTY_CELL)
			.consuming()
			.finish("cropmatron");
		gen.start(Ic2Items.METAL_FORMER, " E ", "TMT", "KKK")
			.key('E', Ic2Items.CIRCUIT)
			.key('T', Ic2Items.TOOL_BOX)
			.key('M', Ic2Items.MACHINE)
			.key('K', Ic2Items.COIL)
			.finish("metal_former");
		gen.start(Ic2Items.ORE_WASHING_PLANT, "III", "BAB", "EWE")
			.key('A', Ic2Items.MACHINE)
			.key('E', Ic2Items.ELECTRIC_MOTOR)
			.key('B', Items.BUCKET)
			.key('W', Ic2Items.CIRCUIT)
			.key('I', Ic2ItemTags.IRON_PLATES)
			.finish("ore_washing_plant");
		gen.start(Ic2Items.REPLICATOR, "SGS", "TTT", "VFV")
			.key('T', Ic2Items.TELEPORTER)
			.key('S', Ic2Items.REINFORCED_STONE)
			.key('G', Ic2Items.REINFORCED_GLASS)
			.key('F', Ic2Items.MFE)
			.key('V', Ic2Items.HV_TRANSFORMER)
			.finish("replicator");
		gen.start(Ic2Items.SOLID_CANNER, " T ", " T ", "CMC")
			.key('T', Ic2Items.TIN_CAN)
			.key('M', Ic2Items.MACHINE)
			.key('C', Ic2Items.CIRCUIT)
			.finish("solid_canner");
		gen.start(Ic2Items.IRON_FURNACE, " I ", "I I", "IFI").key('I', Ic2ItemTags.IRON_PLATES).key('F', Items.FURNACE).finish("iron_furnace");
		gen.start(Ic2Items.ELECTROLYZER, "c c", "cCc", "EME")
			.key('E', Ic2Items.EMPTY_CELL)
			.key('c', Ic2Items.INSULATED_COPPER_CABLE)
			.key('M', Ic2Items.MACHINE)
			.key('C', Ic2Items.CIRCUIT)
			.consuming()
			.finish("electrolyzer");
		gen.start(Ic2Items.RECYCLER, " G ", "DMD", "IDI")
			.key('D', Items.DIRT)
			.key('G', Items.GLOWSTONE_DUST)
			.key('M', Ic2Items.COMPRESSOR)
			.key('I', Items.IRON_INGOT)
			.finish("recycler");
		gen.start(Ic2Items.ADVANCED_MACHINE, "SCS", "AMA", "SCS")
			.key('M', Ic2Items.MACHINE)
			.key('A', Ic2Items.ALLOY)
			.key('S', Ic2ItemTags.STEEL_PLATES)
			.key('C', Ic2Items.CARBON_PLATE)
			.finish("advanced_machine");
		gen.start(Ic2Items.ADVANCED_MACHINE, "SAS", "CMC", "SAS")
			.key('M', Ic2Items.MACHINE)
			.key('A', Ic2Items.ALLOY)
			.key('S', Ic2ItemTags.STEEL_PLATES)
			.key('C', Ic2Items.CARBON_PLATE)
			.finish("advanced_machine_vertical");
		gen.start(Ic2Items.INDUCTION_FURNACE, "CCC", "CFC", "CMC")
			.key('C', Items.COPPER_INGOT)
			.key('F', Ic2Items.ELECTRIC_FURNACE)
			.key('M', Ic2Items.ADVANCED_MACHINE)
			.finish("induction_furnace");
		gen.start(Ic2Items.MATTER_GENERATOR, "GCG", "ALA", "GCG")
			.key('A', Ic2Items.ADVANCED_MACHINE)
			.key('L', Ic2Items.LAPOTRON_CRYSTAL)
			.key('G', Items.GLOWSTONE_DUST)
			.key('C', Ic2Items.ADVANCED_CIRCUIT)
			.finish("matter_generator");
		gen.start(Ic2Items.TERRAFORMER, "GTG", "DMD", "GDG")
			.key('T', Ic2Items.BLANK_TFBP)
			.key('G', Items.GLOWSTONE_DUST)
			.key('D', Items.DIRT)
			.key('M', Ic2Items.ADVANCED_MACHINE)
			.finish("terraformer");
		gen.start(Ic2Items.ELECTRIC_FURNACE, " C ", "RFR")
			.key('C', Ic2Items.CIRCUIT)
			.key('R', Items.REDSTONE)
			.key('F', Ic2Items.IRON_FURNACE)
			.finish("electric_furnace");
		gen.start(Ic2Items.MACERATOR, "FFF", "SMS", " C ")
			.key('F', Items.FLINT)
			.key('S', Items.COBBLESTONE)
			.key('M', Ic2Items.MACHINE)
			.key('C', Ic2Items.CIRCUIT)
			.finish("macerator");
		gen.start(Ic2Items.EXTRACTOR, "TMT", "TCT").key('T', Ic2Items.TREETAP).key('M', Ic2Items.MACHINE).key('C', Ic2Items.CIRCUIT).finish("extractor");
		gen.start(Ic2Items.COMPRESSOR, "S S", "SMS", "SCS").key('S', Items.STONE).key('M', Ic2Items.MACHINE).key('C', Ic2Items.CIRCUIT).finish("compressor");
		gen.start(Ic2Items.CANNER, "TCT", "TMT", "TTT").key('T', Ic2Items.TIN_CASING).key('M', Ic2Items.MACHINE).key('C', Ic2Items.CIRCUIT).finish("canner");
		gen.start(Ic2Items.MINER, " X ", "CMC", " P ")
			.key('P', Ic2Items.MINING_PIPE)
			.key('M', Ic2Items.MACHINE)
			.key('C', Ic2Items.CIRCUIT)
			.key('X', Items.CHEST)
			.finish("miner");
		gen.start(Ic2Items.PUMP, "cCc", "cMc", "PTP")
			.key('c', Ic2Items.EMPTY_CELL)
			.key('T', Ic2Items.TREETAP)
			.key('P', Ic2Items.MINING_PIPE)
			.key('M', Ic2Items.MACHINE)
			.key('C', Ic2Items.CIRCUIT)
			.consuming()
			.finish("pump");
		gen.start(Ic2Items.MAGNETIZER, "RFR", "RMR", "RFR")
			.key('R', Items.REDSTONE)
			.key('F', Ic2Items.IRON_FENCE)
			.key('M', Ic2Items.MACHINE)
			.finish("magnetizer");
		gen.start(Ic2Items.TIN_BLOCK, "MMM", "MMM", "MMM").key('M', Ic2ItemTags.TIN_INGOTS).finish("tin_block");
		gen.start(Ic2Items.BRONZE_BLOCK, "MMM", "MMM", "MMM").key('M', Ic2ItemTags.BRONZE_INGOTS).finish("bronze_block");
		gen.start(Ic2Items.URANIUM_BLOCK, "UUU", "UUU", "UUU").key('U', Ic2Items.URANIUM_238).group("uranium_block").finish("uranium_block");
		gen.start(Ic2Items.URANIUM_BLOCK, "UUU", "UUU", "UUU").key('U', Ic2ItemTags.URANIUM_INGOTS).group("uranium_block").finish("uranium_block_tagged");
		gen.start(Ic2Items.LEAD_BLOCK, "MMM", "MMM", "MMM").key('M', Ic2ItemTags.LEAD_INGOTS).finish("lead_block");
		gen.start(Ic2Items.SILVER_BLOCK, "MMM", "MMM", "MMM").key('M', Ic2ItemTags.SILVER_INGOTS).finish("silver_block");
		gen.start(Ic2Items.STEEL_BLOCK, "MMM", "MMM", "MMM").key('M', Ic2ItemTags.STEEL_INGOTS).finish("steel_block");
		gen.start(Ic2Items.MINING_PIPE, 16, "I I", "I I", "ITI").key('I', Ic2ItemTags.IRON_PLATES).key('T', Ic2Items.TREETAP).finish("mining_pipe");
		gen.start(Ic2Items.NUKE, "RCR", "RMR", "RCR")
			.key('R', Ic2Items.THICK_NEUTRON_REFLECTOR)
			.key('C', Ic2Items.ADVANCED_CIRCUIT)
			.key('M', Ic2Items.ADVANCED_MACHINE)
			.hidden()
			.finish("nuke");
		gen.start(Ic2Items.PERSONAL_CHEST, "c", "M", "C")
			.key('c', Ic2Items.CIRCUIT)
			.key('C', Ic2ItemTags.WOODEN_CHESTS)
			.key('M', Ic2Items.MACHINE)
			.finish("personal_chest");
		gen.start(Ic2Items.TRADE_O_MAT, "RRR", "CMC")
			.key('R', Items.REDSTONE)
			.key('C', Ic2ItemTags.WOODEN_CHESTS)
			.key('M', Ic2Items.MACHINE)
			.finish("trade_o_mat");
		gen.start(Ic2Items.ENERGY_O_MAT, "RBR", "CMC")
			.key('R', Items.REDSTONE)
			.key('C', Ic2Items.INSULATED_COPPER_CABLE)
			.key('M', Ic2Items.MACHINE)
			.key('B', Ic2Items.RE_BATTERY)
			.finish("energy_o_mat");
		gen.start(Ic2Items.REACTOR_CHAMBER, " I ", "ICI", " I ").key('I', Ic2ItemTags.LEAD_PLATES).key('C', Ic2Items.MACHINE).finish("reactor_chamber");
		gen.start(Ic2Items.RUBBER_SHEET, 3, "RRR", "RRR").key('R', Ic2Items.RUBBER).finish("rubber_sheet");
		gen.start(Ic2Items.RESIN_SHEET, 3, "RRR", "RRR").key('R', Ic2Items.RESIN).finish("resin_sheet");
		gen.start(Ic2Items.WOOL_SHEET, "WRW").key('W', ItemTags.WOOL_CARPETS).key('R', Ic2Items.RESIN_SHEET).finish("wool_sheet");
		gen.start(Ic2Items.WOODEN_SCAFFOLD, 4, "PPP", " s ", "s s").key('P', ItemTags.PLANKS).key('s', Items.STICK).finish("wooden_scaffold");
		gen.start(Ic2Items.ADVANCED_RE_BATTERY, "CTC", "TST", "TLT")
			.key('T', Ic2Items.BRONZE_CASING)
			.key('S', Ic2ItemTags.SULFUR_DUSTS)
			.key('L', Ic2ItemTags.LEAD_DUSTS)
			.key('C', Ic2Items.INSULATED_COPPER_CABLE)
			.finish("advanced_re_battery");
		gen.start(Ic2Items.ALLOY_CHESTPLATE, "A A", "ALA", "AIA")
			.key('L', Items.LEATHER_CHESTPLATE)
			.key('I', Items.IRON_CHESTPLATE)
			.key('A', Ic2Items.ALLOY)
			.finish("alloy_chestplate");
		gen.start(Ic2Items.ALLOY_CHESTPLATE, "A A", "AIA", "ALA")
			.key('L', Items.LEATHER_CHESTPLATE)
			.key('I', Items.IRON_CHESTPLATE)
			.key('A', Ic2Items.ALLOY)
			.finish("alloy_chestplate_2");
		gen.start(Ic2Items.BRONZE_BOOTS, "B B", "B B").key('B', Ic2ItemTags.BRONZE_INGOTS).finish("bronze_boots");
		gen.start(Ic2Items.BRONZE_CHESTPLATE, "B B", "BBB", "BBB").key('B', Ic2ItemTags.BRONZE_INGOTS).finish("bronze_chestplate");
		gen.start(Ic2Items.BRONZE_HELMET, "BBB", "B B").key('B', Ic2ItemTags.BRONZE_INGOTS).finish("bronze_helmet");
		gen.start(Ic2Items.BRONZE_LEGGINGS, "BBB", "B B", "B B").key('B', Ic2ItemTags.BRONZE_INGOTS).finish("bronze_leggings");
		gen.start(Ic2Items.HAZMAT_CHESTPLATE, "R R", "ROR", "ROR").key('R', Ic2Items.RUBBER).key('O', Items.ORANGE_DYE).finish("hazmat_chestplate");
		gen.start(Ic2Items.HAZMAT_HELMET, " O ", "RGR", "R#R")
			.key('R', Ic2Items.RUBBER)
			.key('G', Items.GLASS)
			.key('#', Items.IRON_BARS)
			.key('O', Items.ORANGE_DYE)
			.finish("hazmat_helmet");
		gen.start(Ic2Items.HAZMAT_LEGGINGS, "ROR", "R R", "R R").key('R', Ic2Items.RUBBER).key('O', Items.ORANGE_DYE).finish("hazmat_leggings");
		gen.start(Ic2Items.JETPACK, "ICI", "IFI", "R R")
			.key('I', Ic2Items.IRON_CASING)
			.key('C', Ic2Items.CIRCUIT)
			.key('F', Ic2Items.EMPTY_CELL)
			.key('R', Items.REDSTONE)
			.consuming()
			.finish("jetpack");
		gen.start(Ic2Items.NANO_BOOTS, "C C", "CcC").key('C', Ic2Items.CARBON_PLATE).key('c', Ic2Items.ENERGY_CRYSTAL).finish("nano_boots");
		gen.start(Ic2Items.NANO_CHESTPLATE, "C C", "CcC", "CCC").key('C', Ic2Items.CARBON_PLATE).key('c', Ic2Items.ENERGY_CRYSTAL).finish("nano_chestplate");
		gen.start(Ic2Items.NANO_LEGGINGS, "CcC", "C C", "C C").key('C', Ic2Items.CARBON_PLATE).key('c', Ic2Items.ENERGY_CRYSTAL).finish("nano_leggings");
		gen.start(Ic2Items.QUANTUM_BOOTS, "InI", "RLR")
			.key('n', Ic2Items.NANO_BOOTS)
			.key('I', Ic2Items.IRIDIUM)
			.key('L', Ic2Items.LAPOTRON_CRYSTAL)
			.key('R', Ic2Items.RUBBER_BOOTS)
			.finish("quantum_boots");
		gen.start(Ic2Items.QUANTUM_HELMET, "GnG", "ILI", "CNC")
			.key('N', Ic2Items.HAZMAT_HELMET)
			.key('n', Ic2Items.NANO_HELMET)
			.key('I', Ic2Items.IRIDIUM)
			.key('L', Ic2Items.LAPOTRON_CRYSTAL)
			.key('G', Ic2Items.REINFORCED_GLASS)
			.key('C', Ic2Items.ADVANCED_CIRCUIT)
			.finish("quantum_helmet");
		gen.start(Ic2Items.QUANTUM_LEGGINGS, "MLM", "InI", "G G")
			.key('n', Ic2Items.NANO_LEGGINGS)
			.key('I', Ic2Items.IRIDIUM)
			.key('L', Ic2Items.LAPOTRON_CRYSTAL)
			.key('G', Items.GLOWSTONE_DUST)
			.key('M', Ic2Items.MACHINE)
			.finish("quantum_leggings");
		gen.start(Ic2Items.RUBBER_BOOTS, "R R", "R R", "RWR").key('R', Ic2Items.RUBBER).key('W', Items.WHITE_WOOL).finish("rubber_boots");
		gen.start(Ic2Items.LAPOTRON_CRYSTAL, "LCL", "LDL", "LCL")
			.key('D', Ic2Items.ENERGY_CRYSTAL)
			.key('C', Ic2Items.ADVANCED_CIRCUIT)
			.key('L', Ic2ItemTags.LAPIS_DUSTS)
			.finish("lapotron_crystal");
		gen.start(Ic2Items.RE_BATTERY, " C ", "TRT", "TRT")
			.key('T', Ic2Items.TIN_CASING)
			.key('R', Items.REDSTONE)
			.key('C', Ic2Items.INSULATED_TIN_CABLE)
			.finish("re_battery");
		gen.start(Ic2Items.SINGLE_USE_BATTERY, 5, "C", "R", "D")
			.key('D', Ic2ItemTags.COAL_DUSTS)
			.key('R', Items.REDSTONE)
			.key('C', Ic2Items.INSULATED_COPPER_CABLE)
			.finish("single_use_battery");
		gen.start(Ic2Items.SINGLE_USE_BATTERY, 5, "C", "D", "R")
			.key('D', Ic2ItemTags.COAL_DUSTS)
			.key('R', Items.REDSTONE)
			.key('C', Ic2Items.INSULATED_COPPER_CABLE)
			.finish("single_use_battery_2");
		gen.start(Ic2Items.DETECTOR_CABLE, " C ", "RIR", " R ")
			.key('R', Items.REDSTONE)
			.key('I', Ic2Items.TRIPLE_INSULATED_IRON_CABLE)
			.key('C', Ic2Items.CIRCUIT)
			.finish("detector_cable");
		gen.start(Ic2Items.SPLITTER_CABLE, " R ", "ILI", " R ")
			.key('R', Items.REDSTONE)
			.key('I', Ic2Items.TRIPLE_INSULATED_IRON_CABLE)
			.key('L', Items.LEVER)
			.finish("splitter_cable");
		gen.start(Ic2Items.GLASS_FIBRE_CABLE, 6, "GGG", "DRD", "GGG")
			.key('G', Items.GLASS)
			.key('R', Ic2ItemTags.SILVER_DUSTS)
			.key('D', Ic2Items.ENERGIUM_DUST)
			.finish("glass_fibre_cable");
		gen.start(Ic2Items.REINFORCED_DOOR, "ILI", "ILI", "ILI").key('I', Ic2ItemTags.IRON_PLATES).key('L', Ic2ItemTags.LEAD_PLATES).finish("reinforced_door");
		gen.start(Ic2Items.ENERGIUM_DUST, 9, "RDR", "DRD", "RDR").key('D', Ic2ItemTags.DIAMOND_DUSTS).key('R', Items.REDSTONE).finish("energium_dust");
		gen.start(Ic2Items.LEAD_DUST, "XXX", "XXX", "XXX").key('X', Ic2Items.SMALL_LEAD_DUST).finish("lead_dust");
		gen.start(Ic2Items.SULFUR_DUST, "XXX", "XXX", "XXX").key('X', Ic2Items.SMALL_SULFUR_DUST).finish("sulfur_dust");
		gen.start(Ic2Items.COPPER_DUST, "XXX", "XXX", "XXX").key('X', Ic2Items.SMALL_COPPER_DUST).finish("copper_dust");
		gen.start(Ic2Items.GOLD_DUST, "XXX", "XXX", "XXX").key('X', Ic2Items.SMALL_GOLD_DUST).finish("gold_dust");
		gen.start(Ic2Items.IRON_DUST, "XXX", "XXX", "XXX").key('X', Ic2Items.SMALL_IRON_DUST).finish("iron_dust");
		gen.start(Ic2Items.SILVER_DUST, "XXX", "XXX", "XXX").key('X', Ic2Items.SMALL_SILVER_DUST).finish("silver_dust");
		gen.start(Ic2Items.TIN_DUST, "XXX", "XXX", "XXX").key('X', Ic2Items.SMALL_TIN_DUST).finish("tin_dust");
		gen.start(Ic2Items.BRONZE_DUST, "XXX", "XXX", "XXX").key('X', Ic2Items.SMALL_BRONZE_DUST).finish("bronze_dust");
		gen.start(Ic2Items.LAPIS_DUST, "XXX", "XXX", "XXX").key('X', Ic2Items.SMALL_LAPIS_DUST).finish("lapis_dust");
		gen.start(Ic2Items.OBSIDIAN_DUST, "XXX", "XXX", "XXX").key('X', Ic2Items.SMALL_OBSIDIAN_DUST).finish("obsidian_dust");
		gen.start(Ic2Items.LITHIUM_DUST, "XXX", "XXX", "XXX").key('X', Ic2Items.SMALL_LITHIUM_DUST).finish("lithium_dust");
		gen.start(Ic2Items.COAL_FUEL_DUST, 8, "CCC", "CWC", "CCC").key('C', Ic2ItemTags.COAL_DUSTS).key('W', Fluids.WATER, 1000).finish("coal_fuel_dust");
		gen.start(Ic2Items.SMALL_IRON_DUST, "CTC", "TCT", "CTC")
			.key('C', Ic2ItemTags.COPPER_DUSTS)
			.key('T', Ic2ItemTags.TIN_DUSTS)
			.group("small_iron_dust")
			.hidden()
			.finish("small_iron_dust");
		gen.start(Ic2Items.SMALL_IRON_DUST, "TCT", "CTC", "TCT")
			.key('C', Ic2ItemTags.COPPER_DUSTS)
			.key('T', Ic2ItemTags.TIN_DUSTS)
			.group("small_iron_dust")
			.hidden()
			.finish("small_iron_dust_2");
		gen.start(Ic2Items.EMPTY_CELL, " C ", "CGC", " C ").key('C', Ic2Items.TIN_CASING).key('G', Items.GLASS_PANE).finish("empty_cell");
		gen.start(Ic2Items.PLANT_BALL, "PPP", "P P", "PPP").key('P', Items.WHEAT).group("plant_ball").finish("plant_ball_1");
		gen.start(Ic2Items.PLANT_BALL, "PPP", "P P", "PPP").key('P', Items.SUGAR_CANE).group("plant_ball").finish("plant_ball_2");
		gen.start(Ic2Items.PLANT_BALL, "PPP", "P P", "PPP").key('P', Items.CACTUS).group("plant_ball").finish("plant_ball_3");
		gen.start(Ic2Items.PLANT_BALL, "PPP", "P P", "PPP").key('P', ItemTags.LEAVES).group("plant_ball").finish("plant_ball_4");
		gen.start(Ic2Items.PLANT_BALL, "PPP", "P P", "PPP").key('P', ItemTags.SAPLINGS).group("plant_ball").finish("plant_ball_5");
		gen.start(Ic2Items.PLANT_BALL, "PPP", "P P", "PPP").key('P', Items.TALL_GRASS).group("plant_ball").finish("plant_ball_6");
		gen.start(Ic2Items.PLANT_BALL, "PPP", "P P", "PPP").key('P', Items.WHEAT_SEEDS).group("plant_ball").finish("plant_ball_7");
		gen.start(Ic2Items.TIN_INGOT, 9, "B").key('B', Ic2Items.TIN_BLOCK).finish("tin_ingot");
		gen.start(Ic2Items.BRONZE_INGOT, 9, "B").key('B', Ic2Items.BRONZE_BLOCK).finish("bronze_ingot");
		gen.start(Ic2Items.MIXED_METAL_INGOT, 2, "III", "BBB", "TTT")
			.key('I', Ic2ItemTags.IRON_PLATES)
			.key('B', Ic2ItemTags.BRONZE_PLATES)
			.key('T', Ic2ItemTags.TIN_PLATES)
			.finish("mixed_metal_ingot");
		gen.start(Ic2Items.LEAD_INGOT, 9, "B").key('B', Ic2Items.LEAD_BLOCK).finish("lead_ingot");
		gen.start(Ic2Items.SILVER_INGOT, 9, "B").key('B', Ic2Items.SILVER_BLOCK).finish("silver_ingot");
		gen.start(Ic2Items.STEEL_INGOT, 9, "B").key('B', Ic2Items.STEEL_BLOCK).finish("steel_ingot");
		gen.start(Ic2Items.MOX, "UUU", "CCC", "UUU").key('U', Ic2Items.URANIUM_238).key('C', Ic2Items.PLUTONIUM).group("mox").finish("mox_1");
		gen.start(Ic2Items.MOX, "UUU", "CCC", "UUU").key('U', Ic2Items.URANIUM_238).key('C', Ic2ItemTags.PLUTONIUM_INGOTS).group("mox").finish("mox_2");
		gen.start(Ic2Items.MOX, "UUU", "CCC", "UUU").key('U', Ic2ItemTags.URANIUM_INGOTS).key('C', Ic2Items.PLUTONIUM).group("mox").finish("mox_3");
		gen.start(Ic2Items.MOX, "UUU", "CCC", "UUU").key('U', Ic2ItemTags.URANIUM_INGOTS).key('C', Ic2ItemTags.PLUTONIUM_INGOTS).group("mox").finish("mox_4");
		gen.start(Ic2Items.EMPTY_MUG, "SS ", "SSS", "SS ").key('S', Items.STONE).finish("empty_mug");
		gen.start(Ic2Items.NANO_SABER, "GA ", "GA ", "CcC")
			.key('C', Ic2Items.CARBON_PLATE)
			.key('c', Ic2Items.ENERGY_CRYSTAL)
			.key('G', Items.GLOWSTONE_DUST)
			.key('A', Ic2Items.ALLOY)
			.finish("nano_saber");
		gen.start(Ic2Items.CF_POWDER, "SAS", "SCS", "SAS")
			.key('A', Items.SAND)
			.key('C', Items.CLAY_BALL)
			.key('S', Ic2ItemTags.STONE_DUSTS)
			.finish("cf_powder");
		gen.start(Ic2Items.CARBON_FIBRE, "CC", "CC").key('C', Ic2ItemTags.COAL_DUSTS).finish("carbon_fibre");
		gen.start(Ic2Items.CIRCUIT, "CCC", "RIR", "CCC")
			.key('I', Ic2ItemTags.IRON_PLATES)
			.key('R', Items.REDSTONE)
			.key('C', Ic2Items.INSULATED_COPPER_CABLE)
			.group("circuit")
			.finish("circuit");
		gen.start(Ic2Items.CIRCUIT, "CRC", "CIC", "CRC")
			.key('I', Ic2ItemTags.IRON_PLATES)
			.key('R', Items.REDSTONE)
			.key('C', Ic2Items.INSULATED_COPPER_CABLE)
			.group("circuit")
			.finish("circuit_vertical");
		gen.start(Ic2Items.ADVANCED_CIRCUIT, "RGR", "LCL", "RGR")
			.key('L', Items.LAPIS_LAZULI)
			.key('G', Items.GLOWSTONE_DUST)
			.key('R', Items.REDSTONE)
			.key('C', Ic2Items.CIRCUIT)
			.group("advanced_circuit")
			.finish("advanced_circuit");
		gen.start(Ic2Items.ADVANCED_CIRCUIT, "RLR", "GCG", "RLR")
			.key('L', Items.LAPIS_LAZULI)
			.key('G', Items.GLOWSTONE_DUST)
			.key('R', Items.REDSTONE)
			.key('C', Ic2Items.CIRCUIT)
			.group("advanced_circuit")
			.finish("advanced_circuit_vertical");
		gen.start(Ic2Items.COAL_BALL, "CCC", "CFC", "CCC").key('C', Ic2ItemTags.COAL_DUSTS).key('F', Items.FLINT).finish("coal_ball");
		gen.start(Ic2Items.COAL_CHUNK, "###", "#O#", "###")
			.key('#', Ic2Items.COAL_BLOCK)
			.key('O', itemInput(Items.OBSIDIAN, Items.IRON_BLOCK, Items.BRICKS))
			.finish("coal_chunk");
		gen.start(Ic2Items.IRIDIUM, "IAI", "ADA", "IAI").key('I', Ic2Items.IRIDIUM_ORE).key('A', Ic2Items.ALLOY).key('D', Ic2ItemTags.DIAMONDS).finish("iridium");
		gen.start(Ic2Items.PLUTONIUM, "UUU", "UUU", "UUU").key('U', Ic2Items.SMALL_PLUTONIUM).finish("plutonium");
		gen.start(Ic2Items.RTG_PELLET, "RAR", "RAR", "RAR")
			.key('R', Ic2Items.DENSE_IRON_PLATE)
			.key('A', Ic2Items.PLUTONIUM)
			.group("rtg_pellet")
			.finish("rtg_pellet");
		gen.start(Ic2Items.RTG_PELLET, "RRR", "AAA", "RRR")
			.key('R', Ic2Items.DENSE_IRON_PLATE)
			.key('A', Ic2Items.PLUTONIUM)
			.group("rtg_pellet")
			.finish("rtg_pellet_vertical");
		gen.start(Ic2Items.RTG_PELLET, "RAR", "RAR", "RAR")
			.key('R', Ic2Items.DENSE_IRON_PLATE)
			.key('A', Ic2ItemTags.PLUTONIUM_INGOTS)
			.group("rtg_pellet")
			.finish("rtg_pellet_2");
		gen.start(Ic2Items.RTG_PELLET, "RRR", "AAA", "RRR")
			.key('R', Ic2Items.DENSE_IRON_PLATE)
			.key('A', Ic2ItemTags.PLUTONIUM_INGOTS)
			.group("rtg_pellet")
			.finish("rtg_pellet_vertical_2");
		gen.start(Ic2Items.COIL, "CCC", "CXC", "CCC").key('X', Items.IRON_INGOT).key('C', Ic2Items.COPPER_CABLE).finish("coil");
		gen.start(Ic2Items.ELECTRIC_MOTOR, " T ", "CXC", " T ")
			.key('X', Items.IRON_INGOT)
			.key('C', Ic2Items.COIL)
			.key('T', Ic2Items.TIN_CASING)
			.group("electric_motor")
			.finish("electric_motor");
		gen.start(Ic2Items.ELECTRIC_MOTOR, " C ", "TXT", " C ")
			.key('X', Items.IRON_INGOT)
			.key('C', Ic2Items.COIL)
			.key('T', Ic2Items.TIN_CASING)
			.group("electric_motor")
			.finish("electric_motor_vertical");
		gen.start(Ic2Items.POWER_UNIT, "BAC", "BIM", "BAC")
			.key('C', Ic2Items.IRON_CASING)
			.key('B', Ic2Items.RE_BATTERY)
			.key('I', Ic2Items.CIRCUIT)
			.key('M', Ic2Items.ELECTRIC_MOTOR)
			.key('A', Ic2Items.COPPER_CABLE)
			.finish("power_unit");
		gen.start(Ic2Items.SMALL_POWER_UNIT, " AC", "BIM", " AC")
			.key('C', Ic2Items.IRON_CASING)
			.key('B', Ic2Items.RE_BATTERY)
			.key('I', Ic2Items.CIRCUIT)
			.key('M', Ic2Items.ELECTRIC_MOTOR)
			.key('A', Ic2Items.COPPER_CABLE)
			.finish("small_power_unit");
		gen.start(Ic2Items.RAW_CRYSTAL_MEMORY, "SOS", "OSO", "SOS")
			.key('O', Ic2ItemTags.OBSIDIAN_DUSTS)
			.key('S', Ic2Items.SILICON_DIOXIDE_DUST)
			.finish("raw_crystal_memory");
		gen.start(Ic2Items.HEAT_CONDUCTOR, "RCR", "RCR", "RCR").key('R', Ic2Items.RUBBER).key('C', Ic2ItemTags.COPPER_PLATES).finish("heat_conductor");
		gen.start(Ic2Items.COPPER_BOILER, "CCC", "C C", "CCC").key('C', Ic2Items.COPPER_CASING).finish("copper_boiler");
		gen.start(Ic2Items.WOODEN_ROTOR_BLADE, "PSP", "PSP", "PSP").key('P', ItemTags.PLANKS).key('S', ItemTags.LOGS).finish("wooden_rotor_blade");
		gen.start(Ic2Items.BRONZE_ROTOR_BLADE, "PSP", "PSP", "PSP")
			.key('P', Ic2ItemTags.BRONZE_PLATES)
			.key('S', Ic2ItemTags.BRONZE_INGOTS)
			.finish("bronze_rotor_blade");
		gen.start(Ic2Items.IRON_ROTOR_BLADE, "PSP", "PSP", "PSP").key('P', Ic2ItemTags.IRON_PLATES).key('S', Items.IRON_INGOT).finish("iron_rotor_blade");
		gen.start(Ic2Items.STEEL_ROTOR_BLADE, "PSP", "PSP", "PSP")
			.key('P', Ic2ItemTags.STEEL_PLATES)
			.key('S', Ic2ItemTags.STEEL_INGOTS)
			.finish("steel_rotor_blade");
		gen.start(Ic2Items.CARBON_ROTOR_BLADE, "PSP", "PSP", "PSP").key('P', Ic2Items.CARBON_PLATE).key('S', Ic2Items.CARBON_MESH).finish("carbon_rotor_blade");
		gen.start(Ic2Items.WOODEN_ROTOR, " A ", "ABA", " A ").key('A', Ic2Items.WOODEN_ROTOR_BLADE).key('B', Items.IRON_INGOT).finish("wooden_rotor");
		gen.start(Ic2Items.BRONZE_ROTOR, " A ", "ABA", " A ").key('A', Ic2Items.BRONZE_ROTOR_BLADE).key('B', Ic2Items.BRONZE_SHAFT).finish("bronze_rotor");
		gen.start(Ic2Items.IRON_ROTOR, " A ", "ABA", " A ").key('A', Ic2Items.IRON_ROTOR_BLADE).key('B', Ic2Items.IRON_SHAFT).finish("iron_rotor");
		gen.start(Ic2Items.STEEL_ROTOR, " A ", "ABA", " A ").key('A', Ic2Items.STEEL_ROTOR_BLADE).key('B', Ic2Items.IRON_SHAFT).finish("steel_rotor");
		gen.start(Ic2Items.CARBON_ROTOR, " A ", "ABA", " A ").key('A', Ic2Items.CARBON_ROTOR_BLADE).key('B', Ic2Items.STEEL_SHAFT).finish("carbon_rotor");
		gen.start(Ic2Items.STEAM_TURBINE_BLADE, "AAA", "ABA", "AAA")
			.key('A', Ic2ItemTags.STEEL_PLATES)
			.key('B', Ic2ItemTags.STEEL_INGOTS)
			.finish("steam_turbine_blade");
		gen.start(Ic2Items.STEAM_TURBINE, "A", "A", "A").key('A', Ic2Items.STEAM_TURBINE_BLADE).group("steam_turbine").finish("steam_turbine_vertical");
		gen.start(Ic2Items.STEAM_TURBINE, "AAA").key('A', Ic2Items.STEAM_TURBINE_BLADE).group("steam_turbine").finish("steam_turbine");
		gen.start(Ic2Items.SCANNER, "PGP", "CBC", "ccc")
			.key('B', Ic2Items.ADVANCED_RE_BATTERY)
			.key('c', Ic2Items.INSULATED_COPPER_CABLE)
			.key('G', Items.GLOWSTONE_DUST)
			.key('C', Ic2Items.CIRCUIT)
			.key('P', Ic2Items.GOLD_CASING)
			.finish("scanner");
		gen.start(Ic2Items.ADVANCED_SCANNER, "PDP", "GCG", "cSc")
			.key('S', Ic2Items.SCANNER)
			.key('c', Ic2Items.DOUBLE_INSULATED_GOLD_CABLE)
			.key('G', Items.GLOWSTONE_DUST)
			.key('C', Ic2Items.ADVANCED_CIRCUIT)
			.key('P', Ic2Items.GOLD_CASING)
			.key('D', Ic2Items.ENERGY_CRYSTAL)
			.finish("advanced_scanner");
		gen.start(Ic2Items.SCRAP_BOX, "SSS", "SSS", "SSS").key('S', Ic2Items.SCRAP).finish("scrap_box");
		gen.start(Ic2Items.BLANK_TFBP, " C ", " A ", "R R")
			.key('C', Ic2Items.CIRCUIT)
			.key('A', Ic2Items.ADVANCED_CIRCUIT)
			.key('R', Items.REDSTONE)
			.finish("blank_tfbp");
		gen.start(Ic2Items.CHILLING_TFBP, " S ", "S#S", " S ").key('#', Ic2Items.BLANK_TFBP).key('S', Items.SNOWBALL).finish("chilling_tfbp");
		gen.start(Ic2Items.CULTIVATION_TFBP, " S ", "S#S", " S ").key('#', Ic2Items.BLANK_TFBP).key('S', Items.WHEAT_SEEDS).finish("cultivation_tfbp");
		gen.start(Ic2Items.DESERTIFICATION_TFBP, " S ", "S#S", " S ").key('#', Ic2Items.BLANK_TFBP).key('S', Items.SAND).finish("desertification_tfbp");
		gen.start(Ic2Items.FLATIFICATION_TFBP, " D ", "D#D", " D ").key('#', Ic2Items.BLANK_TFBP).key('D', Items.DIRT).finish("flatification_tfbp");
		gen.start(Ic2Items.IRRIGATION_TFBP, " W ", "W#W", " W ").key('#', Ic2Items.BLANK_TFBP).key('W', Items.WATER_BUCKET).finish("irrigation_tfbp");
		gen.start(Ic2Items.MUSHROOM_TFBP, "mMm", "M#M", "mMm")
			.key('#', Ic2Items.BLANK_TFBP)
			.key('M', Items.BROWN_MUSHROOM)
			.key('m', Items.MYCELIUM)
			.finish("mushroom_tfbp");
		gen.start(Ic2Items.BRONZE_AXE, "BB", "SB", "S ").key('B', Ic2ItemTags.BRONZE_INGOTS).key('S', Items.STICK).finish("bronze_axe");
		gen.start(Ic2Items.BRONZE_HOE, "BB", "S ", "S ").key('B', Ic2ItemTags.BRONZE_INGOTS).key('S', Items.STICK).finish("bronze_hoe");
		gen.start(Ic2Items.BRONZE_PICKAXE, "BBB", " S ", " S ").key('B', Ic2ItemTags.BRONZE_INGOTS).key('S', Items.STICK).finish("bronze_pickaxe");
		gen.start(Ic2Items.BRONZE_SHOVEL, "B", "S", "S").key('B', Ic2ItemTags.BRONZE_INGOTS).key('S', Items.STICK).finish("bronze_shovel");
		gen.start(Ic2Items.BRONZE_SWORD, "B", "B", "S").key('B', Ic2ItemTags.BRONZE_INGOTS).key('S', Items.STICK).finish("bronze_sword");
		gen.start(Ic2Items.CUTTER, "A A", " A ", "I I").key('A', Ic2ItemTags.IRON_PLATES).key('I', Items.IRON_INGOT).finish("cutter");
		gen.start(Ic2Items.DIAMOND_DRILL, " D ", "DdD").key('D', Ic2ItemTags.DIAMONDS).key('d', Ic2Items.DRILL).finish("diamond_drill");
		gen.start(Ic2Items.DRILL, " I ", "III", "IBI").key('I', Ic2ItemTags.IRON_PLATES).key('B', Ic2Items.POWER_UNIT).finish("drill");
		gen.start(Ic2Items.FORGE_HAMMER, "II ", "ISS", "II ").key('S', Items.STICK).key('I', Items.IRON_INGOT).group("forge_hammer").finish("forge_hammer");
		gen.start(Ic2Items.FORGE_HAMMER, " II", "SSI", " II").key('S', Items.STICK).key('I', Items.IRON_INGOT).group("forge_hammer").finish("forge_hammer_2");
		gen.start(Ic2Items.IRIDIUM_DRILL, " I ", "IdI", " C ")
			.key('I', Ic2Items.IRIDIUM)
			.key('d', Ic2Items.DIAMOND_DRILL)
			.key('C', Ic2Items.ENERGY_CRYSTAL)
			.finish("iridium_drill");
		gen.start(Ic2Items.WRENCH, "B B", "BBB", " B ").key('B', Ic2ItemTags.BRONZE_INGOTS).group("wrench").finish("wrench");
		gen.start(Ic2Items.WRENCH, " B ", "BBB", "B B").key('B', Ic2ItemTags.BRONZE_INGOTS).group("wrench").finish("wrench_down");
		gen.start(Ic2Items.TOOL_BOX, "ICI", "III").key('C', Ic2ItemTags.WOODEN_CHESTS).key('I', Ic2Items.BRONZE_CASING).finish("tool_box");
		gen.start(Ic2Items.TREETAP, " P ", "PPP", "P  ").key('P', ItemTags.PLANKS).finish("treetap");
		gen.start(Ic2Items.URANIUM, "UUU", "CCC", "UUU").key('U', Ic2Items.URANIUM_238).key('C', Ic2Items.SMALL_URANIUM_235).group("uranium").finish("uranium");
		gen.start(Ic2Items.URANIUM, "UUU", "CCC", "UUU")
			.key('U', Ic2ItemTags.URANIUM_INGOTS)
			.key('C', Ic2Items.SMALL_URANIUM_235)
			.group("uranium")
			.finish("uranium_2");
		gen.start(Ic2Items.URANIUM_235, "UUU", "UUU", "UUU").key('U', Ic2Items.SMALL_URANIUM_235).finish("uranium_235");
		gen.start(Ic2Items.WEED_EX_CELL, "R", "G", "C")
			.key('R', Items.REDSTONE)
			.key('G', Ic2Items.GRIN_POWDER)
			.key('C', Ic2Items.EMPTY_CELL)
			.finish("weed_ex_cell");
		gen.start(Ic2Items.OBSCURATOR, "rEr", "CAC", "rrr")
			.key('r', Items.REDSTONE)
			.key('E', Ic2Items.ADVANCED_RE_BATTERY)
			.key('C', Ic2Items.DOUBLE_INSULATED_GOLD_CABLE)
			.key('A', Ic2Items.ADVANCED_CIRCUIT)
			.finish("obscurator");
		gen.start(Ic2Items.RSH_CONDENSATOR, "RRR", "RVR", "RSR")
			.key('R', Items.REDSTONE)
			.key('V', Ic2Items.HEAT_VENT)
			.key('S', Ic2Items.HEAT_EXCHANGER)
			.finish("rsh_condensator");
		gen.start(Ic2Items.LZH_CONDENSATOR, "RVR", "CLC", "RSR")
			.key('R', Items.REDSTONE)
			.key('V', Ic2Items.REACTOR_HEAT_VENT)
			.key('S', Ic2Items.REACTOR_HEAT_EXCHANGER)
			.key('C', Ic2Items.RSH_CONDENSATOR)
			.key('L', Items.LAPIS_BLOCK)
			.finish("lzh_condensator");
		gen.start(Ic2Items.REACTOR_COOLANT_CELL, " T ", "TWT", " T ")
			.key('W', Ic2Fluids.COOLANT.still, 1000)
			.key('T', Ic2ItemTags.TIN_PLATES)
			.finish("reactor_coolant_cell");
		gen.start(Ic2Items.TRIPLE_REACTOR_COOLANT_CELL, "TTT", "CCC", "TTT")
			.key('C', Ic2Items.REACTOR_COOLANT_CELL)
			.key('T', Ic2ItemTags.TIN_PLATES)
			.finish("triple_reactor_coolant_cell");
		gen.start(Ic2Items.SEXTUPLE_REACTOR_COOLANT_CELL, "TCT", "TcT", "TCT")
			.key('C', Ic2Items.TRIPLE_REACTOR_COOLANT_CELL)
			.key('T', Ic2ItemTags.TIN_PLATES)
			.key('c', Ic2ItemTags.IRON_PLATES)
			.finish("sextuple_reactor_coolant_cell");
		gen.start(Ic2Items.HEAT_EXCHANGER, "CcC", "TCT", "CTC")
			.key('c', Ic2Items.CIRCUIT)
			.key('T', Ic2ItemTags.TIN_PLATES)
			.key('C', Ic2ItemTags.COPPER_PLATES)
			.finish("heat_exchanger_from_copper");
		gen.start(Ic2Items.REACTOR_HEAT_EXCHANGER, "CCC", "CSC", "CCC")
			.key('S', Ic2Items.HEAT_EXCHANGER)
			.key('C', Ic2ItemTags.COPPER_PLATES)
			.finish("reactor_heat_exchanger");
		gen.start(Ic2Items.ADVANCED_HEAT_EXCHANGER, "GcG", "SCS", "GcG")
			.key('S', Ic2Items.HEAT_EXCHANGER)
			.key('C', Ic2ItemTags.COPPER_PLATES)
			.key('G', Ic2ItemTags.LAPIS_PLATES)
			.key('c', Ic2Items.CIRCUIT)
			.finish("advanced_heat_exchanger");
		gen.start(Ic2Items.HEAT_EXCHANGER, " G ", "GSG", " G ")
			.key('S', Ic2Items.HEAT_EXCHANGER)
			.key('G', Ic2ItemTags.GOLD_PLATES)
			.finish("heat_exchanger_from_gold");
		gen.start(Ic2Items.DUAL_MOX_FUEL_ROD, "UIU").key('U', Ic2Items.MOX_FUEL_ROD).key('I', Ic2ItemTags.IRON_PLATES).finish("dual_mox_fuel_rod");
		gen.start(Ic2Items.QUAD_MOX_FUEL_ROD, "UIU", "CIC", "UIU")
			.key('U', Ic2Items.MOX_FUEL_ROD)
			.key('I', Ic2ItemTags.IRON_PLATES)
			.key('C', Ic2ItemTags.COPPER_PLATES)
			.group("quad_mox_fuel_rod")
			.finish("quad_mox_fuel_rod");
		gen.start(Ic2Items.QUAD_MOX_FUEL_ROD, " U ", "CIC", " U ")
			.key('U', Ic2Items.DUAL_MOX_FUEL_ROD)
			.key('I', Ic2ItemTags.IRON_PLATES)
			.key('C', Ic2ItemTags.COPPER_PLATES)
			.group("quad_mox_fuel_rod")
			.finish("quad_mox_fuel_rod_from_dual");
		gen.start(Ic2Items.REACTOR_HEAT_PLATING, "CCC", "CcC", "CCC")
			.key('c', Ic2Items.REACTOR_PLATING)
			.key('C', Ic2ItemTags.COPPER_PLATES)
			.finish("reactor_heat_plating");
		gen.start(Ic2Items.NEUTRON_REFLECTOR, "TcT", "cCc", "TcT")
			.key('c', Ic2ItemTags.COAL_DUSTS)
			.key('C', Ic2ItemTags.COPPER_PLATES)
			.key('T', Ic2ItemTags.TIN_DUSTS)
			.finish("neutron_reflector");
		gen.start(Ic2Items.THICK_NEUTRON_REFLECTOR, "CRC", "RCR", "CRC")
			.key('C', Ic2ItemTags.COPPER_PLATES)
			.key('R', Ic2Items.NEUTRON_REFLECTOR)
			.finish("thick_neutron_reflector");
		gen.start(Ic2Items.IRIDIUM_NEUTRON_REFLECTOR, "RRR", "CIC", "RRR")
			.key('C', Ic2Items.DENSE_COPPER_PLATE)
			.key('R', Ic2Items.THICK_NEUTRON_REFLECTOR)
			.key('I', Ic2Items.IRIDIUM)
			.group("iridium_neutron_reflector")
			.finish("iridium_neutron_reflector");
		gen.start(Ic2Items.IRIDIUM_NEUTRON_REFLECTOR, "RCR", "RIR", "RCR")
			.key('C', Ic2Items.DENSE_COPPER_PLATE)
			.key('R', Ic2Items.THICK_NEUTRON_REFLECTOR)
			.key('I', Ic2Items.IRIDIUM)
			.group("iridium_neutron_reflector")
			.finish("iridium_neutron_reflector_vertical");
		gen.start(Ic2Items.DUAL_URANIUM_FUEL_ROD, "UIU").key('U', Ic2Items.URANIUM_FUEL_ROD).key('I', Ic2ItemTags.IRON_PLATES).finish("dual_uranium_fuel_rod");
		gen.start(Ic2Items.QUAD_URANIUM_FUEL_ROD, "UIU", "CIC", "UIU")
			.key('U', Ic2Items.URANIUM_FUEL_ROD)
			.key('I', Ic2ItemTags.IRON_PLATES)
			.key('C', Ic2ItemTags.COPPER_PLATES)
			.group("quad_uranium_fuel_rod")
			.finish("quad_uranium_fuel_rod");
		gen.start(Ic2Items.QUAD_URANIUM_FUEL_ROD, " U ", "CIC", " U ")
			.key('U', Ic2Items.DUAL_URANIUM_FUEL_ROD)
			.key('I', Ic2ItemTags.IRON_PLATES)
			.key('C', Ic2ItemTags.COPPER_PLATES)
			.group("quad_uranium_fuel_rod")
			.finish("quad_uranium_fuel_rod_from_dual");
		gen.start(Ic2Items.HEAT_VENT, "#I#", "IMI", "#I#")
			.key('M', Ic2Items.ELECTRIC_MOTOR)
			.key('I', Ic2ItemTags.IRON_PLATES)
			.key('#', Items.IRON_BARS)
			.finish("heat_vent");
		gen.start(Ic2Items.REACTOR_HEAT_VENT, "CCC", "CVC", "CCC").key('V', Ic2Items.HEAT_VENT).key('C', Ic2ItemTags.COPPER_PLATES).finish("reactor_heat_vent");
		gen.start(Ic2Items.ADVANCED_HEAT_VENT, "#V#", "#D#", "#V#")
			.key('V', Ic2Items.HEAT_VENT)
			.key('#', Items.IRON_BARS)
			.key('D', Ic2ItemTags.DIAMONDS)
			.finish("advanced_heat_vent");
		gen.start(Ic2Items.OVERCLOCKED_HEAT_VENT, " G ", "GVG", " G ")
			.key('V', Ic2Items.REACTOR_HEAT_VENT)
			.key('G', Ic2ItemTags.GOLD_PLATES)
			.finish("overclocked_heat_vent");
		gen.start(Ic2Items.OVERCLOCKER_UPGRADE, 2, "CCC", "WEW")
			.key('C', Ic2Items.REACTOR_COOLANT_CELL)
			.key('W', Ic2Items.INSULATED_COPPER_CABLE)
			.key('E', Ic2Items.CIRCUIT)
			.group("overclocker_upgrade")
			.finish("overclocker_upgrade_2");
		gen.start(Ic2Items.OVERCLOCKER_UPGRADE, 6, "CCC", "WEW")
			.key('C', Ic2Items.TRIPLE_REACTOR_COOLANT_CELL)
			.key('W', Ic2Items.INSULATED_COPPER_CABLE)
			.key('E', Ic2Items.CIRCUIT)
			.group("overclocker_upgrade")
			.finish("overclocker_upgrade_6");
		gen.start(Ic2Items.OVERCLOCKER_UPGRADE, 12, "CCC", "WEW")
			.key('C', Ic2Items.SEXTUPLE_REACTOR_COOLANT_CELL)
			.key('W', Ic2Items.INSULATED_COPPER_CABLE)
			.key('E', Ic2Items.CIRCUIT)
			.group("overclocker_upgrade")
			.finish("overclocker_upgrade_12");
		gen.start(Ic2Items.TRANSFORMER_UPGRADE, "GGG", "WTW", "GEG")
			.key('G', Items.GLASS)
			.key('W', Ic2Items.DOUBLE_INSULATED_GOLD_CABLE)
			.key('T', Ic2Items.MV_TRANSFORMER)
			.key('E', Ic2Items.CIRCUIT)
			.finish("transformer_upgrade");
		gen.start(Ic2Items.ENERGY_STORAGE_UPGRADE, "www", "WBW", "wEw")
			.key('w', ItemTags.PLANKS)
			.key('W', Ic2Items.INSULATED_COPPER_CABLE)
			.key('B', Ic2Items.RE_BATTERY)
			.key('E', Ic2Items.CIRCUIT)
			.finish("energy_storage_upgrade");
		gen.start(Ic2Items.EJECTOR_UPGRADE, "T T", " P ", "T T")
			.key('T', Ic2ItemTags.TIN_PLATES)
			.key('P', Items.PISTON)
			.group("ejector_upgrade")
			.finish("ejector_upgrade");
		gen.start(Ic2Items.EJECTOR_UPGRADE, 9, "T T", " P ", "T T")
			.key('T', Ic2Items.DENSE_TIN_PLATE)
			.key('P', Items.PISTON)
			.group("ejector_upgrade")
			.finish("ejector_upgrade_9");
		gen.start(Ic2Items.PULLING_UPGRADE, "T T", " P ", "T T")
			.key('T', Ic2ItemTags.TIN_PLATES)
			.key('P', Items.STICKY_PISTON)
			.group("pulling_upgrade")
			.finish("pulling_upgrade");
		gen.start(Ic2Items.PULLING_UPGRADE, 9, "T T", " P ", "T T")
			.key('T', Ic2Items.DENSE_TIN_PLATE)
			.key('P', Items.STICKY_PISTON)
			.group("pulling_upgrade")
			.finish("pulling_upgrade_9");
		gen.start(Ic2Items.FLUID_EJECTOR_UPGRADE, "T T", " E ", "T T")
			.key('E', Ic2Items.ELECTRIC_MOTOR)
			.key('T', Ic2ItemTags.TIN_PLATES)
			.group("fluid_ejector_upgrade")
			.finish("fluid_ejector_upgrade");
		gen.start(Ic2Items.FLUID_EJECTOR_UPGRADE, 9, "T T", " E ", "T T")
			.key('E', Ic2Items.ELECTRIC_MOTOR)
			.key('T', Ic2Items.DENSE_TIN_PLATE)
			.group("fluid_ejector_upgrade")
			.finish("fluid_ejector_upgrade_9");
		gen.start(Ic2Items.FLUID_PULLING_UPGRADE, "TXT", " E ", "T T")
			.key('E', Ic2Items.ELECTRIC_MOTOR)
			.key('T', Ic2ItemTags.TIN_PLATES)
			.key('X', Ic2Items.TREETAP)
			.group("fluid_pulling_upgrade")
			.finish("fluid_pulling_upgrade");
		gen.start(Ic2Items.FLUID_PULLING_UPGRADE, 9, "TXT", " E ", "T T")
			.key('E', Ic2Items.ELECTRIC_MOTOR)
			.key('T', Ic2Items.DENSE_TIN_PLATE)
			.key('X', Ic2Items.TREETAP)
			.group("fluid_pulling_upgrade")
			.finish("fluid_pulling_upgrade_9");
		gen.start(Ic2Items.REDSTONE_INVERTER_UPGRADE, "T T", " L ", "T T")
			.key('L', Items.LEVER)
			.key('T', Ic2ItemTags.TIN_PLATES)
			.group("redstone_inverter_upgrade")
			.finish("redstone_inverter_upgrade");
		gen.start(Ic2Items.REDSTONE_INVERTER_UPGRADE, 9, "T T", " L ", "T T")
			.key('L', Items.LEVER)
			.key('T', Ic2Items.DENSE_TIN_PLATE)
			.group("redstone_inverter_upgrade")
			.finish("redstone_inverter_upgrade_9");
		gen.start(Ic2Items.CHUNK_LOADER, "TET", "LML", "TCT")
			.key('T', Ic2ItemTags.TIN_PLATES)
			.key('E', Items.ENDER_PEARL)
			.key('L', Items.LAPIS_LAZULI)
			.key('M', Ic2Items.MACHINE)
			.key('C', Ic2Items.CIRCUIT)
			.finish("chunk_loader");
		gen.start(Ic2Items.INDUSTRIAL_WORKBENCH, " T ", "HMC")
			.key('H', Ic2Items.FORGE_HAMMER)
			.key('M', Ic2Items.MACHINE)
			.key('C', Ic2Items.CUTTER)
			.key('T', Items.CRAFTING_TABLE)
			.consuming()
			.finish("industrial_workbench");
		gen.start(Ic2Items.BATCH_CRAFTER, " I ", "ACA", "HWH")
			.key('H', Ic2Items.FORGE_HAMMER)
			.key('W', Ic2Items.WRENCH)
			.key('C', Ic2Items.ADVANCED_MACHINE)
			.key('A', Ic2Items.ADVANCED_CIRCUIT)
			.key('I', Ic2Items.INDUSTRIAL_WORKBENCH)
			.consuming()
			.finish("batch_crafter");
		gen.start(Ic2Items.STEAM_REPRESSURIZER, "III", "TBT", "ICI")
			.key('I', Ic2Items.IRON_CASING)
			.key('T', Ic2Items.TANK)
			.key('B', Ic2Items.COPPER_BOILER)
			.key('C', Ic2Items.HEAT_CONDUCTOR)
			.group("steam_repressurizer")
			.finish("steam_repressurizer_from_tank");
		gen.start(Ic2Items.STEAM_REPRESSURIZER, "III", "TBT", "ICI")
			.key('I', Ic2Items.IRON_CASING)
			.key('T', Ic2Items.IRON_TANK)
			.key('B', Ic2Items.COPPER_BOILER)
			.key('C', Ic2Items.HEAT_CONDUCTOR)
			.group("steam_repressurizer")
			.finish("steam_repressurizer_from_iron_tank");
		gen.start(Ic2Items.WEIGHTED_ITEM_DISTRIBUTOR, "CUC", "UMU", "CIC")
			.key('M', Ic2Items.MACHINE)
			.key('C', Items.CHEST)
			.key('U', Ic2Items.EJECTOR_UPGRADE)
			.key('I', Ic2Items.PULLING_UPGRADE)
			.finish("weighted_item_distributor");
		gen.start(Ic2Items.WEIGHTED_FLUID_DISTRIBUTOR, "CUC", "UMU", "CIC")
			.key('M', Ic2Items.MACHINE)
			.key('C', Ic2Items.EMPTY_CELL)
			.key('U', Ic2Items.FLUID_EJECTOR_UPGRADE)
			.key('I', Ic2Items.FLUID_PULLING_UPGRADE)
			.consuming()
			.finish("weighted_fluid_distributor");
		gen.start(Ic2Items.JETPACK_ATTACHMENT_PLATE, "IAI", "CRC", "IAI")
			.key('R', Ic2ItemTags.STEEL_PLATES)
			.key('A', Ic2Items.ALLOY)
			.key('C', Ic2Items.CARBON_PLATE)
			.key('I', Ic2ItemTags.IRIDIUM_NUGGETS)
			.finish("jetpack_attachment_plate");
		gen.start(Items.BUCKET, "T T", " T ").key('T', Ic2ItemTags.TIN_INGOTS).hidden().finish("bucket");
		gen.start(Items.GLOWSTONE_DUST, "RGR", "GRG", "RGR").key('R', Items.REDSTONE).key('G', Ic2ItemTags.GOLD_DUSTS).hidden().finish("glowstone_dust");
		gen.start(Items.GUNPOWDER, 3, "RCR", "CRC", "RCR").key('R', Items.REDSTONE).key('C', Ic2ItemTags.COAL_DUSTS).hidden().finish("gunpowder");
		gen.start(Items.IRON_INGOT, 8, "M").key('M', Ic2Items.MACHINE).group("iron_ingot").finish("iron_ingot_from_machine");
		gen.start(Items.IRON_INGOT, 2, "III", "I I", "III").key('I', Ic2Items.COIN).group("iron_ingot").finish("iron_ingot_from_coin");
		gen.start(Items.PISTON, "TTT", "#X#", "#R#")
			.key('#', Items.COBBLESTONE)
			.key('X', Ic2ItemTags.BRONZE_INGOTS)
			.key('R', Items.REDSTONE)
			.key('T', ItemTags.PLANKS)
			.hidden()
			.finish("piston");
		gen.start(Items.RAIL, 8, "B B", "BsB", "B B").key('B', Ic2ItemTags.BRONZE_INGOTS).key('s', Items.STICK).hidden().finish("rail");
		gen.start(Items.TORCH, 4, "R", "I").key('I', Items.STICK).key('R', Ic2Items.RESIN).hidden().finish("torch");
		gen.start(Ic2Items.WOODEN_STORAGE_BOX, "LPL", "P P", "LPL").key('L', ItemTags.LOGS).key('P', ItemTags.PLANKS).finish("wooden_storage_box");
		gen.start(Ic2Items.IRON_STORAGE_BOX, "PCP", "C C", "PCP").key('P', Ic2ItemTags.IRON_PLATES).key('C', Ic2Items.IRON_CASING).finish("iron_storage_box");
		gen.start(Ic2Items.BRONZE_STORAGE_BOX, "PCP", "C C", "PCP")
			.key('P', Ic2ItemTags.BRONZE_PLATES)
			.key('C', Ic2Items.BRONZE_CASING)
			.finish("bronze_storage_box");
		gen.start(Ic2Items.STEEL_STORAGE_BOX, "PCP", "C C", "PCP").key('P', Ic2ItemTags.STEEL_PLATES).key('C', Ic2Items.STEEL_CASING).finish("steel_storage_box");
		gen.start(Ic2Items.IRIDIUM_STORAGE_BOX, "ISI", "S S", "ISI").key('I', Ic2Items.IRIDIUM).key('S', Ic2ItemTags.STEEL_PLATES).finish("iridium_storage_box");
		gen.start(Ic2Items.BRONZE_TANK, "PUP", "U U", "PUP").key('U', Ic2Items.EMPTY_CELL).key('P', Ic2ItemTags.BRONZE_PLATES).consuming().finish("bronze_tank");
		gen.start(Ic2Items.IRON_TANK, "PUP", "U U", "PUP").key('U', Ic2Items.EMPTY_CELL).key('P', Ic2ItemTags.IRON_PLATES).consuming().finish("iron_tank");
		gen.start(Ic2Items.STEEL_TANK, "PUP", "U U", "PUP").key('U', Ic2Items.EMPTY_CELL).key('P', Ic2ItemTags.STEEL_PLATES).consuming().finish("steel_tank");
		gen.start(Ic2Items.IRIDIUM_TANK, "PUP", "U U", "PUP").key('U', Ic2Items.EMPTY_CELL).key('P', Ic2Items.IRIDIUM).consuming().finish("iridium_tank");
		gen.start(Ic2Items.RUBBER_WOOD, 3, "LL", "LL").key('L', Ic2Items.RUBBER_LOG).finish("rubber_wood");
		gen.start(Ic2Items.STRIPPED_RUBBER_WOOD, 3, "LL", "LL").key('L', Ic2Items.STRIPPED_RUBBER_LOG).finish("stripped_rubber_wood");
		gen.start(Ic2Items.RUBBER_PRESSURE_PLATE, "PP").key('P', Ic2Items.RUBBER_PLANKS).finish("rubber_pressure_plate");
		gen.start(Ic2Items.RUBBER_FENCE, 3, "PIP", "PIP").key('P', Ic2Items.RUBBER_PLANKS).key('I', Items.STICK).finish("rubber_fence");
		gen.start(Ic2Items.RUBBER_FENCE_GATE, "IPI", "IPI").key('P', Ic2Items.RUBBER_PLANKS).key('I', Items.STICK).finish("rubber_fence_gate");
		gen.start(Ic2Items.RUBBER_TRAPDOOR, 2, "PPP", "PPP").key('P', Ic2Items.RUBBER_PLANKS).finish("rubber_trapdoor");
		gen.start(Ic2Items.RUBBER_DOOR, 3, "PP", "PP", "PP").key('P', Ic2Items.RUBBER_PLANKS).finish("rubber_door");
		gen.start(Ic2Items.RUBBER_SIGN, 3, "PPP", "PPP", " I ").key('P', Ic2Items.RUBBER_PLANKS).key('I', Items.STICK).finish("rubber_sign");
		gen.start(Ic2Items.RUBBER_SLAB, 6, "PPP").key('P', Ic2Items.RUBBER_PLANKS).finish("rubber_slab");
		gen.start(Ic2Items.RUBBER_STAIRS, 4, "P  ", "PP ", "PPP").key('P', Ic2Items.RUBBER_PLANKS).finish("rubber_stairs");
	}
}
