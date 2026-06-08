package ic2.data.recipe.helper.builder;

import ic2.core.IC2;
import ic2.data.recipe.helper.json.Ic2RecipeJsonProvider;

import java.util.function.Consumer;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public abstract class Ic2RecipeBuilder<T extends Ic2RecipeBuilder<T>>
{
	protected Consumer<FinishedRecipe> exporter;
	protected final ItemStack result;
	protected String group = "";
	protected Builder advancementBuilder;

	public Ic2RecipeBuilder(ItemStack result, Consumer<FinishedRecipe> exporter)
	{
		this.result = result;
		this.exporter = exporter;
		this.advancementBuilder = Builder.advancement();
	}

	public T group(String group)
	{
		this.group = group;
		return (T) this;
	}

	public T criterion(String string, CriterionTriggerInstance arg)
	{
		this.advancementBuilder.addCriterion(string, arg);
		return (T) this;
	}

	protected abstract Ic2RecipeJsonProvider build(String var1);

	public void finish(String name)
	{
		Ic2RecipeJsonProvider builder = this.build(name);
		if (!this.advancementBuilder.getCriteria().isEmpty())
		{
			ResourceLocation recipeId = builder.getId();
			this.advancementBuilder
				.parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
				.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId))
				.rewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe(recipeId))
				.requirements(RequirementsStrategy.OR);
			builder.setAdvancementBuilder(this.advancementBuilder).setAdvancementId(IC2.getIdentifier("recipes/" + recipeId.getPath()));
		}

		this.exporter.accept(builder);
	}
}
