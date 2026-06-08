package ic2.data.recipe;

import ic2.core.ref.Ic2Items;
import ic2.data.recipe.helper.Ic2RecipeProvider;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class SmeltingProvider extends Ic2RecipeProvider
{
	public SmeltingProvider(DataGenerator generator)
	{
		super(generator);
	}

	@Override
	public String getName()
	{
		return "IC2 Smelting Recipes";
	}

	@Override
	protected void generate(Consumer<FinishedRecipe> consumer)
	{
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.TIN_ORE }), Ic2Items.TIN_INGOT, 0.5F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.TIN_ORE))
			.group("tin_ingot")
			.save(consumer, "ic2:smelting/tin_ingot");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.LEAD_ORE }), Ic2Items.LEAD_INGOT, 0.5F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.LEAD_ORE))
			.group("lead_ingot")
			.save(consumer, "ic2:smelting/lead_ingot");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.URANIUM_ORE }), Ic2Items.URANIUM_INGOT, 0.5F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.URANIUM_ORE))
			.group("uranium_ingot")
			.save(consumer, "ic2:smelting/uranium_ingot");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.DEEPSLATE_TIN_ORE }), Ic2Items.TIN_INGOT, 0.5F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.DEEPSLATE_TIN_ORE))
			.group("tin_ingot")
			.save(consumer, "ic2:smelting/tin_ingot_from_deepslate");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.DEEPSLATE_LEAD_ORE }), Ic2Items.LEAD_INGOT, 0.5F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.DEEPSLATE_LEAD_ORE))
			.group("lead_ingot")
			.save(consumer, "ic2:smelting/lead_ingot_from_deepslate");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.DEEPSLATE_URANIUM_ORE }), Ic2Items.URANIUM_INGOT, 0.5F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.DEEPSLATE_URANIUM_ORE))
			.group("uranium_ingot")
			.save(consumer, "ic2:smelting/uranium_ingot_from_deepslate");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.CRUSHED_IRON }), Items.IRON_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.CRUSHED_IRON))
			.group("iron_ingot")
			.save(consumer, "ic2:smelting/iron_ingot_from_crushed_iron");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.CRUSHED_GOLD }), Items.GOLD_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.CRUSHED_GOLD))
			.group("gold_ingot")
			.save(consumer, "ic2:smelting/gold_ingot_from_crushed_gold");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.CRUSHED_COPPER }), Items.COPPER_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.CRUSHED_COPPER))
			.group("copper_ingot")
			.save(consumer, "ic2:smelting/copper_ingot_from_crushed_copper");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.CRUSHED_TIN }), Ic2Items.TIN_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.CRUSHED_TIN))
			.group("tin_ingot")
			.save(consumer, "ic2:smelting/tin_ingot_from_crushed_tin");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.CRUSHED_LEAD }), Ic2Items.LEAD_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.CRUSHED_LEAD))
			.group("lead_ingot")
			.save(consumer, "ic2:smelting/lead_ingot_from_crushed_lead");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.CRUSHED_URANIUM }), Ic2Items.URANIUM_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.CRUSHED_URANIUM))
			.group("uranium_ingot")
			.save(consumer, "ic2:smelting/uranium_ingot_from_crushed_uranium");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.CRUSHED_SILVER }), Ic2Items.SILVER_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.CRUSHED_SILVER))
			.group("silver_ingot")
			.save(consumer, "ic2:smelting/silver_ingot_from_crushed_silver");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.PURIFIED_IRON }), Items.IRON_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.PURIFIED_IRON))
			.group("iron_ingot")
			.save(consumer, "ic2:smelting/iron_ingot_from_purified_iron");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.PURIFIED_GOLD }), Items.GOLD_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.PURIFIED_GOLD))
			.group("gold_ingot")
			.save(consumer, "ic2:smelting/gold_ingot_from_purified_gold");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.PURIFIED_COPPER }), Items.COPPER_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.PURIFIED_COPPER))
			.group("copper_ingot")
			.save(consumer, "ic2:smelting/copper_ingot_from_purified_copper");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.PURIFIED_TIN }), Ic2Items.TIN_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.PURIFIED_TIN))
			.group("tin_ingot")
			.save(consumer, "ic2:smelting/tin_ingot_from_purified_tin");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.PURIFIED_LEAD }), Ic2Items.LEAD_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.PURIFIED_LEAD))
			.group("lead_ingot")
			.save(consumer, "ic2:smelting/lead_ingot_from_purified_lead");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.PURIFIED_URANIUM }), Ic2Items.URANIUM_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.PURIFIED_URANIUM))
			.group("uranium_ingot")
			.save(consumer, "ic2:smelting/uranium_ingot_from_purified_uranium");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.PURIFIED_SILVER }), Ic2Items.SILVER_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.PURIFIED_SILVER))
			.group("silver_ingot")
			.save(consumer, "ic2:smelting/silver_ingot_from_purified_silver");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.IRON_DUST }), Items.IRON_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.IRON_DUST))
			.group("iron_ingot")
			.save(consumer, "ic2:smelting/iron_ingot_from_iron_dust");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.GOLD_DUST }), Items.GOLD_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.GOLD_DUST))
			.group("gold_ingot")
			.save(consumer, "ic2:smelting/gold_ingot_from_gold_dust");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.COPPER_DUST }), Items.COPPER_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.COPPER_DUST))
			.group("copper_ingot")
			.save(consumer, "ic2:smelting/copper_ingot_from_copper_dust");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.TIN_DUST }), Ic2Items.TIN_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.TIN_DUST))
			.group("tin_ingot")
			.save(consumer, "ic2:smelting/tin_ingot_from_tin_dust");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.LEAD_DUST }), Ic2Items.LEAD_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.LEAD_DUST))
			.group("lead_ingot")
			.save(consumer, "ic2:smelting/lead_ingot_from_lead_dust");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.SILVER_DUST }), Ic2Items.SILVER_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.SILVER_DUST))
			.group("copper_ingot")
			.save(consumer, "ic2:smelting/silver_ingot_from_silver_dust");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.BRONZE_DUST }), Ic2Items.BRONZE_INGOT, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.BRONZE_DUST))
			.group("bronze_ingot")
			.save(consumer, "ic2:smelting/bronze_ingot_from_bronze_dust");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.RUBBER_LOG }), Items.JUNGLE_LOG, 0.1F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.RUBBER_LOG))
			.save(consumer, "ic2:smelting/jungle_log_from_rubber_log");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.RESIN }), Ic2Items.RUBBER, 0.3F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.RESIN))
			.save(consumer, "ic2:smelting/rubber");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.COAL_FUEL_DUST }), Ic2Items.COAL_DUST, 0.0F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.COAL_FUEL_DUST))
			.save(consumer, "ic2:smelting/coal_dust");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.COLD_COFFEE_MUG }), Ic2Items.DARK_COFFEE_MUG, 0.1F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.COLD_COFFEE_MUG))
			.save(consumer, "ic2:smelting/dark_coffee_mug");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(new ItemLike[] { Ic2Items.RAW_CRYSTAL_MEMORY }), Ic2Items.CRYSTAL_MEMORY, 0.1F, 200)
			.unlockedBy("has_item", conditionsFromItem(Ic2Items.RAW_CRYSTAL_MEMORY))
			.save(consumer, "ic2:smelting/crystal_memory");
	}
}
