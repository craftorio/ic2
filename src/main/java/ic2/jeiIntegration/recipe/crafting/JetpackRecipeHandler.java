// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.crafting;

import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeHandler;

public class JetpackRecipeHandler implements IRecipeHandler<JetpackRecipeWrapper>
{
    public Class<JetpackRecipeWrapper> getRecipeClass() {
        return JetpackRecipeWrapper.class;
    }
    
    public String getRecipeCategoryUid(final JetpackRecipeWrapper recipe) {
        return "minecraft.crafting";
    }
    
    public IRecipeWrapper getRecipeWrapper(final JetpackRecipeWrapper wrapper) {
        return (IRecipeWrapper)wrapper;
    }
    
    public boolean isRecipeValid(final JetpackRecipeWrapper recipe) {
        return true;
    }
}
