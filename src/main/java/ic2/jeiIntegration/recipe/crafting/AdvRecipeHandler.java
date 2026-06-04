// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.crafting;

import ic2.api.recipe.IRecipeInput;
import mezz.jei.api.recipe.IRecipeWrapper;
import javax.annotation.Nonnull;
import ic2.core.recipe.AdvRecipe;
import mezz.jei.api.recipe.IRecipeHandler;

public class AdvRecipeHandler implements IRecipeHandler<AdvRecipe>
{
    @Nonnull
    public Class<AdvRecipe> getRecipeClass() {
        return AdvRecipe.class;
    }
    
    @Nonnull
    public String getRecipeCategoryUid(final AdvRecipe recipe) {
        return "minecraft.crafting";
    }
    
    @Nonnull
    public IRecipeWrapper getRecipeWrapper(@Nonnull final AdvRecipe recipe) {
        return (IRecipeWrapper)new AdvRecipeWrapper(recipe);
    }
    
    public boolean isRecipeValid(@Nonnull final AdvRecipe recipe) {
        if (!recipe.canShow()) {
            return false;
        }
        for (final IRecipeInput input : recipe.input) {
            if (input.getInputs().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
