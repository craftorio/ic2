// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import net.minecraft.item.ItemStack;
import ic2.api.recipe.ICraftingRecipeManager;

public class AdvCraftingRecipeManager implements ICraftingRecipeManager
{
    @Override
    public void addRecipe(final ItemStack output, final Object... input) {
        AdvRecipe.addAndRegister(output, input);
    }
    
    @Override
    public void addShapelessRecipe(final ItemStack output, final Object... input) {
        AdvShapelessRecipe.addAndRegister(output, input);
    }
    
    public void addGradualRecipe(final ItemStack output, final int amount, final Object... args) {
        GradualRecipe.addAndRegister(output, amount, args);
    }
}
