package ic2.jeiIntegration.recipe.crafting;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class JetpackRecipeHandler implements IRecipeHandler<JetpackRecipeWrapper> {
   public Class<JetpackRecipeWrapper> getRecipeClass() {
      return JetpackRecipeWrapper.class;
   }

   public String getRecipeCategoryUid(JetpackRecipeWrapper recipe) {
      return "minecraft.crafting";
   }

   public IRecipeWrapper getRecipeWrapper(JetpackRecipeWrapper wrapper) {
      return wrapper;
   }

   public boolean isRecipeValid(JetpackRecipeWrapper recipe) {
      return true;
   }
}
