// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import net.minecraft.item.crafting.Ingredient;
import java.util.List;
import net.minecraft.item.ItemStack;

public interface IRecipeInput
{
    boolean matches(final ItemStack p0);
    
    int getAmount();
    
    List<ItemStack> getInputs();
    
    default Ingredient getIngredient() {
        return Recipes.inputFactory.getIngredient(this);
    }
}
