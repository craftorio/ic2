package ic2.core.recipe.v2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public class BasicMachineRecipeSerializer implements RecipeSerializer<RecipeHolder<IRecipeInput, Collection<ItemStack>>>
{
	private final RecipeType<?> recipeType;
	@Nullable
	private final Function<JsonObject, CompoundTag> metaProcessor;

	public BasicMachineRecipeSerializer(RecipeType<?> recipeType, @Nullable Function<JsonObject, CompoundTag> metaProcessor)
	{
		this.recipeType = recipeType;
		this.metaProcessor = metaProcessor;
	}

	public RecipeHolder<IRecipeInput, Collection<ItemStack>> fromJson(ResourceLocation id, JsonObject json)
	{
		IRecipeInput input = RecipeIo.parseInput(json.get("ingredient"));
		Collection<ItemStack> output = RecipeIo.parseOutputs(json.get("result"), "result");
		CompoundTag meta = this.metaProcessor != null ? this.metaProcessor.apply(json) : null;
		return new RecipeHolder<>(new MachineRecipe<>(input, output, meta), id, this, this.recipeType);
	}

	public RecipeHolder<IRecipeInput, Collection<ItemStack>> fromNetwork(ResourceLocation id, RegistryFriendlyByteBuf buf)
	{
		byte type = buf.readByte();
		if (type != 0)
		{
			throw new Error("Reading recipe error! The type of recipe: \"" + id.getPath() + "\" is wrong!");
		} else
		{
			return new RecipeHolder<>(new MachineRecipe<>(RecipeIo.readInput(buf), RecipeIo.readOutput(buf), buf.readNbt()), id, this, this.recipeType);
		}
	}

	public void toNetwork(RegistryFriendlyByteBuf buf, RecipeHolder<IRecipeInput, Collection<ItemStack>> recipe)
	{
		buf.writeByte(0);
		RecipeIo.writeInput(buf, recipe.recipe().getInput());
		RecipeIo.writeOutput(buf, recipe.recipe().getOutput());
		buf.writeNbt(recipe.recipe().getMetaData());
	}

	@Override
	public MapCodec<RecipeHolder<IRecipeInput, Collection<ItemStack>>> codec()
	{
		return new MapCodec<>()
		{
			@Override
			public <T> Stream<T> keys(DynamicOps<T> ops)
			{
				return Stream.of(ops.createString("ingredient"), ops.createString("result"), ops.createString("type"));
			}

			@Override
			public <T> DataResult<RecipeHolder<IRecipeInput, Collection<ItemStack>>> decode(DynamicOps<T> ops, MapLike<T> input)
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
					return DataResult.error(() -> "Failed to decode IC2 basic machine recipe: " + e.getMessage());
				}
			}

			@Override
			public <T> RecordBuilder<T> encode(RecipeHolder<IRecipeInput, Collection<ItemStack>> input, DynamicOps<T> ops, RecordBuilder<T> prefix)
			{
				return prefix.withErrorsFrom(DataResult.error(() -> "Encoding IC2 basic machine recipes not supported"));
			}
		};
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<IRecipeInput, Collection<ItemStack>>> streamCodec()
	{
		return StreamCodec.of(
				this::toNetwork,
				buf -> this.fromNetwork(ResourceLocation.fromNamespaceAndPath("ic2", "stream"), buf)
		);
	}
}
