package ic2.core.recipe.dynamic;

import java.util.Collection;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface IDynamicRecipeManager
{
	boolean addRecipe(DynamicRecipe var1, boolean var2);

	boolean removeRecipe(Collection<RecipeInputIngredient> var1, Collection<RecipeOutputIngredient> var2);

	DynamicRecipe apply(ItemStack[] var1, FluidStack[] var2, boolean var3);

	Iterable<? extends DynamicRecipe> getRecipes();

	boolean isIterable();
}
