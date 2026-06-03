package ic2.jeiIntegration.recipe.machine;

import javax.annotation.Nonnull;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class IORecipeHandler implements IRecipeHandler<IORecipeWrapper> {
  public Class<IORecipeWrapper> getRecipeClass() {
    return IORecipeWrapper.class;
  }
  
  @Nonnull
  public IRecipeWrapper getRecipeWrapper(@Nonnull IORecipeWrapper recipe) {
    return (IRecipeWrapper)recipe;
  }
  
  public boolean isRecipeValid(@Nonnull IORecipeWrapper recipe) {
    return !recipe.getInputs().isEmpty();
  }
  
  public String getRecipeCategoryUid(IORecipeWrapper recipe) {
    return recipe.category.getUid();
  }
}
