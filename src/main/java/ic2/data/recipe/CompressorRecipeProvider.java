package ic2.data.recipe;

import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.data.recipe.helper.BasicMachineRecipeGenerator;
import ic2.data.recipe.helper.Ic2RecipeProvider;

import java.util.function.Consumer;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

public class CompressorRecipeProvider extends Ic2RecipeProvider
{
	public CompressorRecipeProvider(PackOutput packOutput)
	{
		super(packOutput);
	}

	@Override
	protected void generate(Consumer<FinishedRecipe> consumer)
	{
		BasicMachineRecipeGenerator<? extends BasicMachineRecipeGenerator<?>> gen = (BasicMachineRecipeGenerator<? extends BasicMachineRecipeGenerator<?>>) (new BasicMachineRecipeGenerator<>(
			consumer, Ic2RecipeSerializers.COMPRESSOR
		));
		gen.add(Fluids.WATER, 1000, Blocks.SNOW_BLOCK);
		gen.add(Ic2Items.SMALL_URANIUM_235, 9, Ic2Items.URANIUM_235);
		gen.add(Ic2Items.SMALL_PLUTONIUM, 9, Ic2Items.PLUTONIUM);
		gen.add(Ic2Items.IRIDIUM_SHARD, 9, Ic2Items.IRIDIUM_ORE);
		gen.add(Items.SAND, 4, Blocks.SANDSTONE);
		gen.add(Items.BLAZE_POWDER, 5, Items.BLAZE_ROD);
		gen.add(Items.CLAY_BALL, 4, Blocks.CLAY);
		gen.add(Items.BRICK, 4, Blocks.BRICKS);
		gen.add(Items.NETHER_BRICK, 4, Blocks.NETHER_BRICKS);
		gen.add(Blocks.SNOW_BLOCK, 1, Blocks.ICE);
		gen.add(Blocks.ICE, 2, Blocks.PACKED_ICE);
		gen.add(Items.SNOWBALL, 4, Blocks.SNOW_BLOCK);
		gen.add(Items.GLOWSTONE_DUST, 4, Blocks.GLOWSTONE);
		gen.add(Ic2Items.MIXED_METAL_INGOT, 1, Ic2Items.ALLOY);
		gen.add(Ic2Items.CARBON_MESH, 1, Ic2Items.CARBON_PLATE);
		gen.add(Ic2Items.COAL_BALL, 1, Ic2Items.COAL_BLOCK);
		gen.add(Ic2Items.COAL_CHUNK, 1, Ic2Items.INDUTRIAL_DIAMOND);
		gen.add(Ic2Items.EMPTY_CELL, 1, Ic2Items.AIR_CELL);
		gen.add(Ic2Items.SMALL_BRONZE_DUST, 9, Ic2Items.BRONZE_DUST);
		gen.add(Ic2Items.SMALL_COPPER_DUST, 9, Ic2Items.COPPER_DUST);
		gen.add(Ic2Items.SMALL_GOLD_DUST, 9, Ic2Items.GOLD_DUST);
		gen.add(Ic2Items.SMALL_IRON_DUST, 9, Ic2Items.IRON_DUST);
		gen.add(Ic2Items.SMALL_LAPIS_DUST, 9, Ic2Items.LAPIS_DUST);
		gen.add(Ic2Items.SMALL_LEAD_DUST, 9, Ic2Items.LEAD_DUST);
		gen.add(Ic2Items.SMALL_LITHIUM_DUST, 9, Ic2Items.LITHIUM_DUST);
		gen.add(Ic2Items.SMALL_OBSIDIAN_DUST, 9, Ic2Items.OBSIDIAN_DUST);
		gen.add(Ic2Items.SMALL_SILVER_DUST, 9, Ic2Items.SILVER_DUST);
		gen.add(Ic2Items.SMALL_SULFUR_DUST, 9, Ic2Items.SULFUR_DUST);
		gen.add(Ic2Items.SMALL_TIN_DUST, 9, Ic2Items.TIN_DUST);
		gen.add(Ic2ItemTags.LAPIS_DUSTS, 1, Ic2Items.LAPIS_PLATE);
		gen.add(Ic2ItemTags.OBSIDIAN_DUSTS, 1, Ic2Items.OBSIDIAN_PLATE);
		gen.add(Ic2ItemTags.IRON_PLATES, 9, Ic2Items.DENSE_IRON_PLATE);
		gen.add(Ic2ItemTags.GOLD_PLATES, 9, Ic2Items.DENSE_GOLD_PLATE);
		gen.add(Ic2ItemTags.LEAD_PLATES, 9, Ic2Items.DENSE_LEAD_PLATE);
		gen.add(Ic2ItemTags.BRONZE_PLATES, 9, Ic2Items.DENSE_BRONZE_PLATE);
		gen.add(Ic2ItemTags.TIN_PLATES, 9, Ic2Items.DENSE_TIN_PLATE);
		gen.add(Ic2ItemTags.COPPER_PLATES, 9, Ic2Items.DENSE_COPPER_PLATE);
		gen.add(Ic2ItemTags.LAPIS_PLATES, 9, Ic2Items.DENSE_LAPIS_PLATE);
		gen.add(Ic2ItemTags.OBSIDIAN_PLATES, 9, Ic2Items.DENSE_OBSIDIAN_PLATE);
		gen.add(Ic2ItemTags.STEEL_PLATES, 9, Ic2Items.DENSE_STEEL_PLATE);
		gen.add(Items.IRON_INGOT, 9, Blocks.IRON_BLOCK);
		gen.add(Items.GOLD_INGOT, 9, Blocks.GOLD_BLOCK);
		gen.add(Items.COPPER_INGOT, 9, Blocks.COPPER_BLOCK);
		gen.add(Items.LAPIS_LAZULI, 9, Blocks.LAPIS_BLOCK);
		gen.add(Items.REDSTONE, 9, Blocks.REDSTONE_BLOCK);
		gen.add(Ic2ItemTags.BRONZE_INGOTS, 9, Ic2Items.BRONZE_BLOCK);
		gen.add(Ic2ItemTags.STEEL_INGOTS, 9, Ic2Items.STEEL_BLOCK);
		gen.add(Ic2ItemTags.LEAD_INGOTS, 9, Ic2Items.LEAD_BLOCK);
		gen.add(Ic2ItemTags.TIN_INGOTS, 9, Ic2Items.TIN_BLOCK);
		gen.add(Ic2ItemTags.SILVER_INGOTS, 9, Ic2Items.SILVER_BLOCK);
		gen.add(Ic2Items.ENERGIUM_DUST, 9, Ic2Items.ENERGY_CRYSTAL);
		gen.add(Items.PACKED_ICE, 9, Items.BLUE_ICE);
		gen.add(Items.HONEYCOMB, 4, Items.HONEYCOMB_BLOCK);
		gen.add(Items.QUARTZ, 4, Items.QUARTZ_BLOCK);
		gen.add(Items.AMETHYST_SHARD, 4, Items.AMETHYST_BLOCK);
		gen.add(Items.MOSS_CARPET, 3, Items.MOSS_BLOCK, 2);
		gen.add(Items.RAW_COPPER, 9, Items.RAW_COPPER_BLOCK);
		gen.add(Items.RAW_IRON, 9, Items.RAW_IRON_BLOCK);
		gen.add(Items.RAW_GOLD, 9, Items.RAW_GOLD_BLOCK);
		gen.add(Ic2Items.RAW_TIN, 9, Ic2Items.RAW_TIN_BLOCK);
		gen.add(Ic2Items.RAW_LEAD, 9, Ic2Items.RAW_LEAD_BLOCK);
		gen.add(Ic2Items.RAW_URANIUM, 9, Ic2Items.RAW_URANIUM_BLOCK);
		gen.add(Items.SCULK_VEIN, 4, Items.SCULK);
	}
}
