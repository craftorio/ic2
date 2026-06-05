package ic2.core.uu;

import ic2.core.IC2;
import ic2.core.recipe.AdvRecipe;
import ic2.core.util.LogCategory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public class VanillaSmeltingResolver implements IRecipeResolver {
   private static final double transformCost = 14.0;

   @Override
   public List<RecipeTransformation> getTransformations() {
      List<RecipeTransformation> ret = new ArrayList<>();

      for (Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet()) {
         try {
            List<List<LeanItemStack>> inputs = RecipeUtil.convertIngredients(AdvRecipe.expand(entry.getKey()));
            LeanItemStack output = new LeanItemStack(entry.getValue());
            ret.add(new RecipeTransformation(14.0, inputs, output));
         } catch (IllegalArgumentException e) {
            IC2.log.warn(LogCategory.Uu, e, "invalid recipe");
         }
      }

      return ret;
   }
}
