package ic2.data.recipe.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ic2.core.recipe.v2.RecipeIo;
import ic2.core.ref.Ic2RecipeSerializers;
import ic2.data.recipe.helper.json.Ic2RecipeJsonProvider;

import java.util.function.Consumer;

import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public class BasicMachineRecipeGenerator<T extends BasicMachineRecipeGenerator<T>>
{
	protected final Consumer<FinishedRecipe> exporter;
	protected final RecipeSerializer<?> recipeSerializer;
	protected final boolean requiresMeta;
	@Nullable
	protected Consumer<JsonObject> currentMeta = null;
	protected Builder advancementBuilder = null;
	protected ResourceLocation advancementId = null;

	public BasicMachineRecipeGenerator(Consumer<FinishedRecipe> exporter, RecipeSerializer<?> recipeSerializer)
	{
		this(exporter, recipeSerializer, false);
	}

	public BasicMachineRecipeGenerator(Consumer<FinishedRecipe> exporter, RecipeSerializer<?> recipeSerializer, boolean requiresMeta)
	{
		this.exporter = exporter;
		this.recipeSerializer = recipeSerializer;
		this.requiresMeta = requiresMeta;
	}

	public void add(ItemLike input, int inputCount, ItemLike output)
	{
		this.add(input, inputCount, output, 1);
	}

	public void add(ItemLike inputItem, int inputCount, ItemLike outputItem, int outputCount)
	{
		this.add(inputItem, inputCount, new ItemStack(outputItem, outputCount));
	}

	public void add(ItemLike inputItem, int inputCount, ItemStack... stacks)
	{
		this.add("%s_to_%s".formatted(path(inputItem), path(stacks[0].getItem())), json ->
		{
			JsonObject input = new JsonObject();
			input.addProperty("item", Registry.ITEM.getKey(inputItem.asItem()).toString());
			if (inputCount != 1)
			{
				input.addProperty("count", inputCount);
			}

			json.add("ingredient", input);
			writeOutput(json, stacks);
		});
	}

	public void add(TagKey<Item> inputTag, int inputCount, ItemLike outputItem)
	{
		this.add(inputTag, inputCount, outputItem, 1);
	}

	public void add(TagKey<Item> inputTag, int inputCount, ItemLike outputItem, int outputCount)
	{
		this.add(inputTag, inputCount, new ItemStack(outputItem, outputCount));
	}

	public void add(TagKey<Item> inputTag, int inputCount, ItemStack... stacks)
	{
		this.add("%s_to_%s".formatted(path(inputTag), path(stacks[0].getItem())), json ->
		{
			JsonObject input = new JsonObject();
			input.addProperty("tag", inputTag.location().toString());
			if (inputCount != 1)
			{
				input.addProperty("count", inputCount);
			}

			json.add("ingredient", input);
			writeOutput(json, stacks);
		});
	}

	public void add(Fluid fluid, int fluidAmount, ItemLike outputItem)
	{
		this.add("%s_to_%s".formatted(path(fluid), path(outputItem)), json ->
		{
			JsonObject input = new JsonObject();
			input.addProperty("fluid", Registry.FLUID.getKey(fluid).toString());
			input.addProperty("amount", fluidAmount);
			json.add("ingredient", input);
			writeOutput(json, new ItemStack(outputItem, 1));
		});
	}

	protected static String path(ItemLike item)
	{
		return Registry.ITEM.getKey(item.asItem()).getPath();
	}

	protected static String path(TagKey<Item> tag)
	{
		return tag.location().getPath().replace('/', '_');
	}

	protected static String path(Fluid fluid)
	{
		return Registry.FLUID.getKey(fluid).getPath();
	}

	protected static void writeOutput(JsonObject json, ItemStack... stacks)
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

			for (ItemStack stack : stacks)
			{
				array.add(RecipeIo.resultToJson(stack));
			}

			json.add("result", array);
		}
	}

	protected void add(String id, Consumer<JsonObject> serializer)
	{
		this.exporter.accept((new Ic2RecipeJsonProvider(this.recipeSerializer, id)
		{
			@Override
			public void serializeRecipeData(JsonObject json)
			{
				serializer.accept(json);
				if (BasicMachineRecipeGenerator.this.requiresMeta == (BasicMachineRecipeGenerator.this.currentMeta == null))
				{
					throw new IllegalStateException("Missing meta");
				}

				if (BasicMachineRecipeGenerator.this.requiresMeta)
				{
					BasicMachineRecipeGenerator.this.currentMeta.accept(json);
					BasicMachineRecipeGenerator.this.currentMeta = null;
				}
			}
		}).setAdvancementId(this.advancementId).setAdvancementBuilder(this.advancementBuilder));
	}

	public T cutterLevel(int cutterLevel)
	{
		if (this.recipeSerializer != Ic2RecipeSerializers.BLOCK_CUTTER)
		{
			throw new IllegalArgumentException();
		} else
		{
			return this.setMeta(json -> json.addProperty("hardness", cutterLevel));
		}
	}

	public T minHeat(int minHeat)
	{
		if (this.recipeSerializer != Ic2RecipeSerializers.CENTRIFUGE)
		{
			throw new IllegalArgumentException();
		} else
		{
			return this.setMeta(json -> json.addProperty("minHeat", minHeat));
		}
	}

	public T fluidDuration(int fluid, int duration)
	{
		if (this.recipeSerializer != Ic2RecipeSerializers.BLAST_FURNACE)
		{
			throw new IllegalArgumentException();
		} else
		{
			return this.setMeta(json ->
			{
				json.addProperty("fluid", fluid);
				json.addProperty("duration", duration);
			});
		}
	}

	public T amount(int amount)
	{
		if (this.recipeSerializer != Ic2RecipeSerializers.ORE_WASHER)
		{
			throw new IllegalArgumentException();
		} else
		{
			return this.setMeta(json -> json.addProperty("amount", amount));
		}
	}

	protected T setMeta(Consumer<JsonObject> meta)
	{
		if (this.currentMeta != null)
		{
			throw new IllegalStateException("Meta already set");
		}

		this.currentMeta = meta;
		return (T) this;
	}

	public T setAdvancementBuilder(Builder advancementBuilder)
	{
		this.advancementBuilder = advancementBuilder;
		return (T) this;
	}

	public T setAdvancementId(ResourceLocation advancementId)
	{
		this.advancementId = advancementId;
		return (T) this;
	}
}
