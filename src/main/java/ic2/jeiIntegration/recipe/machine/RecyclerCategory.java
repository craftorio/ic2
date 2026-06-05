package ic2.jeiIntegration.recipe.machine;

import ic2.api.recipe.IBasicMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.core.ref.TeBlock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class RecyclerCategory extends DynamicCategory<IBasicMachineRecipeManager> {
   private final List<List<ItemStack>> trueInputs;

   public RecyclerCategory(IGuiHelper guiHelper) {
      super(TeBlock.recycler, Recipes.recycler, guiHelper);
      List<ItemStack> items = new ArrayList<>();
      if (Recipes.recyclerWhitelist.isEmpty()) {
         for (Item i : ForgeRegistries.ITEMS) {
            ItemStack stack = new ItemStack(i, 1, 32767);
            if (!Recipes.recyclerBlacklist.contains(stack)) {
               items.add(stack);
            }
         }
      } else {
         for (IRecipeInput stack : Recipes.recyclerWhitelist) {
            items.addAll(stack.getInputs());
         }
      }

      this.trueInputs = Collections.singletonList(items);
   }

   @Override
   protected List<List<ItemStack>> getInputStacks(IIngredients wrapper) {
      return this.trueInputs;
   }
}
