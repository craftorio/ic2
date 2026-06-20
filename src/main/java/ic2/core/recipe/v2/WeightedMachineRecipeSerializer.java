package ic2.core.recipe.v2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeWeighted;
import ic2.api.recipe.RecipeOutputWeighted;

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
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public class WeightedMachineRecipeSerializer implements RecipeSerializer<RecipeHolder<IRecipeInput, Collection<ItemStack>>>
{
	private final RecipeType<?> recipeType;
	@Nullable
	private final Function<JsonObject, CompoundTag> metaProcessor;

	public WeightedMachineRecipeSerializer(RecipeType<?> recipeType, @Nullable Function<JsonObject, CompoundTag> metaProcessor)
	{
		this.recipeType = recipeType;
		this.metaProcessor = metaProcessor;
	}

	public RecipeHolder<IRecipeInput, Collection<ItemStack>> fromJson(ResourceLocation id, JsonObject json)
	{
		IRecipeInput input = RecipeIo.parseInput(json.get("ingredient"));
		RecipeOutputWeighted output = new RecipeOutputWeighted();
		CompoundTag meta = this.metaProcessor != null ? this.metaProcessor.apply(json) : null;
		JsonElement resultJsonObj = json.get("result");
		boolean weighted = GsonHelper.getAsBoolean(json, "weighted", false);
		MachineRecipe<IRecipeInput, Collection<ItemStack>> machineRecipe;
		if (weighted)
		{
			RecipeIo.parseWeightedOutputs(resultJsonObj, "result", output);
			machineRecipe = new MachineRecipeWeighted<>(input, output, meta);
		} else
		{
			machineRecipe = new MachineRecipe<>(input, RecipeIo.parseOutputs(resultJsonObj, "result"));
		}

		return new RecipeHolder<>(machineRecipe, id, this, this.recipeType);
	}

	public RecipeHolder<IRecipeInput, Collection<ItemStack>> fromNetwork(ResourceLocation id, RegistryFriendlyByteBuf buf)
	{
		byte type = buf.readByte();
		MachineRecipe<IRecipeInput, Collection<ItemStack>> machineRecipe;
		if (type == 1)
		{
			machineRecipe = new MachineRecipeWeighted<>(RecipeIo.readInput(buf), RecipeIo.readWeightedOutput(buf, new RecipeOutputWeighted()), buf.readNbt());
		} else
		{
			machineRecipe = new MachineRecipe<>(RecipeIo.readInput(buf), RecipeIo.readOutput(buf), buf.readNbt());
		}

		return new RecipeHolder<>(machineRecipe, id, this, this.recipeType);
	}

	public void toNetwork(RegistryFriendlyByteBuf buf, RecipeHolder<IRecipeInput, Collection<ItemStack>> recipe)
	{
		MachineRecipe<IRecipeInput, Collection<ItemStack>> machineRecipe = recipe.recipe();
		if (machineRecipe instanceof MachineRecipeWeighted<?> machineRecipeWeighted)
		{
			buf.writeByte(1);
			RecipeIo.writeInput(buf, (IRecipeInput) machineRecipeWeighted.getInput());
			RecipeIo.writeWeightedOutput(buf, machineRecipeWeighted.getOutputWeighted());
		} else
		{
			buf.writeByte(0);
			RecipeIo.writeInput(buf, machineRecipe.getInput());
			RecipeIo.writeOutput(buf, machineRecipe.getOutput());
		}

		buf.writeNbt(machineRecipe.getMetaData());
	}

	@Override
	public MapCodec<RecipeHolder<IRecipeInput, Collection<ItemStack>>> codec()
	{
		return new MapCodec<>()
		{
			@Override
			public <T> Stream<T> keys(DynamicOps<T> ops)
			{
				return Stream.of(ops.createString("ingredient"), ops.createString("result"), ops.createString("weighted"), ops.createString("type"));
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
					return DataResult.error(() -> "Failed to decode IC2 weighted machine recipe: " + e.getMessage());
				}
			}

			@Override
			public <T> RecordBuilder<T> encode(RecipeHolder<IRecipeInput, Collection<ItemStack>> input, DynamicOps<T> ops, RecordBuilder<T> prefix)
			{
				return prefix.withErrorsFrom(DataResult.error(() -> "Encoding IC2 weighted machine recipes not supported"));
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
