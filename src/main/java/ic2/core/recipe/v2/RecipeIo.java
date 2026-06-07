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
import ic2.data.recipe.helper.WeightedMachineRecipeGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

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
			count = GsonHelper.m_13824_(json.getAsJsonObject(), "count", 1);
		}

		if (json.isJsonObject())
		{
			JsonObject object = json.getAsJsonObject();
			if (object.has("fluid"))
			{
				return new RecipeInputFluidContainer(asFluid(object.get("fluid"), "fluid"), GsonHelper.m_13927_(object, "amount"));
			}

			if (object.has("nbt"))
			{
				Item item = GsonHelper.m_13909_(object, "item");
				ItemStack stack = new ItemStack(item, count);
				stack.m_41751_(asNbt(object.get("data"), "data"));
				return new RecipeInputItemStack(stack);
			}

			if (object.has("any"))
			{
				JsonElement entry = object.get("any");
				if (!entry.isJsonArray())
				{
					throw new JsonSyntaxException("\"any\" IC2 ingredient entry must be an array, was " + GsonHelper.m_13883_(entry));
				}

				return parseMultiple(entry.getAsJsonArray(), count);
			}
		}

		return new RecipeInputIngredient(Ingredient.m_43917_(json), count);
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
		int amountMb = GsonHelper.m_13927_(json, "amount");
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
		Item item = GsonHelper.m_13909_(json, "item");
		int count = GsonHelper.m_13824_(json, "count", 1);
		CompoundTag nbt = getNbt(json, "nbt", null);
		ItemStack stack = new ItemStack(item, count);
		stack.m_41751_(nbt);
		return stack;
	}

	public static void parseWeightedOutput(JsonObject json, RecipeOutputWeighted randomOutput)
	{
		Item item = GsonHelper.m_13909_(json, "item");
		int count = GsonHelper.m_13824_(json, "count", 1);
		int weight = GsonHelper.m_13824_(json, "weight", 1);
		CompoundTag nbt = getNbt(json, "nbt", null);
		ItemStack stack = new ItemStack(item, count);
		stack.m_41751_(nbt);
		randomOutput.addOutput(stack, weight);
	}

	public static JsonObject resultToJson(ItemStack stack)
	{
		JsonObject json = new JsonObject();
		json.addProperty("item", Registry.f_122827_.getKey(stack.getItem()).toString());
		if (stack.m_41613_() != 1)
		{
			json.addProperty("count", stack.m_41613_());
		}

		return json;
	}

	public static JsonObject resultToJson(WeightedMachineRecipeGenerator.WeightedItemStack stack)
	{
		JsonObject json = new JsonObject();
		json.addProperty("item", Registry.f_122827_.getKey(stack.itemStack.getItem()).toString());
		if (stack.itemStack.m_41613_() != 1)
		{
			json.addProperty("count", stack.itemStack.m_41613_());
		}

		json.addProperty("weight", stack.weight);
		return json;
	}

	public static JsonObject fluidStackToJson(Ic2FluidStack stack)
	{
		JsonObject json = new JsonObject();
		json.addProperty("fluid", Registry.f_122822_.getKey(stack.getFluid()).toString());
		json.addProperty("amount", stack.getAmountMb());
		return json;
	}

	public static void writeInput(FriendlyByteBuf buf, IRecipeInput input)
	{
		if (input instanceof RecipeInputFluidContainer fluidContainer)
		{
			buf.writeByte(0);
			buf.m_130130_(Registry.f_122822_.m_7447_(fluidContainer.fluid));
			buf.m_130130_(fluidContainer.amount);
		} else if (input instanceof RecipeInputIngredient ingredient)
		{
			buf.writeByte(1);
			ingredient.getIngredient().m_43923_(buf);
			buf.m_130130_(ingredient.getAmount());
		} else if (input instanceof RecipeInputItemStack stack)
		{
			buf.writeByte(2);
			buf.m_130055_(stack.input);
		} else
		{
			if (!(input instanceof RecipeInputMultiple mult))
			{
				throw new IllegalArgumentException("Unkown RecipeInput type: " + input.getClass().getName());
			}

			buf.writeByte(3);
			buf.m_130130_(mult.inputs.length);

			for (IRecipeInput i : mult.inputs)
			{
				writeInput(buf, i);
			}

			buf.m_130130_(mult.getAmount());
		}
	}

	public static IRecipeInput readInput(FriendlyByteBuf buf)
	{
		return switch (buf.readByte())
		{
			case 0 ->
				new RecipeInputFluidContainer((Fluid) Registry.f_122822_.m_7942_(buf.m_130242_()), buf.m_130242_());
			case 1 -> new RecipeInputIngredient(Ingredient.m_43940_(buf), buf.m_130242_());
			case 2 -> new RecipeInputItemStack(buf.m_130267_());
			case 3 ->
			{
				IRecipeInput[] inputs = new IRecipeInput[buf.m_130242_()];

				for (int i = 0; i < inputs.length; i++)
				{
					inputs[i] = readInput(buf);
				}

				yield new RecipeInputMultiple(buf.m_130242_(), inputs);
			}
			default -> throw new IllegalArgumentException("Unkown RecipeInput type.");
		};
	}

	public static void writeOutput(FriendlyByteBuf buf, Collection<ItemStack> output)
	{
		buf.writeByte(0);
		buf.m_130130_(output.size());

		for (ItemStack stack : output)
		{
			buf.m_130055_(stack);
		}
	}

	public static void writeIntegerOutput(FriendlyByteBuf buf, int output)
	{
		buf.writeByte(0);
		buf.writeInt(output);
	}

	public static void writeWeightedOutput(FriendlyByteBuf buf, RecipeOutputWeighted outputs)
	{
		buf.writeByte(1);
		buf.m_130130_(outputs.getOutputs().size());
		outputs.forEach((stack, weight) ->
		{
			buf.m_130055_(stack);
			buf.writeInt(weight);
		});
	}

	public static Collection<ItemStack> readOutput(FriendlyByteBuf buf)
	{
		int amount = buf.m_130242_();
		List<ItemStack> stacks = new ArrayList<>(amount);

		for (int i = 0; i < amount; i++)
		{
			stacks.add(buf.m_130267_());
		}

		return stacks;
	}

	public static Integer readIntegerOutput(FriendlyByteBuf buf)
	{
		return buf.m_130242_();
	}

	public static RecipeOutputWeighted readWeightedOutput(FriendlyByteBuf buf, RecipeOutputWeighted outputs)
	{
		int amount = buf.m_130242_();

		for (int i = 0; i < amount; i++)
		{
			outputs.addOutput(buf.m_130267_(), buf.readInt());
		}

		return outputs;
	}

	public static void writeFluidStack(FriendlyByteBuf buf, Ic2FluidStack stack)
	{
		buf.m_130130_(Registry.f_122822_.m_7447_(stack.getFluid()));
		buf.m_130130_(stack.getAmountMb());
	}

	public static Ic2FluidStack readFluidStack(FriendlyByteBuf buf)
	{
		return FluidHandler.createFluidStackMb((Fluid) Registry.f_122822_.m_7942_(buf.m_130242_()), buf.m_130242_(), null);
	}

	private static Fluid asFluid(JsonElement element, String name)
	{
		if (element.isJsonPrimitive())
		{
			String string = element.getAsString();
			return (Fluid) Registry.f_122822_
				.m_6612_(ResourceLocation.fromNamespaceAndPath(string))
				.orElseThrow(() -> new JsonSyntaxException("Expected " + name + " to be an fluid, was unknown string '" + string + "'"));
		} else
		{
			throw new JsonSyntaxException("Expected " + name + " to be an fluid, was " + GsonHelper.m_13883_(element));
		}
	}

	private static CompoundTag asNbt(JsonElement element, String name)
	{
		Tag nbtElement = (Tag) JsonOps.INSTANCE.convertTo(NbtOps.f_128958_, element);
		if (nbtElement instanceof CompoundTag nbt)
		{
			return nbt;
		} else
		{
			throw new JsonSyntaxException("Expected " + name + " to be an NBT compound (an object), was " + GsonHelper.m_13883_(element));
		}
	}

	@Nullable
	@Contract("_,_,!null->!null;_,_,null->_")
	private static CompoundTag getNbt(JsonObject object, String key, @Nullable CompoundTag defaultNbt)
	{
		return object.has(key) ? asNbt(object.get(key), key) : defaultNbt;
	}
}
