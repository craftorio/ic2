package ic2.core.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import ic2.api.item.ElectricItem;
import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import ic2.core.item.tool.ItemToolCrafting;
import ic2.core.recipe.v2.RecipeIo;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.core.util.StackUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class AdvShapelessRecipe implements CraftingRecipe
{
	public final ItemStack output;
	public final IRecipeInput[] input;
	public final boolean hidden;
	public final boolean consuming;
	private final ResourceLocation id;

	public AdvShapelessRecipe(ResourceLocation id, IRecipeInput[] input, ItemStack output, boolean hidden, boolean consuming)
	{
		this.id = id;
		this.input = input;
		this.output = output;
		this.hidden = hidden;
		this.consuming = consuming;
	}

	public boolean matches(CraftingContainer inventorycrafting, Level world)
	{
		return this.assemble(inventorycrafting) != StackUtil.emptyStack;
	}

	public ItemStack assemble(CraftingContainer inventorycrafting)
	{
		int offerSize = inventorycrafting.getContainerSize();
		if (offerSize < this.input.length)
		{
			return StackUtil.emptyStack;
		}

		List<IRecipeInput> unmatched = new ArrayList<>(Arrays.asList(this.input));
		double outputCharge = 0.0;

		label36:
		for (int i = 0; i < offerSize; i++)
		{
			ItemStack offer = inventorycrafting.getItem(i);
			if (!StackUtil.isEmpty(offer))
			{
				for (int j = 0; j < unmatched.size(); j++)
				{
					if (unmatched.get(j).matches(offer))
					{
						outputCharge += ElectricItem.manager.getCharge(StackUtil.copyWithSize(offer, 1));
						unmatched.remove(j);
						continue label36;
					}
				}

				return StackUtil.emptyStack;
			}
		}

		if (!unmatched.isEmpty())
		{
			return StackUtil.emptyStack;
		}

		ItemStack ret = this.output.copy();
		ElectricItem.manager.charge(ret, outputCharge, Integer.MAX_VALUE, true, false);
		return ret;
	}

	public ItemStack getResultItem()
	{
		return this.output;
	}

	public boolean canShow()
	{
		return AdvRecipe.canShow(this.input, this.output, this.hidden);
	}

	public NonNullList<ItemStack> getRemainder(CraftingContainer inv)
	{
		if (this.consuming)
		{
			return NonNullList.withSize(inv.getContainerSize(), StackUtil.emptyStack);
		}

		NonNullList<ItemStack> defaultedList = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

		for (int i = 0; i < defaultedList.size(); i++)
		{
			ItemStack stack = inv.getItem(i);
			ItemStack remainder = IC2.envProxy.getRecipeRemainder(stack);
			if (stack.getItem() instanceof ItemToolCrafting)
			{
				remainder = stack.copy();
				remainder.setDamageValue(remainder.getDamageValue() + 1);
				if (remainder.getDamageValue() == remainder.getMaxDamage())
				{
					remainder = ItemStack.EMPTY;
				}
			}

			if (!remainder.isEmpty())
			{
				defaultedList.set(i, remainder);
			}
		}

		return defaultedList;
	}

	public boolean canCraftInDimensions(int x, int y)
	{
		return x * y >= this.input.length;
	}

	public NonNullList<Ingredient> getIngredients()
	{
		NonNullList<Ingredient> list = NonNullList.create();
		if (!this.hidden)
		{
			for (IRecipeInput input : this.input)
			{
				list.add(input.getIngredient());
			}
		}

		return list;
	}

	public boolean isSpecial()
	{
		return this.hidden;
	}

	public ResourceLocation getId()
	{
		return this.id;
	}

	public RecipeSerializer<?> getSerializer()
	{
		return Ic2RecipeSerializers.SHAPELESS;
	}

	public static class Serializer implements RecipeSerializer<AdvShapelessRecipe>
	{
		public AdvShapelessRecipe fromJson(ResourceLocation id, JsonObject json)
		{
			IRecipeInput[] ingredients = getIngredients(GsonHelper.getAsJsonArray(json, "ingredients"));
			if (ingredients.length == 0)
			{
				throw new JsonParseException("No ingredients for IC2 shapeless recipe");
			}

			if (ingredients.length > 9)
			{
				throw new JsonParseException("Too many ingredients for IC2 shapeless recipe");
			}

			ItemStack result = RecipeIo.parseOutput(GsonHelper.getAsJsonObject(json, "result"));
			boolean consuming = GsonHelper.getAsBoolean(json, "consuming", false);
			boolean hidden = GsonHelper.getAsBoolean(json, "hidden", false);
			return new AdvShapelessRecipe(id, ingredients, result, hidden, consuming);
		}

		private static IRecipeInput[] getIngredients(JsonArray json)
		{
			IRecipeInput[] inputs = new IRecipeInput[json.size()];

			for (int i = 0; i < json.size(); i++)
			{
				inputs[i] = RecipeIo.parseInput(json.get(i));
			}

			return inputs;
		}

		public AdvShapelessRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf)
		{
			IRecipeInput[] inputs = new IRecipeInput[buf.readVarInt()];

			for (int i = 0; i < inputs.length; i++)
			{
				inputs[i] = RecipeIo.readInput(buf);
			}

			return new AdvShapelessRecipe(id, inputs, buf.readItem(), buf.readBoolean(), buf.readBoolean());
		}

		public void toNetwork(FriendlyByteBuf buf, AdvShapelessRecipe recipe)
		{
			buf.writeVarInt(recipe.input.length);

			for (IRecipeInput input : recipe.input)
			{
				RecipeIo.writeInput(buf, input);
			}

			buf.writeItem(recipe.output);
			buf.writeBoolean(recipe.hidden);
			buf.writeBoolean(recipe.consuming);
		}
	}
}
