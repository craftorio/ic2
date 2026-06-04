// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.machine;

import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeHandler;

public class ElectrolyzerRecipeHandler implements IRecipeHandler<ElectrolyzerWrapper>
{
    public Class<ElectrolyzerWrapper> getRecipeClass() {
        return ElectrolyzerWrapper.class;
    }
    
    public String getRecipeCategoryUid(final ElectrolyzerWrapper recipe) {
        return recipe.category.getUid();
    }
    
    public IRecipeWrapper getRecipeWrapper(final ElectrolyzerWrapper recipe) {
        return (IRecipeWrapper)recipe;
    }
    
    public boolean isRecipeValid(final ElectrolyzerWrapper recipe) {
        return recipe.getFluidInput() != null && !recipe.getFluidOutputs().isEmpty();
    }
}
