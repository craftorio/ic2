// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.misc;

import mezz.jei.api.gui.IGuiItemStackGroup;
import net.minecraft.item.ItemStack;
import java.util.List;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.gui.IRecipeLayout;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.BlankRecipeCategory;

public class ScrapboxRecipeCategory extends BlankRecipeCategory<IRecipeWrapper>
{
    public static final String UID = "ic2.scrapbox";
    private final IDrawable background;
    
    public ScrapboxRecipeCategory(final IGuiHelper guiHelper) {
        this.background = (IDrawable)guiHelper.createDrawable(new ResourceLocation("ic2:textures/gui/ScrapboxRecipes.png"), 55, 30, 82, 26);
    }
    
    public String getUid() {
        return "ic2.scrapbox";
    }
    
    public String getTitle() {
        return Localization.translate("ic2.crafting.scrap_box");
    }
    
    public IDrawable getBackground() {
        return this.background;
    }
    
    public void setRecipe(final IRecipeLayout recipeLayout, final IRecipeWrapper recipeWrapper, final IIngredients ingredients) {
        final IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 0, 4);
        itemStacks.init(1, true, 60, 4);
        itemStacks.set(0, (List)ingredients.getInputs((Class)ItemStack.class).get(0));
        itemStacks.set(1, (List)ingredients.getOutputs((Class)ItemStack.class).get(0));
    }
    
    public String getModName() {
        return "ic2";
    }
}
