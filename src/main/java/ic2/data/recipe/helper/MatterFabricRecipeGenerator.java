package ic2.data.recipe.helper;

import com.google.gson.JsonObject;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.core.util.IdentifierUtil;
import ic2.data.recipe.helper.json.Ic2RecipeJsonProvider;

import java.util.function.Consumer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class MatterFabricRecipeGenerator
{
	private final Consumer<FinishedRecipe> exporter;

	public MatterFabricRecipeGenerator(Consumer<FinishedRecipe> exporter)
	{
		this.exporter = exporter;
	}

	public void add(ItemLike inputItem, int inputCount, int output)
	{
		this.exporter.accept(new Ic2RecipeJsonProvider(Ic2RecipeSerializers.MATTER_FABRICATOR, "%s_to_%s".formatted(IdentifierUtil.getPath(inputItem), output))
		{
			@Override
			public void serializeRecipeData(JsonObject json)
			{
				JsonObject input = new JsonObject();
				input.addProperty("item", BuiltInRegistries.ITEM.getKey(inputItem.asItem()).toString());
				if (inputCount != 1)
				{
					input.addProperty("count", inputCount);
				}

				json.addProperty("result", output);
				json.add("ingredient", input);
			}
		});
	}
}
