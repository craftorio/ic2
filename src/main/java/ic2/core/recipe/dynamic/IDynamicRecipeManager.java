// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe.dynamic;

import net.minecraftforge.fluids.FluidStack;
import net.minecraft.item.ItemStack;
import java.util.Collection;

public interface IDynamicRecipeManager
{
    boolean addRecipe(final DynamicRecipe p0, final boolean p1);
    
    boolean removeRecipe(final Collection<RecipeInputIngredient> p0, final Collection<RecipeOutputIngredient> p1);
    
    DynamicRecipe apply(final ItemStack[] p0, final FluidStack[] p1, final boolean p2);
    
    Iterable<? extends DynamicRecipe> getRecipes();
    
    boolean isIterable();
}
