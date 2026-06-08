package ic2.data.recipe.helper;

import com.google.gson.JsonObject;
import ic2.core.recipe.input.RecipeInputBase;
import ic2.core.recipe.v2.RecipeIo;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.data.recipe.helper.json.Ic2RecipeJsonProvider;

import java.util.function.Consumer;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CannerBottleRecipeGenerator
{
	private final Consumer<FinishedRecipe> exporter;

	public CannerBottleRecipeGenerator(Consumer<FinishedRecipe> exporter)
	{
		this.exporter = exporter;
	}

	public void add(RecipeInputBase container, RecipeInputBase fill, ItemStack output, String name)
	{
		this.exporter.accept(new Ic2RecipeJsonProvider(Ic2RecipeSerializers.CANNER_BOTTLE, name)
		{
			@Override
			public void serializeRecipeData(JsonObject json)
			{
				json.add("container_ingredient", container.toJson());
				json.add("fill_ingredient", fill.toJson());
				json.add("result", RecipeIo.resultToJson(output));
			}
		});
	}
}
