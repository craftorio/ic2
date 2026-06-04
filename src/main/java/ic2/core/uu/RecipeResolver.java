package ic2.core.uu;

import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class RecipeResolver implements IRecipeResolver {
  private static final double transformCost = 1.0D;
  
  public List<RecipeTransformation> getTransformations() {
    List<RecipeTransformation> ret = new ArrayList<>();
    for (IRecipe irecipe : ForgeRegistries.RECIPES) {
      NonNullList<Ingredient> inputs = irecipe.getIngredients();
      ItemStack output = irecipe.getRecipeOutput();
      if (StackUtil.isEmpty(output) || inputs.isEmpty())
        continue; 
      ret.add(new RecipeTransformation(1.0D, toDoubleStackList((List<Ingredient>)inputs), new LeanItemStack[] { new LeanItemStack(output) }));
    } 
    return ret;
  }
  
  private static List<List<LeanItemStack>> toDoubleStackList(List<Ingredient> list) {
    List<List<LeanItemStack>> ret = new ArrayList<>(list.size());
    for (Ingredient ingredient : list) {
      ItemStack[] arr = ingredient.getMatchingStacks();
      List<LeanItemStack> toAdd = new ArrayList<>(arr.length);
      for (ItemStack stack : arr)
        toAdd.add(new LeanItemStack(stack)); 
      ret.add(toAdd);
    } 
    return ret;
  }
}
