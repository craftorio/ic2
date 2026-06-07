package ic2.data.recipe;

import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.data.recipe.helper.Ic2RecipeProvider;
import ic2.data.recipe.helper.WeightedMachineRecipeGenerator;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

public class MaceratorRecipeProvider extends Ic2RecipeProvider
{
	public MaceratorRecipeProvider(DataGenerator generator)
	{
		super(generator);
	}

	@Override
	protected void generate(Consumer<FinishedRecipe> consumer)
	{
		WeightedMachineRecipeGenerator gen = new WeightedMachineRecipeGenerator(consumer, Ic2RecipeSerializers.MACERATOR);
		gen.add(Items.f_42500_, 1, Items.f_42535_, 4);
		gen.add(Items.f_42585_, 1, Items.f_42593_, 5);
		gen.add(Items.f_41983_, 1, Ic2Items.CLAY_DUST, 2);
		gen.add(Items.f_42200_, 1, Ic2Items.COAL_DUST, 9);
		gen.add(Items.f_42594_, 1, Items.f_41830_);
		gen.add(Items.f_42054_, 1, Items.f_42525_, 4);
		gen.add(Items.f_41832_, 1, Items.f_42484_);
		gen.add(Items.f_41980_, 1, Items.f_42452_);
		gen.add(Items.f_41854_, 1, Items.f_42534_, 9);
		gen.add(Items.f_42048_, 1, Ic2Items.NETHERRACK_DUST);
		gen.add(Items.f_42675_, 1, Ic2Items.GRIN_POWDER);
		gen.add(Items.f_42157_, 1, Items.f_42692_, 4);
		gen.add(Items.f_42160_, 1, Items.f_42692_, 6);
		gen.add(Items.f_42153_, 1, Items.REDSTONE, 9);
		gen.add(Items.f_41856_, 1, Items.f_41830_);
		gen.add(Items.f_42591_, 1, Ic2Items.GRIN_POWDER, 2);
		gen.add(Items.f_41905_, 1, Items.f_42594_);
		gen.add(ItemTags.f_13167_, 1, Items.f_42401_, 2);
		gen.add(Items.f_42363_, 1, Items.f_42201_, 9);
		gen.add(Items.f_42201_, 1, Items.f_41980_, 9);
		gen.add(Items.f_42192_, 1, Items.f_42695_, 4);
		gen.add(Items.f_42194_, 1, Items.f_42695_, 8);
		gen.add(Items.f_42193_, 1, Items.f_42695_, 9);
		gen.add(Items.f_42195_, 1, Items.f_42695_, 6);
		gen.add(Items.f_42789_, 1, Items.f_42784_, 4);
		gen.add(Items.f_42026_, 1, Items.f_42749_, 11);
		gen.add(Items.f_42158_, 1, Items.f_42692_, 4);
		gen.add(Items.f_42159_, 1, Items.f_42692_, 4);
		gen.add(Items.f_42156_, 1, Items.f_42692_, 4);
		gen.add(Items.f_42050_, 1, Items.f_42049_);
		gen.add(Items.f_150998_, 1, Items.f_151049_, 4);
		gen.add(Items.f_151054_, 1, Items.f_151087_, 4);
		gen.add(Items.f_151034_, 1, Items.f_151035_);
		gen.add(Items.f_150996_, 1, Items.f_151051_, 9);
		gen.add(Items.f_150995_, 1, Items.f_151050_, 9);
		gen.add(Items.f_150997_, 1, Items.f_151053_, 9);
		gen.add(Ic2Items.BIO_CHAFF, 1, Items.f_42329_);
		gen.add(Ic2Items.COFFEE_BEANS, 3, Ic2Items.COFFEE_POWDER);
		gen.add(Ic2Items.ENERGY_CRYSTAL, 1, Ic2Items.ENERGIUM_DUST, 9);
		gen.add(Ic2Items.FUEL_ROD, 1, Ic2Items.IRON_DUST);
		gen.add(Ic2Items.IRIDIUM, 1, Ic2Items.IRIDIUM_SHARD, 9);
		gen.add(Ic2Items.TIN_CAN, 2, Ic2Items.TIN_DUST);
		gen.add(Items.f_41982_, 8, Ic2Items.BIO_CHAFF);
		gen.add(Items.f_42619_, 8, Ic2Items.BIO_CHAFF);
		gen.add(ItemTags.f_13143_, 8, Ic2Items.BIO_CHAFF);
		gen.add(Items.f_42028_, 8, Ic2Items.BIO_CHAFF);
		gen.add(Items.f_42578_, 16, Ic2Items.BIO_CHAFF);
		gen.add(Ic2Items.PLANT_BALL, 1, Ic2Items.BIO_CHAFF);
		gen.add(Items.f_42620_, 8, Ic2Items.BIO_CHAFF);
		gen.add(Items.f_42046_, 8, Ic2Items.BIO_CHAFF);
		gen.add(Items.f_42577_, 16, Ic2Items.BIO_CHAFF);
		gen.add(ItemTags.f_13180_, 4, Ic2Items.BIO_CHAFF);
		gen.add(Items.f_41909_, 8, Ic2Items.BIO_CHAFF);
		gen.add(Items.f_42210_, 8, Ic2Items.BIO_CHAFF);
		gen.add(Ic2Items.WEED, 32, Ic2Items.BIO_CHAFF);
		gen.add(Items.f_42405_, 8, Ic2Items.BIO_CHAFF);
		gen.add(Items.f_42404_, 16, Ic2Items.BIO_CHAFF);
		gen.add(Ic2ItemTags.BRONZE_INGOTS, 1, Ic2Items.BRONZE_DUST);
		gen.add(Items.f_42413_, 1, Ic2Items.COAL_DUST);
		gen.add(Items.f_151052_, 1, Ic2Items.COPPER_DUST);
		gen.add(Items.f_42415_, 1, Ic2Items.DIAMOND_DUST);
		gen.add(Items.f_42417_, 1, Ic2Items.GOLD_DUST);
		gen.add(Items.f_42416_, 1, Ic2Items.IRON_DUST);
		gen.add(Items.f_42534_, 1, Ic2Items.LAPIS_DUST);
		gen.add(Ic2ItemTags.LEAD_INGOTS, 1, Ic2Items.LEAD_DUST);
		gen.add(Items.f_41999_, 1, Ic2Items.OBSIDIAN_DUST);
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
		gen.add(ItemTags.f_144318_, 1, WeightedMachineRecipeGenerator.WeightedItemStack.of(Items.f_151051_, new int[][] { { 6, 4 }, { 9, 3 }, { 10, 3 } }));
		gen.add(Items.f_151051_, 1, Ic2Items.CRUSHED_COPPER, 2);
		gen.add(ItemTags.f_13152_, 1, WeightedMachineRecipeGenerator.WeightedItemStack.of(Items.f_151053_, countAndWeights));
		gen.add(Items.f_151053_, 1, Ic2Items.CRUSHED_GOLD, 2);
		gen.add(ItemTags.f_144312_, 1, WeightedMachineRecipeGenerator.WeightedItemStack.of(Items.f_151050_, countAndWeights));
		gen.add(Items.f_151050_, 1, Ic2Items.CRUSHED_IRON, 2);
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
