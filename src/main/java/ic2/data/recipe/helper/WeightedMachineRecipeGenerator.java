package ic2.data.recipe.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ic2.core.recipe.v2.RecipeIo;

import java.util.ArrayList;
import java.util.function.Consumer;

import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

public class WeightedMachineRecipeGenerator extends BasicMachineRecipeGenerator<WeightedMachineRecipeGenerator>
{
	public WeightedMachineRecipeGenerator(Consumer<FinishedRecipe> exporter, RecipeSerializer<?> recipeSerializer)
	{
		this(exporter, recipeSerializer, false);
	}

	public WeightedMachineRecipeGenerator(Consumer<FinishedRecipe> exporter, RecipeSerializer<?> recipeSerializer, boolean requiresMeta)
	{
		super(exporter, recipeSerializer, requiresMeta);
	}

	public void add(ItemLike inputItem, int inputCount, WeightedMachineRecipeGenerator.WeightedItemStack... weightedItemStacks)
	{
		this.add("%s_to_%s".formatted(path(inputItem), path(weightedItemStacks[0].itemStack.getItem())), json ->
		{
			JsonObject input = new JsonObject();
			input.addProperty("item", Registry.ITEM.getKey(inputItem.asItem()).toString());
			if (inputCount != 1)
			{
				input.addProperty("count", inputCount);
			}

			json.add("ingredient", input);
			json.addProperty("weighted", true);
			writeOutput(json, weightedItemStacks);
		});
	}

	public void add(TagKey<Item> inputTag, int inputCount, WeightedMachineRecipeGenerator.WeightedItemStack... stacks)
	{
		this.add("%s_to_%s".formatted(path(inputTag), path(stacks[0].itemStack.getItem())), json ->
		{
			JsonObject input = new JsonObject();
			input.addProperty("tag", inputTag.location().toString());
			if (inputCount != 1)
			{
				input.addProperty("count", inputCount);
			}

			json.add("ingredient", input);
			json.addProperty("weighted", true);
			writeOutput(json, stacks);
		});
	}

	protected static void writeOutput(JsonObject json, WeightedMachineRecipeGenerator.WeightedItemStack... stacks)
	{
		if (stacks.length == 0)
		{
			throw new IllegalArgumentException("Need at least one output stack.");
		}

		if (stacks.length == 1)
		{
			json.add("result", RecipeIo.resultToJson(stacks[0]));
		} else
		{
			JsonArray array = new JsonArray(stacks.length);

			for (WeightedMachineRecipeGenerator.WeightedItemStack stack : stacks)
			{
				array.add(RecipeIo.resultToJson(stack));
			}

			json.add("result", array);
		}
	}

	public static class WeightedItemStack
	{
		public ItemStack itemStack;
		public int weight;

		public WeightedItemStack(ItemStack itemStack, int weight)
		{
			this.itemStack = itemStack;
			this.weight = weight;
		}

		public static WeightedMachineRecipeGenerator.WeightedItemStack of(ItemStack itemStack, int weight)
		{
			return new WeightedMachineRecipeGenerator.WeightedItemStack(itemStack, weight);
		}

		public static WeightedMachineRecipeGenerator.WeightedItemStack[] of(ItemLike item, int[][] countAndWeights)
		{
			ArrayList<WeightedMachineRecipeGenerator.WeightedItemStack> retArr = new ArrayList<>();

			for (int[] countWeight : countAndWeights)
			{
				retArr.add(of(new ItemStack(item, countWeight[0]), countWeight[1]));
			}

			return retArr.toArray(new WeightedMachineRecipeGenerator.WeightedItemStack[0]);
		}
	}
}
