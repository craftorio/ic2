package ic2.jeiIntegration.recipe.crafting;

import ic2.api.recipe.IRecipeInput;
import ic2.core.recipe.AdvRecipe;
import javax.annotation.Nonnull;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class AdvRecipeHandler implements IRecipeHandler<AdvRecipe> {
  @Nonnull
  public Class<AdvRecipe> getRecipeClass() {
    return AdvRecipe.class;
  }
  
  @Nonnull
  public String getRecipeCategoryUid(AdvRecipe recipe) {
    return "minecraft.crafting";
  }
  
  @Nonnull
  public IRecipeWrapper getRecipeWrapper(@Nonnull AdvRecipe recipe) {
    return (IRecipeWrapper)new AdvRecipeWrapper(recipe);
  }
  
  public boolean isRecipeValid(@Nonnull AdvRecipe recipe) {
    if (!recipe.canShow())
      return false; 
    for (IRecipeInput input : recipe.input) {
      if (input.getInputs().isEmpty())
        return false; 
    } 
    return true;
  }
}
