package ic2.jeiIntegration.recipe.machine;

import ic2.core.util.StackUtil;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class CannerCanningHandler implements IRecipeHandler<CannerCanningWrapper> {
  public Class<CannerCanningWrapper> getRecipeClass() {
    return CannerCanningWrapper.class;
  }
  
  public String getRecipeCategoryUid(CannerCanningWrapper recipe) {
    return recipe.category.getUid();
  }
  
  public IRecipeWrapper getRecipeWrapper(CannerCanningWrapper recipe) {
    return (IRecipeWrapper)recipe;
  }
  
  public boolean isRecipeValid(CannerCanningWrapper recipe) {
    return (!recipe.getInput().isEmpty() && !recipe.getCan().isEmpty() && !StackUtil.isEmpty(recipe.getOutput()));
  }
}
