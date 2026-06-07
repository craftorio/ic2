package ic2.data.recipe.helper;

import ic2.data.recipe.helper.builder.AdvShapedRecipeBuilder;

import java.util.function.Consumer;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class AdvShapedRecipeGenerator
{
	private final Consumer<FinishedRecipe> exporter;

	public AdvShapedRecipeGenerator(Consumer<FinishedRecipe> exporter)
	{
		this.exporter = exporter;
	}

	public AdvShapedRecipeBuilder start(ItemLike result, String... pattern)
	{
		return this.start(result, 1, pattern);
	}

	public AdvShapedRecipeBuilder start(ItemLike result, int amount, String... pattern)
	{
		return new AdvShapedRecipeBuilder(new ItemStack(result, amount), pattern, this.exporter);
	}
}
