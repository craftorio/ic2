package ic2.data.recipe.helper.builder;

import ic2.core.recipe.input.RecipeInputBase;
import ic2.core.recipe.input.RecipeInputFluidContainer;
import ic2.core.recipe.input.RecipeInputIngredient;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.data.recipe.helper.json.AdvShapelessRecipeJsonProvider;
import ic2.data.recipe.helper.json.Ic2RecipeJsonProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

public class ShapelessRecipeBuilder<T extends ShapelessRecipeBuilder<T>> extends Ic2RecipeBuilder<T>
{
	protected final List<RecipeInputBase> ingredient = new ArrayList<>();

	public ShapelessRecipeBuilder(ItemStack result, Consumer<FinishedRecipe> exporter)
	{
		super(result, exporter);
	}

	public T add(ItemLike item)
	{
		this.ingredient.add(new RecipeInputIngredient(Ingredient.of(new ItemLike[] { item }), 1));
		return (T) this;
	}

	public T add(Ingredient ingredient)
	{
		this.ingredient.add(new RecipeInputIngredient(ingredient, 1));
		return (T) this;
	}

	public T add(TagKey<Item> tag)
	{
		this.ingredient.add(new RecipeInputIngredient(Ingredient.of(tag), 1));
		return (T) this;
	}

	public T add(Fluid fluid, int amount)
	{
		this.ingredient.add(new RecipeInputFluidContainer(fluid, amount));
		return (T) this;
	}

	public T add(RecipeInputBase input)
	{
		this.ingredient.add(input);
		return (T) this;
	}

	@Override
	public Ic2RecipeJsonProvider build(String name)
	{
		return new AdvShapelessRecipeJsonProvider(Ic2RecipeSerializers.SHAPELESS, name)
			.setIngredient(this.ingredient)
			.setResult(this.result)
			.setGroup(this.group);
	}
}
