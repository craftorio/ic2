package ic2.core.recipe.dynamic;

import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;

public class RecipeInputItemStack extends RecipeInputIngredient<ItemStack> {
  public static RecipeInputItemStack of(ItemStack ingredient) {
    return new RecipeInputItemStack(ingredient);
  }
  
  public static RecipeInputItemStack of(ItemStack ingredient, boolean consumable) {
    return new RecipeInputItemStack(ingredient, consumable);
  }
  
  protected RecipeInputItemStack(ItemStack ingredient) {
    super(ingredient);
  }
  
  protected RecipeInputItemStack(ItemStack ingredient, boolean consumable) {
    super(ingredient, consumable);
  }
  
  public Object getUnspecific() {
    return this.ingredient.getItem();
  }
  
  public RecipeInputIngredient<ItemStack> copy() {
    return of(this.ingredient.copy());
  }
  
  public boolean isEmpty() {
    return StackUtil.isEmpty(this.ingredient);
  }
  
  public int getCount() {
    return StackUtil.getSize(this.ingredient);
  }
  
  public void shrink(int amount) {
    this.ingredient.shrink(amount);
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
