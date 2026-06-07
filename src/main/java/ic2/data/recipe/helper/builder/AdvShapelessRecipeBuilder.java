package ic2.data.recipe.helper.builder;

import ic2.core.ref.Ic2RecipeSerializers;
import ic2.data.recipe.helper.json.AdvShapelessRecipeJsonProvider;
import ic2.data.recipe.helper.json.Ic2RecipeJsonProvider;

import java.util.function.Consumer;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

public class AdvShapelessRecipeBuilder extends ShapelessRecipeBuilder<AdvShapelessRecipeBuilder>
{
	private boolean consuming = false;
	private boolean hidden = false;

	public AdvShapelessRecipeBuilder(ItemStack result, Consumer<FinishedRecipe> exporter)
	{
		super(result, exporter);
	}

	public AdvShapelessRecipeBuilder consuming()
	{
		this.consuming = true;
		return this;
	}

	public AdvShapelessRecipeBuilder hidden()
	{
		this.hidden = true;
		return this;
	}

	@Override
	public Ic2RecipeJsonProvider build(String name)
	{
		return new AdvShapelessRecipeJsonProvider(Ic2RecipeSerializers.SHAPELESS, name)
			.setConsuming(this.consuming)
			.setHidden(this.hidden)
			.setIngredient(this.ingredient)
			.setResult(this.result)
			.setGroup(this.group);
	}
}
