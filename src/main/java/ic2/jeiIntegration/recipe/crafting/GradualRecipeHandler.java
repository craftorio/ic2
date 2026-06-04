// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.crafting;

import mezz.jei.api.recipe.IRecipeWrapper;
import ic2.core.recipe.GradualRecipe;
import mezz.jei.api.recipe.IRecipeHandler;

public class GradualRecipeHandler implements IRecipeHandler<GradualRecipe>
{
    public Class<GradualRecipe> getRecipeClass() {
        return GradualRecipe.class;
    }
    
    public String getRecipeCategoryUid(final GradualRecipe recipe) {
        return "minecraft.crafting";
    }
    
    public IRecipeWrapper getRecipeWrapper(final GradualRecipe recipe) {
        return (IRecipeWrapper)new GradualRecipeWrapper(recipe);
    }
    
    public boolean isRecipeValid(final GradualRecipe recipe) {
        return recipe.canShow() && recipe.chargeMaterial != null;
    }
}
