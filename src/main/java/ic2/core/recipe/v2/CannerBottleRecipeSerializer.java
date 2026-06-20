package ic2.core.recipe.v2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.core.ref.Ic2RecipeTypes;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CannerBottleRecipeSerializer implements RecipeSerializer<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>>
{
	public RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> fromJson(ResourceLocation id, JsonObject json)
	{
		IRecipeInput container = RecipeIo.parseInput(json.get("container_ingredient"));
		IRecipeInput fill = RecipeIo.parseInput(json.get("fill_ingredient"));
		ItemStack output = RecipeIo.parseOutput(GsonHelper.getAsJsonObject(json, "result"));
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerBottleRecipeManager.Input(container, fill), output), id, this, Ic2RecipeTypes.CANNER_BOTTLE);
	}

	public RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> fromNetwork(ResourceLocation id, RegistryFriendlyByteBuf buf)
	{
		IRecipeInput container = RecipeIo.readInput(buf);
		IRecipeInput fill = RecipeIo.readInput(buf);
		ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerBottleRecipeManager.Input(container, fill), output), id, this, Ic2RecipeTypes.CANNER_BOTTLE);
	}

	public void toNetwork(RegistryFriendlyByteBuf buf, RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> recipe)
	{
		RecipeIo.writeInput(buf, recipe.recipe().getInput().container);
		RecipeIo.writeInput(buf, recipe.recipe().getInput().fill);
		ItemStack.STREAM_CODEC.encode(buf, recipe.recipe().getOutput());
	}

	@Override
	public MapCodec<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> codec()
	{
		return new MapCodec<>()
		{
			@Override
			public <T> Stream<T> keys(DynamicOps<T> ops)
			{
				return Stream.of(ops.createString("container_ingredient"), ops.createString("fill_ingredient"), ops.createString("result"), ops.createString("type"));
			}

			@Override
			public <T> DataResult<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> decode(DynamicOps<T> ops, MapLike<T> input)
			{
				JsonObject json = new JsonObject();
				input.entries().forEach(pair ->
				{
					String key = ops.getStringValue(pair.getFirst()).getOrThrow();
					JsonElement value = ops.convertTo(JsonOps.INSTANCE, pair.getSecond());
					json.add(key, value);
				});
				try
				{
					return DataResult.success(fromJson(ResourceLocation.fromNamespaceAndPath("ic2", "codec"), json));
				} catch (Exception e)
				{
					return DataResult.error(() -> "Failed to decode IC2 canner bottle recipe: " + e.getMessage());
				}
			}

			@Override
			public <T> RecordBuilder<T> encode(RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> input, DynamicOps<T> ops, RecordBuilder<T> prefix)
			{
				return prefix.withErrorsFrom(DataResult.error(() -> "Encoding IC2 canner bottle recipes not supported"));
			}
		};
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> streamCodec()
	{
		return StreamCodec.of(
				this::toNetwork,
				buf -> this.fromNetwork(ResourceLocation.fromNamespaceAndPath("ic2", "stream"), buf)
		);
	}
}
