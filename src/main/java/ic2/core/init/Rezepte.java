package ic2.core.init;

import ic2.api.recipe.IBasicMachineRecipeManager;
import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.block.machine.CannerBottleRecipeManager;
import ic2.core.block.machine.CannerEnrichRecipeManager;
import ic2.core.block.machine.EmptyFluidContainerRecipeManager;
import ic2.core.block.machine.FillFluidContainerRecipeManager;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.recipe.BasicMachineRecipeManager;
import ic2.core.recipe.MatterAmplifierRecipeManager;
import ic2.core.recipe.SmeltingRecipeManager;
import ic2.core.recipe.v2.RecipeHolder;
import ic2.core.recipe.v2.RecipeManagerGetter;
import ic2.core.ref.Ic2RecipeTypes;

import java.util.Collection;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class Rezepte
{
	static void loadRecipes()
	{
		Recipes.furnace = new SmeltingRecipeManager();
		Recipes.emptyFluidContainer = new EmptyFluidContainerRecipeManager();
		Recipes.fillFluidContainer = new FillFluidContainerRecipeManager();
		Recipes.macerator = basicRecipe(Ic2RecipeTypes.MACERATOR);
		Recipes.extractor = basicRecipe(Ic2RecipeTypes.EXTRACTOR);
		Recipes.compressor = basicRecipe(Ic2RecipeTypes.COMPRESSOR);
		Recipes.centrifuge = basicRecipe(Ic2RecipeTypes.CENTRIFUGE);
		Recipes.blockcutter = basicRecipe(Ic2RecipeTypes.BLOCK_CUTTER);
		Recipes.blastfurnace = basicRecipe(Ic2RecipeTypes.BLAST_FURNACE);
		Recipes.metalformerExtruding = basicRecipe(Ic2RecipeTypes.METAL_FORMER_EXTRUDING);
		Recipes.metalformerCutting = basicRecipe(Ic2RecipeTypes.METAL_FORMER_CUTTING);
		Recipes.metalformerRolling = basicRecipe(Ic2RecipeTypes.METAL_FORMER_ROLLING);
		Recipes.oreWashing = basicRecipe(Ic2RecipeTypes.ORE_WASHER);
		Recipes.matterFabricator = new RecipeManagerGetter<>(recipeManager ->
		{
			MatterAmplifierRecipeManager manager = new MatterAmplifierRecipeManager();

			for (RecipeHolder<IRecipeInput, Integer> holder : recipeManager.m_44013_(Ic2RecipeTypes.MATTER_FABRICATOR))
			{
				manager.addRecipe(holder.recipe().getInput(), holder.recipe().getOutput(), null, false);
			}

			return manager;
		});
		Recipes.cannerBottle = new RecipeManagerGetter<>(recipeManager ->
		{
			CannerBottleRecipeManager manager = new CannerBottleRecipeManager();

			for (RecipeHolder<ICannerBottleRecipeManager.Input, ItemStack> holder : recipeManager.m_44013_(Ic2RecipeTypes.CANNER_BOTTLE))
			{
				manager.addRecipe(holder.recipe().getInput(), holder.recipe().getOutput(), null, false);
			}

			return manager;
		});
		Recipes.cannerEnrich = new RecipeManagerGetter<>(recipeManager ->
		{
			CannerEnrichRecipeManager manager = new CannerEnrichRecipeManager();

			for (RecipeHolder<ICannerEnrichRecipeManager.Input, Ic2FluidStack> holder : recipeManager.m_44013_(Ic2RecipeTypes.CANNER_ENRICH))
			{
				manager.addRecipe(holder.recipe().getInput(), holder.recipe().getOutput(), null, false);
			}

			return manager;
		});
	}

	public static void registerRecipes()
	{
		loadRecipes();
	}

	private static RecipeManagerGetter<IBasicMachineRecipeManager> basicRecipe(RecipeType<RecipeHolder<IRecipeInput, Collection<ItemStack>>> recipeType)
	{
		return new RecipeManagerGetter<>(recipeManager ->
		{
			BasicMachineRecipeManager manager = new BasicMachineRecipeManager();

			for (RecipeHolder<IRecipeInput, Collection<ItemStack>> holder : recipeManager.m_44013_(recipeType))
			{
				manager.addRecipe(holder.recipe(), false);
			}

			return manager;
		});
	}
}
