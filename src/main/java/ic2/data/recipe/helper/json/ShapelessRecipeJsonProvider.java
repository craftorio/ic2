package ic2.data.recipe.helper.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ic2.core.recipe.input.RecipeInputBase;

import java.util.List;

import net.minecraft.world.item.crafting.RecipeSerializer;

public class ShapelessRecipeJsonProvider extends Ic2RecipeJsonProvider
{
	protected List<RecipeInputBase> ingredient;

	public ShapelessRecipeJsonProvider(RecipeSerializer<?> serializer, String fileName)
	{
		super(serializer, fileName);
	}

	@Override
	public void m_7917_(JsonObject json)
	{
		JsonArray ingredients = new JsonArray();

		for (RecipeInputBase ing : this.ingredient)
		{
			ingredients.add(ing.toJson());
		}

		json.add("ingredients", ingredients);
		if (!this.group.isEmpty())
		{
			json.addProperty("group", this.group);
		}

		super.m_7917_(json);
	}

	public ShapelessRecipeJsonProvider setIngredient(List<RecipeInputBase> ingredient)
	{
		this.ingredient = ingredient;
		return this;
	}
}
