package ic2.data.recipe.helper.builder;

import ic2.core.recipe.input.RecipeInputBase;
import ic2.core.recipe.input.RecipeInputFluidContainer;
import ic2.core.recipe.input.RecipeInputIngredient;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.data.recipe.helper.Ic2RecipeProvider;
import ic2.data.recipe.helper.json.AdvShapedRecipeJsonProvider;
import ic2.data.recipe.helper.json.Ic2RecipeJsonProvider;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

public class AdvShapedRecipeBuilder extends Ic2RecipeBuilder<AdvShapedRecipeBuilder>
{
	private final String[] pattern;
	private final Map<Character, RecipeInputBase> key = new LinkedHashMap<>();
	private boolean consuming = false;
	private boolean hidden = false;

	public AdvShapedRecipeBuilder(ItemStack result, String[] pattern, Consumer<FinishedRecipe> exporter)
	{
		super(result, exporter);
		this.pattern = pattern;
	}

	public AdvShapedRecipeBuilder key(char key, ItemLike item)
	{
		this.key.put(key, new RecipeInputIngredient(Ingredient.m_43929_(new ItemLike[] { item }), 1));
		this.criterion("has_" + Registry.f_122827_.getKey(item.m_5456_()).m_135815_(), Ic2RecipeProvider.conditionsFromItem(item));
		return this;
	}

	public AdvShapedRecipeBuilder key(char key, TagKey<Item> tag)
	{
		this.key.put(key, new RecipeInputIngredient(Ingredient.m_204132_(tag), 1));
		this.criterion("has_" + tag.f_203868_().m_135815_(), Ic2RecipeProvider.conditionsFromTag(tag));
		return this;
	}

	public AdvShapedRecipeBuilder key(char key, Fluid fluid, int amount)
	{
		this.key.put(key, new RecipeInputFluidContainer(fluid, amount));
		return this;
	}

	public AdvShapedRecipeBuilder key(char key, RecipeInputBase input)
	{
		this.key.put(key, input);
		input.getInputs().forEach(itemStack ->
		{
			Item item = itemStack.getItem();
			this.criterion("has_" + Registry.f_122827_.getKey(item).m_135815_(), Ic2RecipeProvider.conditionsFromItem(item));
		});
		return this;
	}

	@Override
	protected Ic2RecipeJsonProvider build(String name)
	{
		return new AdvShapedRecipeJsonProvider(Ic2RecipeSerializers.SHAPED, name)
			.setPattern(this.pattern)
			.setKeyMap(this.key)
			.setHidden(this.hidden)
			.setConsuming(this.consuming)
			.setResult(this.result)
			.setGroup(this.group);
	}

	public AdvShapedRecipeBuilder consuming()
	{
		this.consuming = true;
		return this;
	}

	public AdvShapedRecipeBuilder hidden()
	{
		this.hidden = true;
		return this;
	}
}
