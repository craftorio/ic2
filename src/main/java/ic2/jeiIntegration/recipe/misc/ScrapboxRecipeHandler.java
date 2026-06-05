package ic2.jeiIntegration.recipe.misc;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ScrapboxRecipeHandler implements IRecipeHandler<ScrapboxRecipeWrapper> {
   public Class<ScrapboxRecipeWrapper> getRecipeClass() {
      return ScrapboxRecipeWrapper.class;
   }

   public String getRecipeCategoryUid(ScrapboxRecipeWrapper recipe) {
      return "ic2.scrapbox";
   }

   public IRecipeWrapper getRecipeWrapper(ScrapboxRecipeWrapper recipe) {
      return recipe;
   }

   public boolean isRecipeValid(ScrapboxRecipeWrapper recipe) {
      return true;
   }
}
