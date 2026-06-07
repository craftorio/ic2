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
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.EntityPredicate.Composite;
import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.advancements.critereon.ItemPredicate.Builder;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataGenerator.PathProvider;
import net.minecraft.data.DataGenerator.Target;
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
	private final DataGenerator generator;
	private final PathProvider recipesPathResolver;
	private final PathProvider advancementsPathResolver;

	public Ic2RecipeProvider(DataGenerator root)
	{
		this.generator = root;
		this.recipesPathResolver = root.m_236036_(Target.DATA_PACK, "recipes");
		this.advancementsPathResolver = root.m_236036_(Target.DATA_PACK, "advancements");
	}

	protected abstract void generate(Consumer<FinishedRecipe> var1);

	protected static RecipeInputIngredient tagInput(TagKey<Item> tag)
	{
		return tagInput(tag, 1);
	}

	protected static RecipeInputIngredient tagInput(TagKey<Item> tag, int amount)
	{
		return new RecipeInputIngredient(Ingredient.m_204132_(tag), amount);
	}

	protected static RecipeInputIngredient itemInput(ItemLike... item)
	{
		return new RecipeInputIngredient(Ingredient.m_43929_(item), 1);
	}

	protected static RecipeInputIngredient itemInput(ItemLike item, int amount)
	{
		return new RecipeInputIngredient(Ingredient.m_43929_(new ItemLike[] { item }), amount);
	}

	protected static Ic2FluidStack bucket(Fluid fluid)
	{
		return FluidHandler.createFluidStackMb(fluid, 1000, null);
	}

	public String m_6055_()
	{
		return this.getClass().getSimpleName();
	}

	public void m_213708_(CachedOutput writer)
	{
		Set<ResourceLocation> set = Sets.newHashSet();
		this.generate(provider ->
		{
			if (!set.add(provider.m_6445_()))
			{
				throw new IllegalStateException("Duplicate recipe " + provider.m_6445_());
			}

			saveRecipe(writer, provider.m_125966_(), this.recipesPathResolver.m_236048_(provider.m_6445_()));
			JsonObject jsonObject = provider.m_5860_();
			if (jsonObject != null)
			{
				saveRecipeAdvancement(writer, jsonObject, this.advancementsPathResolver.m_236048_(provider.m_6448_()));
			}
		});
	}

	private static void saveRecipe(CachedOutput cache, JsonObject json, Path path)
	{
		try
		{
			Ic2DataGenerators.saveJsonPreserveOrder(GSON, cache, json, path);
		} catch (IOException var4)
		{
			LOGGER.error("Couldn't save recipe {}", path, var4);
		}
	}

	private static void saveRecipeAdvancement(CachedOutput cache, JsonObject json, Path path)
	{
		try
		{
			DataProvider.m_236072_(cache, json, path);
		} catch (IOException var4)
		{
			LOGGER.error("Couldn't save recipe advancement {}", path, var4);
		}
	}

	public static TriggerInstance conditionsFromItem(Ints count, ItemLike item)
	{
		return conditionsFromItemPredicates(Builder.m_45068_().m_151445_(new ItemLike[] { item }).m_151443_(count).m_45077_());
	}

	public static TriggerInstance conditionsFromItem(ItemLike item)
	{
		return conditionsFromItemPredicates(Builder.m_45068_().m_151445_(new ItemLike[] { item }).m_45077_());
	}

	public static TriggerInstance conditionsFromTag(TagKey<Item> tag)
	{
		return conditionsFromItemPredicates(Builder.m_45068_().m_204145_(tag).m_45077_());
	}

	public static TriggerInstance conditionsFromItemPredicates(ItemPredicate... predicates)
	{
		return new TriggerInstance(Composite.f_36667_, Ints.f_55364_, Ints.f_55364_, Ints.f_55364_, predicates);
	}
}
