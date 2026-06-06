package ic2.jeiIntegration.recipe.machine;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ElectrolyzerRecipeHandler implements IRecipeHandler<ElectrolyzerWrapper>
{
	public Class<ElectrolyzerWrapper> getRecipeClass()
	{
		return ElectrolyzerWrapper.class;
	}

	public String getRecipeCategoryUid(ElectrolyzerWrapper recipe)
	{
		return recipe.category.getUid();
	}

	public IRecipeWrapper getRecipeWrapper(ElectrolyzerWrapper recipe)
	{
		return recipe;
	}

	public boolean isRecipeValid(ElectrolyzerWrapper recipe)
	{
		return recipe.getFluidInput() != null && !recipe.getFluidOutputs().isEmpty();
	}
}
