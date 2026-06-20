package ic2.core.recipe.v2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.ref.Ic2RecipeTypes;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CannerEnrichRecipeSerializer implements RecipeSerializer<RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack>>
{
	public RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> fromJson(ResourceLocation id, JsonObject json)
	{
		Ic2FluidStack input = RecipeIo.parseFluidStack(GsonHelper.getAsJsonObject(json, "input_ingredient"));
		IRecipeInput additive = RecipeIo.parseInput(json.get("additive_ingredient"));
		Ic2FluidStack result = RecipeIo.parseFluidStack(GsonHelper.getAsJsonObject(json, "result"));
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerEnrichRecipeManager.Input(input, additive), result), id, this, Ic2RecipeTypes.CANNER_ENRICH);
	}

	public RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> fromNetwork(ResourceLocation id, RegistryFriendlyByteBuf buf)
	{
		Ic2FluidStack input = RecipeIo.readFluidStack(buf);
		IRecipeInput additive = RecipeIo.readInput(buf);
		Ic2FluidStack result = RecipeIo.readFluidStack(buf);
		return new RecipeHolder<>(new MachineRecipe<>(new ICannerEnrichRecipeManager.Input(input, additive), result), id, this, Ic2RecipeTypes.CANNER_ENRICH);
	}

	public void toNetwork(RegistryFriendlyByteBuf buf, RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> recipe)
	{
		RecipeIo.writeFluidStack(buf, recipe.recipe().getInput().fluid);
		RecipeIo.writeInput(buf, recipe.recipe().getInput().additive);
		RecipeIo.writeFluidStack(buf, recipe.recipe().getOutput());
	}

	@Override
	public MapCodec<RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack>> codec()
	{
		return new MapCodec<>()
		{
			@Override
			public <T> Stream<T> keys(DynamicOps<T> ops)
			{
				return Stream.of(ops.createString("input_ingredient"), ops.createString("additive_ingredient"), ops.createString("result"), ops.createString("type"));
			}

			@Override
			public <T> DataResult<RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack>> decode(DynamicOps<T> ops, MapLike<T> input)
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
					return DataResult.error(() -> "Failed to decode IC2 canner enrich recipe: " + e.getMessage());
				}
			}

			@Override
			public <T> RecordBuilder<T> encode(RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> input, DynamicOps<T> ops, RecordBuilder<T> prefix)
			{
				return prefix.withErrorsFrom(DataResult.error(() -> "Encoding IC2 canner enrich recipes not supported"));
			}
		};
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack>> streamCodec()
	{
		return StreamCodec.of(
				this::toNetwork,
				buf -> this.fromNetwork(ResourceLocation.fromNamespaceAndPath("ic2", "stream"), buf)
		);
	}
}
