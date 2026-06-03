package ic2.api.recipe;

import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

public interface IRecipeInput {
  boolean matches(ItemStack paramItemStack);
  
  int getAmount();
  
  List<ItemStack> getInputs();
  
  default Ingredient getIngredient() {
    return Recipes.inputFactory.getIngredient(this);
  }
}
