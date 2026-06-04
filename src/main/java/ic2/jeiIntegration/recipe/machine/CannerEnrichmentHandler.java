// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.machine;

import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeHandler;

public class CannerEnrichmentHandler implements IRecipeHandler<CannerEnrichmentWrapper>
{
    public Class<CannerEnrichmentWrapper> getRecipeClass() {
        return CannerEnrichmentWrapper.class;
    }
    
    public String getRecipeCategoryUid(final CannerEnrichmentWrapper recipe) {
        return recipe.category.getUid();
    }
    
    public IRecipeWrapper getRecipeWrapper(final CannerEnrichmentWrapper recipe) {
        return (IRecipeWrapper)recipe;
    }
    
    public boolean isRecipeValid(final CannerEnrichmentWrapper recipe) {
        return !recipe.getAdditives().isEmpty() && recipe.getInput() != null && recipe.getOutput() != null;
    }
}
