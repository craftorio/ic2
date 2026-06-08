package ic2.core.recipe.v2;

import com.google.gson.JsonObject;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;

import java.util.function.Function;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public class IntegerOutputRecipeSerializer implements RecipeSerializer<RecipeHolder<IRecipeInput, Integer>>
{
	private final RecipeType<?> recipeType;
	@Nullable
	private final Function<JsonObject, CompoundTag> metaProcessor;

	public IntegerOutputRecipeSerializer(RecipeType<?> recipeType, @Nullable Function<JsonObject, CompoundTag> metaProcessor)
	{
		this.recipeType = recipeType;
		this.metaProcessor = metaProcessor;
	}

	public RecipeHolder<IRecipeInput, Integer> fromJson(ResourceLocation id, JsonObject json)
	{
		IRecipeInput input = RecipeIo.parseInput(json.get("ingredient"));
		int output = json.get("result").getAsInt();
		CompoundTag meta = this.metaProcessor != null ? this.metaProcessor.apply(json) : null;
		return new RecipeHolder<>(new MachineRecipe<>(input, output, meta), id, this, this.recipeType);
	}

	public RecipeHolder<IRecipeInput, Integer> fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
	{
		byte type = buf.readByte();
		if (type != 0)
		{
			throw new Error("Reading recipe error! The type of recipe: \"" + id.getPath() + "\" is wrong!");
		} else
		{
			return new RecipeHolder<>(new MachineRecipe<>(RecipeIo.readInput(buf), RecipeIo.readIntegerOutput(buf), buf.readNbt()), id, this, this.recipeType);
		}
	}

	public void toNetwork(FriendlyByteBuf buf, RecipeHolder<IRecipeInput, Integer> recipe)
	{
		RecipeIo.writeInput(buf, recipe.recipe().getInput());
		RecipeIo.writeIntegerOutput(buf, recipe.recipe().getOutput());
		buf.writeNbt(recipe.recipe().getMetaData());
	}
}
