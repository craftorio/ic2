package ic2.data.recipe.helper.json;

import com.google.gson.JsonObject;
import ic2.core.IC2;
import ic2.core.recipe.v2.RecipeIo;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

public abstract class Ic2RecipeJsonProvider implements FinishedRecipe
{
	private final RecipeSerializer<?> serializer;
	private final String fileName;
	protected Builder advancementBuilder = null;
	protected ResourceLocation advancementId = null;
	protected ItemStack result;
	protected String group = "";

	protected Ic2RecipeJsonProvider(RecipeSerializer<?> serializer, String fileName)
	{
		this.serializer = serializer;
		this.fileName = fileName;
	}

	public void serializeRecipeData(JsonObject json)
	{
		json.add("result", RecipeIo.resultToJson(this.result));
	}

	public final ResourceLocation getId()
	{
		return IC2.getIdentifier(Registry.RECIPE_SERIALIZER.getKey(this.serializer).getPath() + "/" + this.fileName);
	}

	public final RecipeSerializer<?> getType()
	{
		return this.serializer;
	}

	@Nullable
	public final JsonObject serializeAdvancement()
	{
		return this.advancementBuilder != null ? this.advancementBuilder.serializeToJson() : null;
	}

	@Nullable
	public final ResourceLocation getAdvancementId()
	{
		return this.advancementId != null ? this.advancementId : null;
	}

	public Ic2RecipeJsonProvider setAdvancementBuilder(Builder advancementBuilder)
	{
		this.advancementBuilder = advancementBuilder;
		return this;
	}

	public Ic2RecipeJsonProvider setAdvancementId(ResourceLocation advancementId)
	{
		this.advancementId = advancementId;
		return this;
	}

	public Ic2RecipeJsonProvider setGroup(String group)
	{
		this.group = group;
		return this;
	}

	public Ic2RecipeJsonProvider setResult(ItemStack result)
	{
		this.result = result;
		return this;
	}
}
