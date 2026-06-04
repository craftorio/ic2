package ic2.core.recipe.dynamic;

import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

public class RecipeInputOreDictionary extends RecipeInputIngredient<String> {
  public final int amount;
  
  public final Integer meta;
  
  private List<ItemStack> equivalents;
  
  public static RecipeInputOreDictionary of(String ingredient) {
    return of(ingredient, 1);
  }
  
  public static RecipeInputOreDictionary of(String ingredient, int amount) {
    return of(ingredient, amount, null);
  }
  
  public static RecipeInputOreDictionary of(String ingredient, int amount, Integer meta) {
    return new RecipeInputOreDictionary(ingredient, amount, meta);
  }
  
  protected RecipeInputOreDictionary(String ingredient) {
    this(ingredient, 1);
  }
  
  protected RecipeInputOreDictionary(String ingredient, int amount) {
    this(ingredient, amount, null);
  }
  
  protected RecipeInputOreDictionary(String ingredient, int amount, Integer meta) {
    super(ingredient);
    this.amount = amount;
    this.meta = meta;
  }
  
  public Object getUnspecific() {
    return null;
  }
  
  public RecipeInputIngredient<String> copy() {
    throw new UnsupportedOperationException("Not supported");
  }
  
  public boolean isEmpty() {
    return (this.amount <= 0);
  }
  
  public int getCount() {
    return this.amount;
  }
  
  public void shrink(int amount) {
    throw new UnsupportedOperationException("Not supported");
  }
  
  public boolean matches(Object other) {
    if (!(other instanceof ItemStack))
      return false; 
    List<ItemStack> inputs = getEquivalents();
    boolean useOreStackMeta = (this.meta == null);
    Item subjectItem = ((ItemStack)other).getItem();
    int subjectMeta = ((ItemStack)other).func_77952_i();
    for (ItemStack oreStack : inputs) {
      Item oreItem = oreStack.getItem();
      if (oreItem == null)
        continue; 
      int metaRequired = useOreStackMeta ? oreStack.func_77952_i() : this.meta.intValue();
      if (subjectItem == oreItem && (subjectMeta == metaRequired || metaRequired == 32767))
        return true; 
    } 
    return false;
  }
  
  public boolean matchesStrict(Object other) {
    if (!(other instanceof String))
      return false; 
    return this.ingredient.equals(other);
  }
  
  public String toStringSafe() {
    return this.ingredient;
  }
  
  private List<ItemStack> getEquivalents() {
    if (this.equivalents != null)
      return this.equivalents; 
    NonNullList nonNullList = OreDictionary.getOres(this.ingredient);
    if (nonNullList != OreDictionary.EMPTY_LIST)
      this.equivalents = (List<ItemStack>)nonNullList; 
    return (List<ItemStack>)nonNullList;
  }
}
