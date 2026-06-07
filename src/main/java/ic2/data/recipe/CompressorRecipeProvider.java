package ic2.data.recipe;

import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.data.recipe.helper.BasicMachineRecipeGenerator;
import ic2.data.recipe.helper.Ic2RecipeProvider;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

public class CompressorRecipeProvider extends Ic2RecipeProvider
{
	public CompressorRecipeProvider(DataGenerator generator)
	{
		super(generator);
	}

	@Override
	protected void generate(Consumer<FinishedRecipe> consumer)
	{
		BasicMachineRecipeGenerator<? extends BasicMachineRecipeGenerator<?>> gen = (BasicMachineRecipeGenerator<? extends BasicMachineRecipeGenerator<?>>) (new BasicMachineRecipeGenerator<>(
			consumer, Ic2RecipeSerializers.COMPRESSOR
		));
		gen.add(Fluids.f_76193_, 1000, Blocks.f_50127_);
		gen.add(Ic2Items.SMALL_URANIUM_235, 9, Ic2Items.URANIUM_235);
		gen.add(Ic2Items.SMALL_PLUTONIUM, 9, Ic2Items.PLUTONIUM);
		gen.add(Ic2Items.IRIDIUM_SHARD, 9, Ic2Items.IRIDIUM_ORE);
		gen.add(Items.f_41830_, 4, Blocks.f_50062_);
		gen.add(Items.f_42593_, 5, Items.f_42585_);
		gen.add(Items.f_42461_, 4, Blocks.f_50129_);
		gen.add(Items.f_42460_, 4, Blocks.f_50076_);
		gen.add(Items.f_42691_, 4, Blocks.f_50197_);
		gen.add(Blocks.f_50127_, 1, Blocks.f_50126_);
		gen.add(Blocks.f_50126_, 2, Blocks.f_50354_);
		gen.add(Items.f_42452_, 4, Blocks.f_50127_);
		gen.add(Items.f_42525_, 4, Blocks.f_50141_);
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
		gen.add(Items.f_42416_, 9, Blocks.f_50075_);
		gen.add(Items.f_42417_, 9, Blocks.f_50074_);
		gen.add(Items.f_151052_, 9, Blocks.f_152504_);
		gen.add(Items.f_42534_, 9, Blocks.f_50060_);
		gen.add(Items.REDSTONE, 9, Blocks.f_50330_);
		gen.add(Ic2ItemTags.BRONZE_INGOTS, 9, Ic2Items.BRONZE_BLOCK);
		gen.add(Ic2ItemTags.STEEL_INGOTS, 9, Ic2Items.STEEL_BLOCK);
		gen.add(Ic2ItemTags.LEAD_INGOTS, 9, Ic2Items.LEAD_BLOCK);
		gen.add(Ic2ItemTags.TIN_INGOTS, 9, Ic2Items.TIN_BLOCK);
		gen.add(Ic2ItemTags.SILVER_INGOTS, 9, Ic2Items.SILVER_BLOCK);
		gen.add(Ic2Items.ENERGIUM_DUST, 9, Ic2Items.ENERGY_CRYSTAL);
		gen.add(Items.f_42201_, 9, Items.f_42363_);
		gen.add(Items.f_42784_, 4, Items.f_42789_);
		gen.add(Items.f_42692_, 4, Items.f_42157_);
		gen.add(Items.f_151049_, 4, Items.f_150998_);
		gen.add(Items.f_151015_, 3, Items.f_151016_, 2);
		gen.add(Items.f_151051_, 9, Items.f_150996_);
		gen.add(Items.f_151050_, 9, Items.f_150995_);
		gen.add(Items.f_151053_, 9, Items.f_150997_);
		gen.add(Ic2Items.RAW_TIN, 9, Ic2Items.RAW_TIN_BLOCK);
		gen.add(Ic2Items.RAW_LEAD, 9, Ic2Items.RAW_LEAD_BLOCK);
		gen.add(Ic2Items.RAW_URANIUM, 9, Ic2Items.RAW_URANIUM_BLOCK);
		gen.add(Items.f_220193_, 4, Items.f_220192_);
	}
}
