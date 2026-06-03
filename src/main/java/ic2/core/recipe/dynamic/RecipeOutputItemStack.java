package ic2.core.recipe.dynamic;

import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;

public class RecipeOutputItemStack extends RecipeOutputIngredient<ItemStack> {
  public static RecipeOutputItemStack of(ItemStack ingredient) {
    return new RecipeOutputItemStack(ingredient);
  }
  
  protected RecipeOutputItemStack(ItemStack ingredient) {
    super(ingredient);
  }
  
  public RecipeOutputIngredient<ItemStack> copy() {
    return of(this.ingredient.func_77946_l());
  }
  
  public boolean isEmpty() {
    return StackUtil.isEmpty(this.ingredient);
  }
  
  public boolean matches(Object other) {
    if (!(other instanceof ItemStack))
      return false; 
    return StackUtil.checkItemEqualityStrict(this.ingredient, (ItemStack)other);
  }
  
  public boolean matchesStrict(Object other) {
    return matches(other);
  }
  
  public String toStringSafe() {
    return StackUtil.toStringSafe(this.ingredient);
  }
}
