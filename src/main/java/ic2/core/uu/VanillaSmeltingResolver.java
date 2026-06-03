package ic2.core.uu;

import ic2.core.IC2;
import ic2.core.recipe.AdvRecipe;
import ic2.core.util.LogCategory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public class VanillaSmeltingResolver implements IRecipeResolver {
  private static final double transformCost = 14.0D;
  
  public List<RecipeTransformation> getTransformations() {
    List<RecipeTransformation> ret = new ArrayList<>();
    for (Map.Entry<ItemStack, ItemStack> entry : (Iterable<Map.Entry<ItemStack, ItemStack>>)FurnaceRecipes.func_77602_a().func_77599_b().entrySet()) {
      try {
        List<List<LeanItemStack>> inputs = RecipeUtil.convertIngredients(AdvRecipe.expand(entry.getKey()));
        LeanItemStack output = new LeanItemStack(entry.getValue());
        ret.add(new RecipeTransformation(14.0D, inputs, new LeanItemStack[] { output }));
      } catch (IllegalArgumentException e) {
        IC2.log.warn(LogCategory.Uu, e, "invalid recipe");
      } 
    } 
    return ret;
  }
}
