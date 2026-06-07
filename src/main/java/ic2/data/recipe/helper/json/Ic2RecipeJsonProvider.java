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

	public void m_7917_(JsonObject json)
	{
		json.add("result", RecipeIo.resultToJson(this.result));
	}

	public final ResourceLocation m_6445_()
	{
		return IC2.getIdentifier(Registry.f_122865_.getKey(this.serializer).m_135815_() + "/" + this.fileName);
	}

	public final RecipeSerializer<?> m_6637_()
	{
		return this.serializer;
	}

	@Nullable
	public final JsonObject m_5860_()
	{
		return this.advancementBuilder != null ? this.advancementBuilder.m_138400_() : null;
	}

	@Nullable
	public final ResourceLocation m_6448_()
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
