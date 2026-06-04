package ic2.jeiIntegration.recipe.crafting;

import ic2.api.recipe.IRecipeInput;
import ic2.core.recipe.AdvRecipe;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.item.ItemStack;

public class AdvRecipeWrapper extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper {
  private final AdvRecipe recipe;
  
  public AdvRecipeWrapper(AdvRecipe recipe) {
    this.recipe = recipe;
  }
  
  public List<List<ItemStack>> getInputs() {
    int mask = this.recipe.masks[0];
    int itemIndex = 0;
    List<IRecipeInput> ret = new ArrayList<>();
    for (int i = 0; i < 9; i++) {
      if (i % 3 < this.recipe.inputWidth && i / 3 < this.recipe.inputHeight)
        if ((mask >>> 8 - i & 0x1) != 0) {
          ret.add(this.recipe.input[itemIndex++]);
        } else {
          ret.add(null);
        }  
    } 
    return replaceRecipeInputs(ret);
  }
  
  public int getWidth() {
    return this.recipe.inputWidth;
  }
  
  public int getHeight() {
    return this.recipe.inputHeight;
  }
  
  public static List<List<ItemStack>> replaceRecipeInputs(List<IRecipeInput> list) {
    List<List<ItemStack>> out = new ArrayList<>(list.size());
    for (IRecipeInput recipe : list) {
      if (recipe == null) {
        out.add(Collections.emptyList());
        continue;
      } 
      List<ItemStack> replace = new ArrayList<>(recipe.getInputs());
      for (ListIterator<ItemStack> it = replace.listIterator(); it.hasNext(); ) {
        ItemStack stack = it.next();
        if (stack != null && stack.getItem() instanceof ic2.api.item.IElectricItem)
          it.set(StackUtil.copyWithWildCard(stack)); 
      } 
      out.add(replace);
    } 
    return out;
  }
  
  public void getIngredients(IIngredients ingredients) {
    ingredients.setInputLists(ItemStack.class, getInputs());
    ingredients.setOutput(ItemStack.class, this.recipe.getRecipeOutput());
  }
}
