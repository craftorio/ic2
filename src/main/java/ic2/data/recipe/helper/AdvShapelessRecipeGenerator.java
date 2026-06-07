package ic2.data.recipe.helper;

import ic2.data.recipe.helper.builder.AdvShapelessRecipeBuilder;

import java.util.function.Consumer;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class AdvShapelessRecipeGenerator
{
	private final Consumer<FinishedRecipe> exporter;

	public AdvShapelessRecipeGenerator(Consumer<FinishedRecipe> exporter)
	{
		this.exporter = exporter;
	}

	public AdvShapelessRecipeBuilder start(ItemLike result)
	{
		return this.start(result, 1);
	}

	public AdvShapelessRecipeBuilder start(ItemLike result, int amount)
	{
		return new AdvShapelessRecipeBuilder(new ItemStack(result, amount), this.exporter);
	}
}
