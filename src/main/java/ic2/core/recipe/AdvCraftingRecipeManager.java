package ic2.core.recipe;

import ic2.api.recipe.ICraftingRecipeManager;
import net.minecraft.item.ItemStack;

public class AdvCraftingRecipeManager implements ICraftingRecipeManager {
   @Override
   public void addRecipe(ItemStack output, Object... input) {
      AdvRecipe.addAndRegister(output, input);
   }

   @Override
   public void addShapelessRecipe(ItemStack output, Object... input) {
      AdvShapelessRecipe.addAndRegister(output, input);
   }

   public void addGradualRecipe(ItemStack output, int amount, Object... args) {
      GradualRecipe.addAndRegister(output, amount, args);
   }
}
