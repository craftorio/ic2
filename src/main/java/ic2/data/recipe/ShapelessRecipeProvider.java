package ic2.data.recipe;

import ic2.core.recipe.input.RecipeInputMultiple;
import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2Items;
import ic2.data.recipe.helper.AdvShapelessRecipeGenerator;
import ic2.data.recipe.helper.Ic2RecipeProvider;
import ic2.data.recipe.helper.ShapelessRecipeGenerator;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

public class ShapelessRecipeProvider extends Ic2RecipeProvider
{
	public ShapelessRecipeProvider(DataGenerator generator)
	{
		super(generator);
	}

	@Override
	protected void generate(Consumer<FinishedRecipe> consumer)
	{
		AdvShapelessRecipeGenerator gen = new AdvShapelessRecipeGenerator(consumer);
		ShapelessRecipeGenerator vanillaGen = new ShapelessRecipeGenerator(consumer);
		RecipeInputMultiple processedTin = new RecipeInputMultiple(
			1, tagInput(Ic2ItemTags.TIN_DUSTS), itemInput(Ic2Items.CRUSHED_TIN), itemInput(Ic2Items.PURIFIED_TIN)
		);
		RecipeInputMultiple processedCopper = new RecipeInputMultiple(
			1, tagInput(Ic2ItemTags.COPPER_DUSTS), itemInput(Ic2Items.CRUSHED_COPPER), itemInput(Ic2Items.PURIFIED_COPPER)
		);
		gen.start(Ic2Items.BRONZE_DUST, 4).add(processedTin).add(processedCopper).add(processedCopper).add(processedCopper).finish("bronze_dust");
		gen.start(Ic2Items.BRONZE_CASING, 2).add(Ic2ItemTags.BRONZE_PLATES).add(Ic2ItemTags.FORGE_HAMMERS).finish("bronze_casing");
		gen.start(Ic2Items.COPPER_CASING, 2).add(Ic2ItemTags.COPPER_PLATES).add(Ic2ItemTags.FORGE_HAMMERS).finish("copper_casing");
		gen.start(Ic2Items.GOLD_CASING, 2).add(Ic2ItemTags.GOLD_PLATES).add(Ic2ItemTags.FORGE_HAMMERS).finish("gold_casing");
		gen.start(Ic2Items.IRON_CASING, 2).add(Ic2ItemTags.IRON_PLATES).add(Ic2ItemTags.FORGE_HAMMERS).finish("iron_casing");
		gen.start(Ic2Items.LEAD_CASING, 2).add(Ic2ItemTags.LEAD_PLATES).add(Ic2ItemTags.FORGE_HAMMERS).finish("lead_casing");
		gen.start(Ic2Items.TIN_CASING, 2).add(Ic2ItemTags.TIN_PLATES).add(Ic2ItemTags.FORGE_HAMMERS).finish("tin_casing");
		gen.start(Ic2Items.BRONZE_PLATE).add(Ic2ItemTags.BRONZE_INGOTS).add(Ic2ItemTags.FORGE_HAMMERS).finish("bronze_plate");
		gen.start(Ic2Items.COPPER_PLATE).add(Items.f_151052_).add(Ic2ItemTags.FORGE_HAMMERS).finish("copper_plate");
		gen.start(Ic2Items.GOLD_PLATE).add(Items.f_42417_).add(Ic2ItemTags.FORGE_HAMMERS).finish("gold_plate");
		gen.start(Ic2Items.IRON_PLATE).add(Items.f_42416_).add(Ic2ItemTags.FORGE_HAMMERS).finish("iron_plate");
		gen.start(Ic2Items.LEAD_PLATE).add(Ic2ItemTags.LEAD_INGOTS).add(Ic2ItemTags.FORGE_HAMMERS).finish("lead_plate");
		gen.start(Ic2Items.TIN_PLATE).add(Ic2ItemTags.TIN_INGOTS).add(Ic2ItemTags.FORGE_HAMMERS).finish("tin_plate");
		gen.start(Ic2Items.INSULATED_COPPER_CABLE).add(Ic2Items.RUBBER).add(Ic2Items.COPPER_CABLE).finish("insulated_copper_cable");
		gen.start(Ic2Items.COPPER_CABLE, 2).add(Ic2ItemTags.COPPER_PLATES).add(Ic2ItemTags.WIRE_CUTTERS).finish("copper_cable");
		gen.start(Ic2Items.TIN_CABLE, 3).add(Ic2ItemTags.TIN_PLATES).add(Ic2ItemTags.WIRE_CUTTERS).finish("tin_cable");
		gen.start(Ic2Items.INSULATED_TIN_CABLE).add(Ic2Items.RUBBER).add(Ic2Items.TIN_CABLE).finish("insulated_tin_cable");
		gen.start(Ic2Items.GOLD_CABLE, 4).add(Ic2ItemTags.GOLD_PLATES).add(Ic2ItemTags.WIRE_CUTTERS).finish("gold_cable");
		gen.start(Ic2Items.INSULATED_GOLD_CABLE).add(Ic2Items.RUBBER).add(Ic2Items.GOLD_CABLE).finish("insulated_gold_cable");
		gen.start(Ic2Items.DOUBLE_INSULATED_GOLD_CABLE).add(Ic2Items.RUBBER).add(Ic2Items.INSULATED_GOLD_CABLE).finish("double_insulated_gold_cable");
		gen.start(Ic2Items.DOUBLE_INSULATED_GOLD_CABLE)
			.add(Ic2Items.RUBBER)
			.add(Ic2Items.RUBBER)
			.add(Ic2Items.GOLD_CABLE)
			.finish("double_insulated_gold_cable_2");
		gen.start(Ic2Items.INSULATED_IRON_CABLE).add(Ic2Items.RUBBER).add(Ic2Items.IRON_CABLE).finish("insulated_iron_cable");
		gen.start(Ic2Items.DOUBLE_INSULATED_IRON_CABLE).add(Ic2Items.RUBBER).add(Ic2Items.RUBBER).add(Ic2Items.IRON_CABLE).finish("double_insulated_iron_cable");
		gen.start(Ic2Items.TRIPLE_INSULATED_IRON_CABLE)
			.add(Ic2Items.RUBBER)
			.add(Ic2Items.RUBBER)
			.add(Ic2Items.RUBBER)
			.add(Ic2Items.IRON_CABLE)
			.finish("triple_insulated_iron_cable");
		gen.start(Ic2Items.DOUBLE_INSULATED_IRON_CABLE).add(Ic2Items.RUBBER).add(Ic2Items.INSULATED_IRON_CABLE).finish("double_insulated_iron_cable_2");
		gen.start(Ic2Items.TRIPLE_INSULATED_IRON_CABLE)
			.add(Ic2Items.RUBBER)
			.add(Ic2Items.RUBBER)
			.add(Ic2Items.INSULATED_IRON_CABLE)
			.finish("triple_insulated_iron_cable_2");
		gen.start(Ic2Items.TRIPLE_INSULATED_IRON_CABLE).add(Ic2Items.RUBBER).add(Ic2Items.DOUBLE_INSULATED_IRON_CABLE).finish("triple_insulated_iron_cable_3");
		gen.start(Ic2Items.WATER_CELL).add(Ic2Items.EMPTY_CELL).add(Items.f_42447_).finish("water_cell");
		gen.start(Ic2Items.LAVA_CELL).add(Ic2Items.EMPTY_CELL).add(Items.f_42448_).finish("lava_cell");
		gen.start(Ic2Items.COFFEE_POWDER).add(Ic2Items.COFFEE_BEANS).finish("coffee_powder");
		gen.start(Ic2Items.COAL_FUEL_DUST).add(Ic2ItemTags.COAL_DUSTS).add(Fluids.f_76193_, 1000).finish("coal_fuel_dust");
		gen.start(Ic2Items.HYDRATED_TIN_DUST).add(Ic2ItemTags.TIN_DUSTS).add(Fluids.f_76193_, 1000).finish("hydrated_tin_dust");
		gen.start(Ic2Items.FERTILIZER, 2).add(Ic2Items.SCRAP).add(Items.f_42499_).finish("fertilizer");
		gen.start(Ic2Items.FERTILIZER, 2).add(Ic2Items.SCRAP).add(Ic2Items.SCRAP).add(Ic2Items.FERTILIZER).finish("fertilizer_2");
		gen.start(Ic2Items.COLD_COFFEE_MUG).add(Ic2Items.EMPTY_MUG).add(Ic2Items.COFFEE_POWDER).add(Fluids.f_76193_, 1000).finish("cold_coffee_mug");
		gen.start(Ic2Items.COFFEE_MUG).add(Ic2Items.DARK_COFFEE_MUG).add(Items.f_42501_).add(Items.f_42455_).finish("coffee_mug");
		gen.start(Ic2Items.CARBON_MESH).add(Ic2Items.CARBON_FIBRE).add(Ic2Items.CARBON_FIBRE).finish("carbon_mesh");
		gen.start(Ic2Items.SMALL_PLUTONIUM, 9).add(Ic2Items.PLUTONIUM).finish("small_plutonium");
		vanillaGen.start(Ic2Items.BLACK_PAINTER).add(Ic2Items.PAINTER).add(Items.f_42498_).finish("black_painter");
		vanillaGen.start(Ic2Items.BLUE_PAINTER).add(Ic2Items.PAINTER).add(Items.f_42494_).finish("blue_painter");
		vanillaGen.start(Ic2Items.BROWN_PAINTER).add(Ic2Items.PAINTER).add(Items.f_42495_).finish("brown_painter");
		vanillaGen.start(Ic2Items.LIGHT_BLUE_PAINTER).add(Ic2Items.PAINTER).add(Items.f_42538_).finish("light_blue_painter");
		vanillaGen.start(Ic2Items.CYAN_PAINTER).add(Ic2Items.PAINTER).add(Items.f_42492_).finish("cyan_painter");
		vanillaGen.start(Ic2Items.GRAY_PAINTER).add(Ic2Items.PAINTER).add(Items.f_42490_).finish("gray_painter");
		vanillaGen.start(Ic2Items.GREEN_PAINTER).add(Ic2Items.PAINTER).add(Items.f_42496_).finish("green_painter");
		vanillaGen.start(Ic2Items.LIGHT_GRAY_PAINTER).add(Ic2Items.PAINTER).add(Items.f_42491_).finish("light_gray_painter");
		vanillaGen.start(Ic2Items.LIME_PAINTER).add(Ic2Items.PAINTER).add(Items.f_42540_).finish("lime_painter");
		vanillaGen.start(Ic2Items.MAGENTA_PAINTER).add(Ic2Items.PAINTER).add(Items.f_42537_).finish("magenta_painter");
		vanillaGen.start(Ic2Items.ORANGE_PAINTER).add(Ic2Items.PAINTER).add(Items.f_42536_).finish("orange_painter");
		vanillaGen.start(Ic2Items.PINK_PAINTER).add(Ic2Items.PAINTER).add(Items.f_42489_).finish("pink_painter");
		vanillaGen.start(Ic2Items.PURPLE_PAINTER).add(Ic2Items.PAINTER).add(Items.f_42493_).finish("purple_painter");
		vanillaGen.start(Ic2Items.RED_PAINTER).add(Ic2Items.PAINTER).add(Items.f_42497_).finish("red_painter");
		vanillaGen.start(Ic2Items.WHITE_PAINTER).add(Ic2Items.PAINTER).add(Items.f_42535_).finish("white_painter");
		vanillaGen.start(Ic2Items.YELLOW_PAINTER).add(Ic2Items.PAINTER).add(Items.f_42539_).finish("yellow_painter");
		gen.start(Ic2Items.ELECTRIC_WRENCH).add(Ic2Items.WRENCH).add(Ic2Items.SMALL_POWER_UNIT).finish("electric_wrench");
		gen.start(Ic2Items.ELECTRIC_TREETAP).add(Ic2Items.TREETAP).add(Ic2Items.SMALL_POWER_UNIT).finish("electric_treetap");
		gen.start(Ic2Items.SMALL_URANIUM_235, 9).add(Ic2Items.URANIUM_235).finish("small_uranium_235");
		gen.start(Ic2Items.URANIUM_238, 9).add(Ic2Items.URANIUM_BLOCK).finish("uranium_238");
		gen.start(Ic2Items.REACTOR_PLATING).add(Ic2ItemTags.LEAD_PLATES).add(Ic2Items.ALLOY).finish("reactor_plating");
		gen.start(Ic2Items.CONTAINMENT_REACTOR_PLATING)
			.add(Ic2Items.REACTOR_PLATING)
			.add(Ic2Items.ALLOY)
			.add(Ic2Items.ALLOY)
			.finish("containment_reactor_plating");
		gen.start(Items.f_41999_).add(Fluids.f_76193_, 1000).add(Fluids.f_76193_, 1000).add(Fluids.f_76195_, 1000).add(Fluids.f_76195_, 1000).finish("obsidian");
		gen.start(Items.f_41862_).add(Items.f_41869_).add(Ic2Items.RESIN).hidden().finish("sticky_piston");
		gen.start(Ic2Items.MANUAL_KINETIC_GENERATOR).add(Ic2Items.MACHINE).add(Items.f_41966_).finish("manual_kinetic_generator");
		gen.start(Ic2Items.RUBBER_PLANKS, 4).add(Ic2ItemTags.RUBBER_LOGS).finish("rubber_planks");
		gen.start(Ic2Items.RUBBER_BUTTON).add(Ic2Items.RUBBER_PLANKS).finish("rubber_button");
	}
}
