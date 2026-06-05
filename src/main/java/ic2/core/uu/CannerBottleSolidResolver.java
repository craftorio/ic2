package ic2.core.uu;

import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.util.LogCategory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.ItemStack;

public class CannerBottleSolidResolver implements IRecipeResolver {
   private static final double transformCost = 0.0;

   @Override
   public List<RecipeTransformation> getTransformations() {
      List<RecipeTransformation> ret = new ArrayList<>();

      for (MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack> recipe : Recipes.cannerBottle.getRecipes()) {
         try {
            List<LeanItemStack> container = RecipeUtil.convertOutputs(recipe.getInput().container.getInputs());
            List<LeanItemStack> fill = RecipeUtil.convertOutputs(recipe.getInput().fill.getInputs());
            if (!container.isEmpty() && !fill.isEmpty()) {
               List<List<LeanItemStack>> inputs = new ArrayList<>(2);
               inputs.add(container);
               inputs.add(fill);
               ret.add(new RecipeTransformation(0.0, inputs, RecipeUtil.convertOutputs(Collections.singletonList(recipe.getOutput()))));
            }
         } catch (IllegalArgumentException e) {
            IC2.log.warn(LogCategory.Uu, e, "invalid recipe");
         }
      }

      return ret;
   }
}
