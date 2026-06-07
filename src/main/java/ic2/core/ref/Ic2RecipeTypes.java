package ic2.core.ref;

import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.recipe.v2.RecipeHolder;

import java.util.Collection;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public class Ic2RecipeTypes
{
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> MACERATOR = register("macerator");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> EXTRACTOR = register("extractor");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> COMPRESSOR = register("compressor");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> CENTRIFUGE = register("centrifuge");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> BLOCK_CUTTER = register("block_cutter");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> BLAST_FURNACE = register("blast_furnace");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> METAL_FORMER_EXTRUDING = register("metal_former_extruding");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> METAL_FORMER_CUTTING = register("metal_former_cutting");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> METAL_FORMER_ROLLING = register("metal_former_rolling");
	public static final RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> ORE_WASHER = register("ore_washing");
	public static final RecipeType<RecipeHolder<IRecipeInput, Integer>> MATTER_FABRICATOR = register("matter_fabricator");
	public static final RecipeType<RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack>> CANNER_BOTTLE = register("canner_bottle");
	public static final RecipeType<RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack>> CANNER_ENRICH = register("canner_enrich");

	public static void init()
	{
	}

	private static <T extends Recipe<?>> RecipeType<T> register(String name)
	{
		return IC2.envProxy.registerRecipeType(IC2.getIdentifier(name));
	}
}
