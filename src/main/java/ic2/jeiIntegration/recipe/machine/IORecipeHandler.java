// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.machine;

import mezz.jei.api.recipe.IRecipeWrapper;
import javax.annotation.Nonnull;
import mezz.jei.api.recipe.IRecipeHandler;

public class IORecipeHandler implements IRecipeHandler<IORecipeWrapper>
{
    public Class<IORecipeWrapper> getRecipeClass() {
        return IORecipeWrapper.class;
    }
    
    @Nonnull
    public IRecipeWrapper getRecipeWrapper(@Nonnull final IORecipeWrapper recipe) {
        return (IRecipeWrapper)recipe;
    }
    
    public boolean isRecipeValid(@Nonnull final IORecipeWrapper recipe) {
        return !recipe.getInputs().isEmpty();
    }
    
    public String getRecipeCategoryUid(final IORecipeWrapper recipe) {
        return recipe.category.getUid();
    }
}
