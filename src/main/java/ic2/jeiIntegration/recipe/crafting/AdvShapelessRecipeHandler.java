package ic2.jeiIntegration.recipe.crafting;

import ic2.api.recipe.IRecipeInput;
import ic2.core.recipe.AdvShapelessRecipe;
import javax.annotation.Nonnull;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class AdvShapelessRecipeHandler implements IRecipeHandler<AdvShapelessRecipe> {
   @Nonnull
   public Class<AdvShapelessRecipe> getRecipeClass() {
      return AdvShapelessRecipe.class;
   }

   @Nonnull
   public String getRecipeCategoryUid(AdvShapelessRecipe recipe) {
      return "minecraft.crafting";
   }

   @Nonnull
   public IRecipeWrapper getRecipeWrapper(@Nonnull AdvShapelessRecipe recipe) {
      return new AdvShapelessRecipeWrapper(recipe);
   }

   public boolean isRecipeValid(@Nonnull AdvShapelessRecipe recipe) {
      if (!recipe.canShow()) {
         return false;
      }

      for (IRecipeInput input : recipe.input) {
         if (input.getInputs().isEmpty()) {
            return false;
         }
      }

      return true;
   }
}
