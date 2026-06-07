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
	public String m_6055_()
	{
		return "IC2 Smelting Recipes";
	}

	@Override
	protected void generate(Consumer<FinishedRecipe> consumer)
	{
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.TIN_ORE }), Ic2Items.TIN_INGOT, 0.5F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.TIN_ORE))
			.m_126145_("tin_ingot")
			.m_176500_(consumer, "ic2:smelting/tin_ingot");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.LEAD_ORE }), Ic2Items.LEAD_INGOT, 0.5F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.LEAD_ORE))
			.m_126145_("lead_ingot")
			.m_176500_(consumer, "ic2:smelting/lead_ingot");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.URANIUM_ORE }), Ic2Items.URANIUM_INGOT, 0.5F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.URANIUM_ORE))
			.m_126145_("uranium_ingot")
			.m_176500_(consumer, "ic2:smelting/uranium_ingot");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.DEEPSLATE_TIN_ORE }), Ic2Items.TIN_INGOT, 0.5F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.DEEPSLATE_TIN_ORE))
			.m_126145_("tin_ingot")
			.m_176500_(consumer, "ic2:smelting/tin_ingot_from_deepslate");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.DEEPSLATE_LEAD_ORE }), Ic2Items.LEAD_INGOT, 0.5F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.DEEPSLATE_LEAD_ORE))
			.m_126145_("lead_ingot")
			.m_176500_(consumer, "ic2:smelting/lead_ingot_from_deepslate");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.DEEPSLATE_URANIUM_ORE }), Ic2Items.URANIUM_INGOT, 0.5F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.DEEPSLATE_URANIUM_ORE))
			.m_126145_("uranium_ingot")
			.m_176500_(consumer, "ic2:smelting/uranium_ingot_from_deepslate");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.CRUSHED_IRON }), Items.f_42416_, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.CRUSHED_IRON))
			.m_126145_("iron_ingot")
			.m_176500_(consumer, "ic2:smelting/iron_ingot_from_crushed_iron");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.CRUSHED_GOLD }), Items.f_42417_, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.CRUSHED_GOLD))
			.m_126145_("gold_ingot")
			.m_176500_(consumer, "ic2:smelting/gold_ingot_from_crushed_gold");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.CRUSHED_COPPER }), Items.f_151052_, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.CRUSHED_COPPER))
			.m_126145_("copper_ingot")
			.m_176500_(consumer, "ic2:smelting/copper_ingot_from_crushed_copper");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.CRUSHED_TIN }), Ic2Items.TIN_INGOT, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.CRUSHED_TIN))
			.m_126145_("tin_ingot")
			.m_176500_(consumer, "ic2:smelting/tin_ingot_from_crushed_tin");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.CRUSHED_LEAD }), Ic2Items.LEAD_INGOT, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.CRUSHED_LEAD))
			.m_126145_("lead_ingot")
			.m_176500_(consumer, "ic2:smelting/lead_ingot_from_crushed_lead");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.CRUSHED_URANIUM }), Ic2Items.URANIUM_INGOT, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.CRUSHED_URANIUM))
			.m_126145_("uranium_ingot")
			.m_176500_(consumer, "ic2:smelting/uranium_ingot_from_crushed_uranium");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.CRUSHED_SILVER }), Ic2Items.SILVER_INGOT, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.CRUSHED_SILVER))
			.m_126145_("silver_ingot")
			.m_176500_(consumer, "ic2:smelting/silver_ingot_from_crushed_silver");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.PURIFIED_IRON }), Items.f_42416_, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.PURIFIED_IRON))
			.m_126145_("iron_ingot")
			.m_176500_(consumer, "ic2:smelting/iron_ingot_from_purified_iron");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.PURIFIED_GOLD }), Items.f_42417_, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.PURIFIED_GOLD))
			.m_126145_("gold_ingot")
			.m_176500_(consumer, "ic2:smelting/gold_ingot_from_purified_gold");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.PURIFIED_COPPER }), Items.f_151052_, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.PURIFIED_COPPER))
			.m_126145_("copper_ingot")
			.m_176500_(consumer, "ic2:smelting/copper_ingot_from_purified_copper");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.PURIFIED_TIN }), Ic2Items.TIN_INGOT, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.PURIFIED_TIN))
			.m_126145_("tin_ingot")
			.m_176500_(consumer, "ic2:smelting/tin_ingot_from_purified_tin");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.PURIFIED_LEAD }), Ic2Items.LEAD_INGOT, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.PURIFIED_LEAD))
			.m_126145_("lead_ingot")
			.m_176500_(consumer, "ic2:smelting/lead_ingot_from_purified_lead");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.PURIFIED_URANIUM }), Ic2Items.URANIUM_INGOT, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.PURIFIED_URANIUM))
			.m_126145_("uranium_ingot")
			.m_176500_(consumer, "ic2:smelting/uranium_ingot_from_purified_uranium");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.PURIFIED_SILVER }), Ic2Items.SILVER_INGOT, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.PURIFIED_SILVER))
			.m_126145_("silver_ingot")
			.m_176500_(consumer, "ic2:smelting/silver_ingot_from_purified_silver");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.IRON_DUST }), Items.f_42416_, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.IRON_DUST))
			.m_126145_("iron_ingot")
			.m_176500_(consumer, "ic2:smelting/iron_ingot_from_iron_dust");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.GOLD_DUST }), Items.f_42417_, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.GOLD_DUST))
			.m_126145_("gold_ingot")
			.m_176500_(consumer, "ic2:smelting/gold_ingot_from_gold_dust");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.COPPER_DUST }), Items.f_151052_, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.COPPER_DUST))
			.m_126145_("copper_ingot")
			.m_176500_(consumer, "ic2:smelting/copper_ingot_from_copper_dust");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.TIN_DUST }), Ic2Items.TIN_INGOT, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.TIN_DUST))
			.m_126145_("tin_ingot")
			.m_176500_(consumer, "ic2:smelting/tin_ingot_from_tin_dust");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.LEAD_DUST }), Ic2Items.LEAD_INGOT, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.LEAD_DUST))
			.m_126145_("lead_ingot")
			.m_176500_(consumer, "ic2:smelting/lead_ingot_from_lead_dust");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.SILVER_DUST }), Ic2Items.SILVER_INGOT, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.SILVER_DUST))
			.m_126145_("copper_ingot")
			.m_176500_(consumer, "ic2:smelting/silver_ingot_from_silver_dust");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.BRONZE_DUST }), Ic2Items.BRONZE_INGOT, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.BRONZE_DUST))
			.m_126145_("bronze_ingot")
			.m_176500_(consumer, "ic2:smelting/bronze_ingot_from_bronze_dust");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.RUBBER_LOG }), Items.f_41840_, 0.1F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.RUBBER_LOG))
			.m_176500_(consumer, "ic2:smelting/jungle_log_from_rubber_log");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.RESIN }), Ic2Items.RUBBER, 0.3F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.RESIN))
			.m_176500_(consumer, "ic2:smelting/rubber");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.COAL_FUEL_DUST }), Ic2Items.COAL_DUST, 0.0F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.COAL_FUEL_DUST))
			.m_176500_(consumer, "ic2:smelting/coal_dust");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.COLD_COFFEE_MUG }), Ic2Items.DARK_COFFEE_MUG, 0.1F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.COLD_COFFEE_MUG))
			.m_176500_(consumer, "ic2:smelting/dark_coffee_mug");
		SimpleCookingRecipeBuilder.m_126272_(Ingredient.m_43929_(new ItemLike[] { Ic2Items.RAW_CRYSTAL_MEMORY }), Ic2Items.CRYSTAL_MEMORY, 0.1F, 200)
			.m_126132_("has_item", conditionsFromItem(Ic2Items.RAW_CRYSTAL_MEMORY))
			.m_176500_(consumer, "ic2:smelting/crystal_memory");
	}
}
