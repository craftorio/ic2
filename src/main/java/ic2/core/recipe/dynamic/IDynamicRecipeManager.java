package ic2.core.recipe.dynamic;

import java.util.Collection;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IDynamicRecipeManager {
  boolean addRecipe(DynamicRecipe paramDynamicRecipe, boolean paramBoolean);
  
  boolean removeRecipe(Collection<RecipeInputIngredient> paramCollection, Collection<RecipeOutputIngredient> paramCollection1);
  
  DynamicRecipe apply(ItemStack[] paramArrayOfItemStack, FluidStack[] paramArrayOfFluidStack, boolean paramBoolean);
  
  Iterable<? extends DynamicRecipe> getRecipes();
  
  boolean isIterable();
}
