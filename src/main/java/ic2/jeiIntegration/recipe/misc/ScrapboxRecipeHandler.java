// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.misc;

import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeHandler;

public class ScrapboxRecipeHandler implements IRecipeHandler<ScrapboxRecipeWrapper>
{
    public Class<ScrapboxRecipeWrapper> getRecipeClass() {
        return ScrapboxRecipeWrapper.class;
    }
    
    public String getRecipeCategoryUid(final ScrapboxRecipeWrapper recipe) {
        return "ic2.scrapbox";
    }
    
    public IRecipeWrapper getRecipeWrapper(final ScrapboxRecipeWrapper recipe) {
        return (IRecipeWrapper)recipe;
    }
    
    public boolean isRecipeValid(final ScrapboxRecipeWrapper recipe) {
        return true;
    }
}
