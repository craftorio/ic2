// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.crafting;

import ic2.api.recipe.IRecipeInput;
import mezz.jei.api.recipe.IRecipeWrapper;
import javax.annotation.Nonnull;
import ic2.core.recipe.AdvShapelessRecipe;
import mezz.jei.api.recipe.IRecipeHandler;

public class AdvShapelessRecipeHandler implements IRecipeHandler<AdvShapelessRecipe>
{
    @Nonnull
    public Class<AdvShapelessRecipe> getRecipeClass() {
        return AdvShapelessRecipe.class;
    }
    
    @Nonnull
    public String getRecipeCategoryUid(final AdvShapelessRecipe recipe) {
        return "minecraft.crafting";
    }
    
    @Nonnull
    public IRecipeWrapper getRecipeWrapper(@Nonnull final AdvShapelessRecipe recipe) {
        return (IRecipeWrapper)new AdvShapelessRecipeWrapper(recipe);
    }
    
    public boolean isRecipeValid(@Nonnull final AdvShapelessRecipe recipe) {
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
