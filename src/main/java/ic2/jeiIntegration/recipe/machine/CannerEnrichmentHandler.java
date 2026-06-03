package ic2.jeiIntegration.recipe.machine;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class CannerEnrichmentHandler implements IRecipeHandler<CannerEnrichmentWrapper> {
  public Class<CannerEnrichmentWrapper> getRecipeClass() {
    return CannerEnrichmentWrapper.class;
  }
  
  public String getRecipeCategoryUid(CannerEnrichmentWrapper recipe) {
    return recipe.category.getUid();
  }
  
  public IRecipeWrapper getRecipeWrapper(CannerEnrichmentWrapper recipe) {
    return (IRecipeWrapper)recipe;
  }
  
  public boolean isRecipeValid(CannerEnrichmentWrapper recipe) {
    return (!recipe.getAdditives().isEmpty() && recipe.getInput() != null && recipe.getOutput() != null);
  }
}
