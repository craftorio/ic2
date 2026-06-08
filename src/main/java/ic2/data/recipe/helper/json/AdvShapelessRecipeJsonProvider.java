package ic2.data.recipe.helper.json;

import com.google.gson.JsonObject;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class AdvShapelessRecipeJsonProvider extends ShapelessRecipeJsonProvider
{
	private boolean consuming = false;
	private boolean hidden = false;

	public AdvShapelessRecipeJsonProvider(RecipeSerializer<?> serializer, String fileName)
	{
		super(serializer, fileName);
	}

	@Override
	public void serializeRecipeData(JsonObject json)
	{
		if (this.consuming)
		{
			json.addProperty("consuming", true);
		}

		if (this.hidden)
		{
			json.addProperty("hidden", true);
		}

		super.serializeRecipeData(json);
	}

	public AdvShapelessRecipeJsonProvider setConsuming(boolean consuming)
	{
		this.consuming = consuming;
		return this;
	}

	public AdvShapelessRecipeJsonProvider setHidden(boolean hidden)
	{
		this.hidden = hidden;
		return this;
	}
}
