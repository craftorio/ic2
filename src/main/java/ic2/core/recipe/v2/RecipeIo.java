package ic2.core.recipe.v2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.RecipeOutputWeighted;
import ic2.core.fluid.FluidHandler;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.recipe.input.RecipeInputFluidContainer;
import ic2.core.recipe.input.RecipeInputIngredient;
import ic2.core.recipe.input.RecipeInputItemStack;
import ic2.core.recipe.input.RecipeInputMultiple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

public class RecipeIo
{
	public static IRecipeInput parseInput(JsonElement json)
	{
		if (json.isJsonArray())
		{
			return parseMultiple(json.getAsJsonArray(), 1);
		}

		int count = 1;
		if (json.isJsonObject())
		{
			count = GsonHelper.getAsInt(json.getAsJsonObject(), "count", 1);
		}

		if (json.isJsonObject())
		{
			JsonObject object = json.getAsJsonObject();
			if (object.has("fluid"))
			{
				return new RecipeInputFluidContainer(asFluid(object.get("fluid"), "fluid"), GsonHelper.getAsInt(object, "amount"));
			}

			if (object.has("nbt"))
			{
				Holder<Item> item = GsonHelper.getAsItem(object, "item");
				ItemStack stack = new ItemStack(item, count);
				stack.set(DataComponents.CUSTOM_DATA, CustomData.of(asNbt(object.get("nbt"), "nbt")));
				return new RecipeInputItemStack(stack);
			}

			if (object.has("any"))
			{
				JsonElement entry = object.get("any");
				if (!entry.isJsonArray())
				{
					throw new JsonSyntaxException("\"any\" IC2 ingredient entry must be an array, was " + GsonHelper.getType(entry));
				}

				return parseMultiple(entry.getAsJsonArray(), count);
			}
		}

		return new RecipeInputIngredient(Ingredient.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(), count);
	}

	private static RecipeInputMultiple parseMultiple(JsonArray array, int count)
	{
		IRecipeInput[] inputs = new IRecipeInput[array.size()];

		for (int i = 0; i < array.size(); i++)
		{
			inputs[i] = parseInput(array.get(i));
		}

		return new RecipeInputMultiple(count, inputs);
	}

	public static Ic2FluidStack parseFluidStack(JsonObject json)
	{
		Fluid fluid = asFluid(json.get("fluid"), "fluid");
		int amountMb = GsonHelper.getAsInt(json, "amount");
		return FluidHandler.createFluidStackMb(fluid, amountMb, null);
	}

	public static Collection<ItemStack> parseOutputs(JsonElement json, String name)
	{
		if (!json.isJsonArray())
		{
			if (json.isJsonObject())
			{
				return List.of(parseOutput(json.getAsJsonObject()));
			} else
			{
				throw new JsonSyntaxException("Expected " + name + " to be an array or object for output parsing.");
			}
		} else
		{
			JsonArray array = json.getAsJsonArray();
			ArrayList<ItemStack> output = new ArrayList<>(array.size());

			for (int i = 0; i < array.size(); i++)
			{
				output.addAll(parseOutputs(array.get(i), "array element"));
			}

			return output;
		}
	}

	public static void parseWeightedOutputs(JsonElement json, String name, RecipeOutputWeighted randomOutput)
	{
		if (json.isJsonArray())
		{
			JsonArray array = json.getAsJsonArray();

			for (int i = 0; i < array.size(); i++)
			{
				parseWeightedOutputs(array.get(i), "array element", randomOutput);
			}
		} else
		{
			if (!json.isJsonObject())
			{
				throw new JsonSyntaxException("Expected " + name + " to be an array or object for output parsing.");
			}

			parseWeightedOutput(json.getAsJsonObject(), randomOutput);
		}
	}

	public static ItemStack parseOutput(JsonObject json)
	{
		Holder<Item> item = GsonHelper.getAsItem(json, "item");
		int count = GsonHelper.getAsInt(json, "count", 1);
		CompoundTag nbt = getNbt(json, "nbt", null);
		ItemStack stack = new ItemStack(item, count);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
		return stack;
	}

	public static void parseWeightedOutput(JsonObject json, RecipeOutputWeighted randomOutput)
	{
		Holder<Item> item = GsonHelper.getAsItem(json, "item");
		int count = GsonHelper.getAsInt(json, "count", 1);
		int weight = GsonHelper.getAsInt(json, "weight", 1);
		CompoundTag nbt = getNbt(json, "nbt", null);
		ItemStack stack = new ItemStack(item, count);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
		randomOutput.addOutput(stack, weight);
	}

	public static JsonObject resultToJson(ItemStack stack)
	{
		JsonObject json = new JsonObject();
		json.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
		if (stack.getCount() != 1)
		{
			json.addProperty("count", stack.getCount());
		}

		return json;
	}

	public static JsonObject resultToJson(WeightedItemStack stack)
	{
		JsonObject json = new JsonObject();
		json.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.itemStack.getItem()).toString());
		if (stack.itemStack.getCount() != 1)
		{
			json.addProperty("count", stack.itemStack.getCount());
		}

		json.addProperty("weight", stack.weight);
		return json;
	}

	public static JsonObject fluidStackToJson(Ic2FluidStack stack)
	{
		JsonObject json = new JsonObject();
		json.addProperty("fluid", BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString());
		json.addProperty("amount", stack.getAmountMb());
		return json;
	}

	public static void writeInput(RegistryFriendlyByteBuf buf, IRecipeInput input)
	{
		switch (input)
		{
			case RecipeInputFluidContainer fluidContainer ->
			{
				buf.writeByte(0);
				buf.writeVarInt(BuiltInRegistries.FLUID.getId(fluidContainer.fluid));
				buf.writeVarInt(fluidContainer.amount);
			}
			case RecipeInputIngredient ingredient ->
			{
				buf.writeByte(1);
				Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient.getIngredient());
				buf.writeVarInt(ingredient.getAmount());
			}
			case RecipeInputItemStack stack ->
			{
				buf.writeByte(2);
				ItemStack.STREAM_CODEC.encode(buf, stack.input);
			}
			case null, default ->
			{
				if (!(input instanceof RecipeInputMultiple mult))
				{
					throw new IllegalArgumentException("Unkown RecipeInput type: " + input.getClass().getName());
				}

				buf.writeByte(3);
				buf.writeVarInt(mult.inputs.length);

				for (IRecipeInput i : mult.inputs)
				{
					writeInput(buf, i);
				}

				buf.writeVarInt(mult.getAmount());
			}
		}
	}

	public static IRecipeInput readInput(RegistryFriendlyByteBuf buf)
	{
		return switch (buf.readByte())
		{
			case 0 -> new RecipeInputFluidContainer(BuiltInRegistries.FLUID.byId(buf.readVarInt()), buf.readVarInt());
			case 1 -> new RecipeInputIngredient(Ingredient.CONTENTS_STREAM_CODEC.decode(buf), buf.readVarInt());
			case 2 -> new RecipeInputItemStack(ItemStack.STREAM_CODEC.decode(buf));
			case 3 ->
			{
				IRecipeInput[] inputs = new IRecipeInput[buf.readVarInt()];

				for (int i = 0; i < inputs.length; i++)
				{
					inputs[i] = readInput(buf);
				}

				yield new RecipeInputMultiple(buf.readVarInt(), inputs);
			}
			default -> throw new IllegalArgumentException("Unkown RecipeInput type.");
		};
	}

	public static void writeOutput(RegistryFriendlyByteBuf buf, Collection<ItemStack> output)
	{
		buf.writeVarInt(output.size());

		for (ItemStack stack : output)
		{
			ItemStack.STREAM_CODEC.encode(buf, stack);
		}
	}

	public static void writeIntegerOutput(RegistryFriendlyByteBuf buf, int output)
	{
		buf.writeInt(output);
	}

	public static void writeWeightedOutput(RegistryFriendlyByteBuf buf, RecipeOutputWeighted outputs)
	{
		buf.writeVarInt(outputs.getOutputs().size());
		outputs.forEach((stack, weight) ->
		{
			ItemStack.STREAM_CODEC.encode(buf, stack);
			buf.writeInt(weight);
		});
	}

	public static Collection<ItemStack> readOutput(RegistryFriendlyByteBuf buf)
	{
		int amount = buf.readVarInt();
		List<ItemStack> stacks = new ArrayList<>(amount);

		for (int i = 0; i < amount; i++)
		{
			stacks.add(ItemStack.STREAM_CODEC.decode(buf));
		}

		return stacks;
	}

	public static Integer readIntegerOutput(RegistryFriendlyByteBuf buf)
	{
		return buf.readInt();
	}

	public static RecipeOutputWeighted readWeightedOutput(RegistryFriendlyByteBuf buf, RecipeOutputWeighted outputs)
	{
		int amount = buf.readVarInt();

		for (int i = 0; i < amount; i++)
		{
			outputs.addOutput(ItemStack.STREAM_CODEC.decode(buf), buf.readInt());
		}

		return outputs;
	}

	public static void writeFluidStack(RegistryFriendlyByteBuf buf, Ic2FluidStack stack)
	{
		buf.writeVarInt(BuiltInRegistries.FLUID.getId(stack.getFluid()));
		buf.writeVarInt(stack.getAmountMb());
	}

	public static Ic2FluidStack readFluidStack(RegistryFriendlyByteBuf buf)
	{
		return FluidHandler.createFluidStackMb(BuiltInRegistries.FLUID.byId(buf.readVarInt()), buf.readVarInt(), null);
	}

	private static Fluid asFluid(JsonElement element, String name)
	{
		if (element.isJsonPrimitive())
		{
			String string = element.getAsString();
			return BuiltInRegistries.FLUID
				// TODO
				.getOptional(ResourceLocation.parse(string))
				.orElseThrow(() -> new JsonSyntaxException("Expected " + name + " to be an fluid, was unknown string '" + string + "'"));
		} else
		{
			throw new JsonSyntaxException("Expected " + name + " to be an fluid, was " + GsonHelper.getType(element));
		}
	}

	private static CompoundTag asNbt(JsonElement element, String name)
	{
		Tag nbtElement = JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, element);
		if (nbtElement instanceof CompoundTag nbt)
		{
			return nbt;
		} else
		{
			throw new JsonSyntaxException("Expected " + name + " to be an NBT compound (an object), was " + GsonHelper.getType(element));
		}
	}

	@Nullable
	@Contract("_,_,!null->!null;_,_,null->_")
	private static CompoundTag getNbt(JsonObject object, String key, @Nullable CompoundTag defaultNbt)
	{
		return object.has(key) ? asNbt(object.get(key), key) : defaultNbt;
	}
}
