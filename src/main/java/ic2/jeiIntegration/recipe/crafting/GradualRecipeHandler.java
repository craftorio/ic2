package ic2.jeiIntegration.recipe.crafting;

import ic2.core.recipe.GradualRecipe;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class GradualRecipeHandler implements IRecipeHandler<GradualRecipe> {
   public Class<GradualRecipe> getRecipeClass() {
      return GradualRecipe.class;
   }

   public String getRecipeCategoryUid(GradualRecipe recipe) {
      return "minecraft.crafting";
   }

   public IRecipeWrapper getRecipeWrapper(GradualRecipe recipe) {
      return new GradualRecipeWrapper(recipe);
   }

   public boolean isRecipeValid(GradualRecipe recipe) {
      return recipe.canShow() && recipe.chargeMaterial != null;
   }
}
