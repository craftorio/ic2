package ic2.data.recipe.helper;

import ic2.data.recipe.helper.builder.ShapelessRecipeBuilder;

import java.util.function.Consumer;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ShapelessRecipeGenerator
{
	private final Consumer<FinishedRecipe> exporter;

	public ShapelessRecipeGenerator(Consumer<FinishedRecipe> exporter)
	{
		this.exporter = exporter;
	}

	public ShapelessRecipeBuilder<?> start(ItemLike result)
	{
		return this.start(result, 1);
	}

	public ShapelessRecipeBuilder<?> start(ItemLike result, int amount)
	{
		return new ShapelessRecipeBuilder(new ItemStack(result, amount), this.exporter);
	}
}
