package ic2.data.recipe.helper;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import ic2.core.fluid.FluidHandler;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.recipe.input.RecipeInputIngredient;
import ic2.data.Ic2DataGenerators;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.advancements.critereon.ItemPredicate.Builder;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.slf4j.Logger;

public abstract class Ic2RecipeProvider implements DataProvider
{
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final PackOutput packOutput;

	public Ic2RecipeProvider(PackOutput packOutput)
	{
		this.packOutput = packOutput;
	}

	protected abstract void generate(Consumer<FinishedRecipe> var1);

	protected static RecipeInputIngredient tagInput(TagKey<Item> tag)
	{
		return tagInput(tag, 1);
	}

	protected static RecipeInputIngredient tagInput(TagKey<Item> tag, int amount)
	{
		return new RecipeInputIngredient(Ingredient.of(tag), amount);
	}

	protected static RecipeInputIngredient itemInput(ItemLike... item)
	{
		return new RecipeInputIngredient(Ingredient.of(item), 1);
	}

	protected static RecipeInputIngredient itemInput(ItemLike item, int amount)
	{
		return new RecipeInputIngredient(Ingredient.of(new ItemLike[] { item }), amount);
	}

	protected static Ic2FluidStack bucket(Fluid fluid)
	{
		return FluidHandler.createFluidStackMb(fluid, 1000, null);
	}

	public String getName()
	{
		return this.getClass().getSimpleName();
	}

	public CompletableFuture<?> run(CachedOutput writer)
	{
		Set<ResourceLocation> set = Sets.newHashSet();
		this.generate(provider ->
		{
			if (!set.add(provider.getId()))
			{
				throw new IllegalStateException("Duplicate recipe " + provider.getId());
			}

			Path recipePath = this.packOutput.getOutputFolder()
				.resolve("data/" + provider.getId().getNamespace() + "/recipes/" + provider.getId().getPath() + ".json");
			saveRecipe(writer, provider.serializeRecipe(), recipePath);
			JsonObject jsonObject = provider.serializeAdvancement();
			if (jsonObject != null)
			{
				Path advancementPath = this.packOutput.getOutputFolder()
					.resolve("data/" + provider.getAdvancementId().getNamespace() + "/advancements/" + provider.getAdvancementId().getPath() + ".json");
				saveRecipeAdvancement(writer, jsonObject, advancementPath);
			}
		});
		return CompletableFuture.allOf();
	}

	private static void saveRecipe(CachedOutput cache, JsonObject json, Path path)
	{
		try
		{
			Ic2DataGenerators.saveJsonPreserveOrder(GSON, cache, json, path);
		} catch (Exception var4)
		{
			LOGGER.error("Couldn't save recipe {}", path, var4);
		}
	}

	private static void saveRecipeAdvancement(CachedOutput cache, JsonObject json, Path path)
	{
		try
		{
			DataProvider.saveStable(cache, json, path);
		} catch (Exception var4)
		{
			LOGGER.error("Couldn't save recipe advancement {}", path, var4);
		}
	}

	public static TriggerInstance conditionsFromItem(Ints count, ItemLike item)
	{
		return conditionsFromItemPredicates(Builder.item().of(new ItemLike[] { item }).withCount(count).build());
	}

	public static TriggerInstance conditionsFromItem(ItemLike item)
	{
		return conditionsFromItemPredicates(Builder.item().of(new ItemLike[] { item }).build());
	}

	public static TriggerInstance conditionsFromTag(TagKey<Item> tag)
	{
		return conditionsFromItemPredicates(Builder.item().of(tag).build());
	}

	public static TriggerInstance conditionsFromItemPredicates(ItemPredicate... predicates)
	{
		return new TriggerInstance(ContextAwarePredicate.ANY, Ints.ANY, Ints.ANY, Ints.ANY, predicates);
	}
}
