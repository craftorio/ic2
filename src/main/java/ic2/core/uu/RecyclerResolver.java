package ic2.core.uu;

import ic2.api.recipe.Recipes;
import ic2.core.block.machine.tileentity.TileEntityRecycler;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.ItemStack;

public class RecyclerResolver implements ILateRecipeResolver {
  public List<RecipeTransformation> getTransformations(Iterable<LeanItemStack> obtainableStacks) {
    List<LeanItemStack> input = new ArrayList<>();
    for (LeanItemStack obtainableStack : obtainableStacks) {
      ItemStack stack = obtainableStack.toMcStack();
      if (StackUtil.isEmpty(stack))
        continue; 
      if (!((Collection)Recipes.recycler.apply(stack, false).getOutput()).isEmpty())
        input.add(new LeanItemStack(stack, TileEntityRecycler.recycleChance())); 
    } 
    return Arrays.asList(new RecipeTransformation[] { new RecipeTransformation(transformCost, Collections.singletonList(input), new LeanItemStack[] { new LeanItemStack(ItemName.crafting.getItemStack((Enum)CraftingItemType.scrap)) }) });
  }
  
  private static final double transformCost = 55.0D * TileEntityRecycler.recycleChance() / 4000.0D * 107.0D;
}
