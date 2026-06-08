package ic2.data.recipe;

import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.data.recipe.helper.Ic2RecipeProvider;
import ic2.data.recipe.helper.WeightedMachineRecipeGenerator;

import java.util.function.Consumer;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

public class MaceratorRecipeProvider extends Ic2RecipeProvider
{
	public MaceratorRecipeProvider(PackOutput packOutput)
	{
		super(packOutput);
	}

	@Override
	protected void generate(Consumer<FinishedRecipe> consumer)
	{
		WeightedMachineRecipeGenerator gen = new WeightedMachineRecipeGenerator(consumer, Ic2RecipeSerializers.MACERATOR);
		gen.add(Items.BONE, 1, Items.WHITE_DYE, 4);
		gen.add(Items.BLAZE_ROD, 1, Items.BLAZE_POWDER, 5);
		gen.add(Items.CLAY, 1, Ic2Items.CLAY_DUST, 2);
		gen.add(Items.COAL_BLOCK, 1, Ic2Items.COAL_DUST, 9);
		gen.add(Items.COBBLESTONE, 1, Items.SAND);
		gen.add(Items.GLOWSTONE, 1, Items.GLOWSTONE_DUST, 4);
		gen.add(Items.GRAVEL, 1, Items.FLINT);
		gen.add(Items.ICE, 1, Items.SNOWBALL);
		gen.add(Items.LAPIS_BLOCK, 1, Items.LAPIS_LAZULI, 9);
		gen.add(Items.NETHERRACK, 1, Ic2Items.NETHERRACK_DUST);
		gen.add(Items.POISONOUS_POTATO, 1, Ic2Items.GRIN_POWDER);
		gen.add(Items.QUARTZ_BLOCK, 1, Items.QUARTZ, 4);
		gen.add(Items.QUARTZ_STAIRS, 1, Items.QUARTZ, 6);
		gen.add(Items.REDSTONE_BLOCK, 1, Items.REDSTONE, 9);
		gen.add(Items.SANDSTONE, 1, Items.SAND);
		gen.add(Items.SPIDER_EYE, 1, Ic2Items.GRIN_POWDER, 2);
		gen.add(Items.STONE, 1, Items.COBBLESTONE);
		gen.add(ItemTags.WOOL, 1, Items.STRING, 2);
		gen.add(Items.BLUE_ICE, 1, Items.PACKED_ICE, 9);
		gen.add(Items.PACKED_ICE, 1, Items.ICE, 9);
		gen.add(Items.PRISMARINE, 1, Items.PRISMARINE_SHARD, 4);
		gen.add(Items.DARK_PRISMARINE, 1, Items.PRISMARINE_SHARD, 8);
		gen.add(Items.PRISMARINE_BRICKS, 1, Items.PRISMARINE_SHARD, 9);
		gen.add(Items.PRISMARINE_STAIRS, 1, Items.PRISMARINE_SHARD, 6);
		gen.add(Items.HONEYCOMB_BLOCK, 1, Items.HONEYCOMB, 4);
		gen.add(Items.CHAIN, 1, Items.IRON_NUGGET, 11);
		gen.add(Items.QUARTZ_BRICKS, 1, Items.QUARTZ, 4);
		gen.add(Items.QUARTZ_PILLAR, 1, Items.QUARTZ, 4);
		gen.add(Items.CHISELED_QUARTZ_BLOCK, 1, Items.QUARTZ, 4);
		gen.add(Items.SOUL_SOIL, 1, Items.SOUL_SAND);
		gen.add(Items.AMETHYST_BLOCK, 1, Items.AMETHYST_SHARD, 4);
		gen.add(Items.DRIPSTONE_BLOCK, 1, Items.POINTED_DRIPSTONE, 4);
		gen.add(Items.DEEPSLATE, 1, Items.COBBLED_DEEPSLATE);
		gen.add(Items.RAW_COPPER_BLOCK, 1, Items.RAW_COPPER, 9);
		gen.add(Items.RAW_IRON_BLOCK, 1, Items.RAW_IRON, 9);
		gen.add(Items.RAW_GOLD_BLOCK, 1, Items.RAW_GOLD, 9);
		gen.add(Ic2Items.BIO_CHAFF, 1, Items.DIRT);
		gen.add(Ic2Items.COFFEE_BEANS, 3, Ic2Items.COFFEE_POWDER);
		gen.add(Ic2Items.ENERGY_CRYSTAL, 1, Ic2Items.ENERGIUM_DUST, 9);
		gen.add(Ic2Items.FUEL_ROD, 1, Ic2Items.IRON_DUST);
		gen.add(Ic2Items.IRIDIUM, 1, Ic2Items.IRIDIUM_SHARD, 9);
		gen.add(Ic2Items.TIN_CAN, 2, Ic2Items.TIN_DUST);
		gen.add(Items.CACTUS, 8, Ic2Items.BIO_CHAFF);
		gen.add(Items.CARROT, 8, Ic2Items.BIO_CHAFF);
		gen.add(ItemTags.LEAVES, 8, Ic2Items.BIO_CHAFF);
		gen.add(Items.MELON, 8, Ic2Items.BIO_CHAFF);
		gen.add(Items.MELON_SEEDS, 16, Ic2Items.BIO_CHAFF);
		gen.add(Ic2Items.PLANT_BALL, 1, Ic2Items.BIO_CHAFF);
		gen.add(Items.POTATO, 8, Ic2Items.BIO_CHAFF);
		gen.add(Items.PUMPKIN, 8, Ic2Items.BIO_CHAFF);
		gen.add(Items.PUMPKIN_SEEDS, 16, Ic2Items.BIO_CHAFF);
		gen.add(ItemTags.SAPLINGS, 4, Ic2Items.BIO_CHAFF);
		gen.add(Items.SUGAR_CANE, 8, Ic2Items.BIO_CHAFF);
		gen.add(Items.TALL_GRASS, 8, Ic2Items.BIO_CHAFF);
		gen.add(Ic2Items.WEED, 32, Ic2Items.BIO_CHAFF);
		gen.add(Items.WHEAT, 8, Ic2Items.BIO_CHAFF);
		gen.add(Items.WHEAT_SEEDS, 16, Ic2Items.BIO_CHAFF);
		gen.add(Ic2ItemTags.BRONZE_INGOTS, 1, Ic2Items.BRONZE_DUST);
		gen.add(Items.COAL, 1, Ic2Items.COAL_DUST);
		gen.add(Items.COPPER_INGOT, 1, Ic2Items.COPPER_DUST);
		gen.add(Items.DIAMOND, 1, Ic2Items.DIAMOND_DUST);
		gen.add(Items.GOLD_INGOT, 1, Ic2Items.GOLD_DUST);
		gen.add(Items.IRON_INGOT, 1, Ic2Items.IRON_DUST);
		gen.add(Items.LAPIS_LAZULI, 1, Ic2Items.LAPIS_DUST);
		gen.add(Ic2ItemTags.LEAD_INGOTS, 1, Ic2Items.LEAD_DUST);
		gen.add(Items.OBSIDIAN, 1, Ic2Items.OBSIDIAN_DUST);
		gen.add(Ic2ItemTags.SILVER_INGOTS, 1, Ic2Items.SILVER_DUST);
		gen.add(Ic2ItemTags.STEEL_INGOTS, 1, Ic2Items.IRON_DUST);
		gen.add(Ic2ItemTags.TIN_INGOTS, 1, Ic2Items.TIN_DUST);
		gen.add(Ic2ItemTags.BRONZE_PLATES, 1, Ic2Items.BRONZE_DUST);
		gen.add(Ic2ItemTags.COPPER_PLATES, 1, Ic2Items.COPPER_DUST);
		gen.add(Ic2ItemTags.GOLD_PLATES, 1, Ic2Items.GOLD_DUST);
		gen.add(Ic2ItemTags.IRON_PLATES, 1, Ic2Items.IRON_DUST);
		gen.add(Ic2ItemTags.LAPIS_PLATES, 1, Ic2Items.LAPIS_DUST);
		gen.add(Ic2ItemTags.LEAD_PLATES, 1, Ic2Items.LEAD_DUST);
		gen.add(Ic2ItemTags.OBSIDIAN_PLATES, 1, Ic2Items.OBSIDIAN_DUST);
		gen.add(Ic2ItemTags.TIN_PLATES, 1, Ic2Items.TIN_DUST);
		gen.add(Ic2Items.DENSE_BRONZE_PLATE, 1, Ic2Items.BRONZE_DUST, 9);
		gen.add(Ic2Items.DENSE_COPPER_PLATE, 1, Ic2Items.COPPER_DUST, 9);
		gen.add(Ic2Items.DENSE_GOLD_PLATE, 1, Ic2Items.GOLD_DUST, 9);
		gen.add(Ic2Items.DENSE_IRON_PLATE, 1, Ic2Items.IRON_DUST, 9);
		gen.add(Ic2Items.DENSE_LAPIS_PLATE, 1, Ic2Items.LAPIS_DUST, 9);
		gen.add(Ic2Items.DENSE_LEAD_PLATE, 1, Ic2Items.LEAD_DUST, 9);
		gen.add(Ic2Items.DENSE_OBSIDIAN_PLATE, 1, Ic2Items.OBSIDIAN_DUST, 9);
		gen.add(Ic2Items.DENSE_TIN_PLATE, 1, Ic2Items.TIN_DUST, 9);
		gen.add(Ic2Items.CRUSHED_COPPER, 1, Ic2Items.COPPER_DUST);
		gen.add(Ic2Items.CRUSHED_GOLD, 1, Ic2Items.GOLD_DUST);
		gen.add(Ic2Items.CRUSHED_IRON, 1, Ic2Items.IRON_DUST);
		gen.add(Ic2Items.CRUSHED_LEAD, 1, Ic2Items.LEAD_DUST);
		gen.add(Ic2Items.CRUSHED_SILVER, 1, Ic2Items.SILVER_DUST);
		gen.add(Ic2Items.CRUSHED_TIN, 1, Ic2Items.TIN_DUST);
		gen.add(Ic2Items.PURIFIED_COPPER, 1, Ic2Items.COPPER_DUST);
		gen.add(Ic2Items.PURIFIED_GOLD, 1, Ic2Items.GOLD_DUST);
		gen.add(Ic2Items.PURIFIED_IRON, 1, Ic2Items.IRON_DUST);
		gen.add(Ic2Items.PURIFIED_LEAD, 1, Ic2Items.LEAD_DUST);
		gen.add(Ic2Items.PURIFIED_SILVER, 1, Ic2Items.SILVER_DUST);
		gen.add(Ic2Items.PURIFIED_TIN, 1, Ic2Items.TIN_DUST);
		int[][] countAndWeights = new int[][] { { 2, 6 }, { 3, 2 }, { 4, 2 } };
		gen.add(ItemTags.COPPER_ORES, 1, WeightedMachineRecipeGenerator.WeightedItemStack.of(Items.RAW_COPPER, new int[][] { { 6, 4 }, { 9, 3 }, { 10, 3 } }));
		gen.add(Items.RAW_COPPER, 1, Ic2Items.CRUSHED_COPPER, 2);
		gen.add(ItemTags.GOLD_ORES, 1, WeightedMachineRecipeGenerator.WeightedItemStack.of(Items.RAW_GOLD, countAndWeights));
		gen.add(Items.RAW_GOLD, 1, Ic2Items.CRUSHED_GOLD, 2);
		gen.add(ItemTags.IRON_ORES, 1, WeightedMachineRecipeGenerator.WeightedItemStack.of(Items.RAW_IRON, countAndWeights));
		gen.add(Items.RAW_IRON, 1, Ic2Items.CRUSHED_IRON, 2);
		gen.add(Ic2ItemTags.LEAD_ORES, 1, WeightedMachineRecipeGenerator.WeightedItemStack.of(Ic2Items.RAW_LEAD, countAndWeights));
		gen.add(Ic2ItemTags.LEAD_RAW_ORES, 1, Ic2Items.CRUSHED_LEAD, 2);
		gen.add(Ic2ItemTags.TIN_ORES, 1, WeightedMachineRecipeGenerator.WeightedItemStack.of(Ic2Items.RAW_TIN, countAndWeights));
		gen.add(Ic2ItemTags.TIN_RAW_ORES, 1, Ic2Items.CRUSHED_TIN, 2);
		gen.add(Ic2ItemTags.URANIUM_ORES, 1, WeightedMachineRecipeGenerator.WeightedItemStack.of(Ic2Items.RAW_URANIUM, countAndWeights));
		gen.add(Ic2ItemTags.URANIUM_RAW_ORES, 1, Ic2Items.CRUSHED_URANIUM, 2);
		gen.add(Ic2ItemTags.SILVER_ORES, 1, Ic2Items.CRUSHED_SILVER, 2);
		gen.add(Ic2Items.RAW_TIN_BLOCK, 1, Ic2Items.RAW_TIN, 9);
		gen.add(Ic2Items.RAW_LEAD_BLOCK, 1, Ic2Items.RAW_LEAD, 9);
		gen.add(Ic2Items.RAW_URANIUM_BLOCK, 1, Ic2Items.RAW_URANIUM, 9);
		gen.add(Ic2Items.LEAD_BLOCK, 1, Ic2Items.LEAD_INGOT, 9);
		gen.add(Ic2Items.TIN_BLOCK, 1, Ic2Items.TIN_INGOT, 9);
		gen.add(Ic2Items.URANIUM_BLOCK, 1, Ic2Items.URANIUM_INGOT, 9);
	}
}
