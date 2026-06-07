package ic2.data.recipe.helper.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ic2.core.recipe.input.RecipeInputBase;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.world.item.crafting.RecipeSerializer;

public class AdvShapedRecipeJsonProvider extends Ic2RecipeJsonProvider
{
	private String[] pattern;
	private Map<Character, RecipeInputBase> keyMap = new LinkedHashMap<>();
	private boolean consuming = false;
	private boolean hidden = false;

	public AdvShapedRecipeJsonProvider(RecipeSerializer<?> serializer, String fileName)
	{
		super(serializer, fileName);
	}

	@Override
	public void m_7917_(JsonObject json)
	{
		JsonArray patterns = new JsonArray();

		for (String line : this.pattern)
		{
			patterns.add(line);
		}

		json.add("pattern", patterns);
		JsonObject keys = new JsonObject();

		for (Entry<Character, RecipeInputBase> entry : this.keyMap.entrySet())
		{
			keys.add(entry.getKey().toString(), entry.getValue().toJson());
		}

		json.add("key", keys);
		if (this.consuming)
		{
			json.addProperty("consuming", true);
		}

		if (this.hidden)
		{
			json.addProperty("hidden", true);
		}

		if (!this.group.isEmpty())
		{
			json.addProperty("group", this.group);
		}

		super.m_7917_(json);
	}

	public AdvShapedRecipeJsonProvider setConsuming(boolean consuming)
	{
		this.consuming = consuming;
		return this;
	}

	public AdvShapedRecipeJsonProvider setHidden(boolean hidden)
	{
		this.hidden = hidden;
		return this;
	}

	public AdvShapedRecipeJsonProvider setPattern(String[] pattern)
	{
		this.pattern = pattern;
		return this;
	}

	public AdvShapedRecipeJsonProvider setKeyMap(Map<Character, RecipeInputBase> keyMap)
	{
		this.keyMap = keyMap;
		return this;
	}
}
