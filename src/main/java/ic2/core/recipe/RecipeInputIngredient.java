package ic2.core.recipe;

import ic2.api.recipe.IRecipeInput;
import java.util.Arrays;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

public class RecipeInputIngredient implements IRecipeInput {
   private Ingredient ingredient;

   RecipeInputIngredient(Ingredient ingredient) {
      this.ingredient = ingredient;
   }

   @Override
   public boolean matches(ItemStack subject) {
      return this.ingredient.apply(subject);
   }

   @Override
   public List<ItemStack> getInputs() {
      return Arrays.asList(this.ingredient.getMatchingStacks());
   }

   @Override
   public int getAmount() {
      return 1;
   }

   @Override
   public Ingredient getIngredient() {
      return this.ingredient;
   }
}
