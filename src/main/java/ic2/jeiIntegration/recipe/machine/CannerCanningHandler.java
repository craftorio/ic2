// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.machine;

import ic2.core.util.StackUtil;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeHandler;

public class CannerCanningHandler implements IRecipeHandler<CannerCanningWrapper>
{
    public Class<CannerCanningWrapper> getRecipeClass() {
        return CannerCanningWrapper.class;
    }
    
    public String getRecipeCategoryUid(final CannerCanningWrapper recipe) {
        return recipe.category.getUid();
    }
    
    public IRecipeWrapper getRecipeWrapper(final CannerCanningWrapper recipe) {
        return (IRecipeWrapper)recipe;
    }
    
    public boolean isRecipeValid(final CannerCanningWrapper recipe) {
        return !recipe.getInput().isEmpty() && !recipe.getCan().isEmpty() && !StackUtil.isEmpty(recipe.getOutput());
    }
}
