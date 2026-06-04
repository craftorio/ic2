// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.crafting;

import mezz.jei.api.ingredients.IIngredients;
import java.awt.Color;
import ic2.core.init.Localization;
import ic2.core.util.Util;
import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import java.util.List;
import ic2.core.recipe.GradualRecipe;
import mezz.jei.api.recipe.BlankRecipeWrapper;

public class GradualRecipeWrapper extends BlankRecipeWrapper
{
    private final GradualRecipe recipe;
    
    public GradualRecipeWrapper(final GradualRecipe recipe) {
        this.recipe = recipe;
    }
    
    public List<ItemStack> getInputs() {
        final List<ItemStack> ret = new ArrayList<ItemStack>(2);
        ret.add(this.recipe.chargeMaterial);
        final ItemStack repairItem = this.recipe.getRecipeOutput();
        this.recipe.item.setCustomDamage(repairItem, this.recipe.amount);
        ret.add(repairItem);
        return ret;
    }
    
    public void drawInfo(final Minecraft minecraft, final int recipeWidth, final int recipeHeight, final int mouseX, final int mouseY) {
        assert this.recipe.item.getMaxCustomDamage(this.recipe.getRecipeOutput()) > 0;
        final String effectiveness = Localization.translate("ic2.jei.condenser", Util.limit(this.recipe.amount / (float)this.recipe.item.getMaxCustomDamage(this.recipe.getRecipeOutput()) * 100.0f, 0.0f, 100.0f));
        final int width = minecraft.fontRenderer.getStringWidth(effectiveness);
        if (143 - width < 55) {
            minecraft.fontRenderer.drawSplitString(effectiveness, 55, 88, 90, Color.darkGray.getRGB());
        }
        else {
            minecraft.fontRenderer.drawString(effectiveness, (55 + (143 - width)) / 2, 42, Color.darkGray.getRGB());
        }
    }
    
    public void getIngredients(final IIngredients ingredients) {
        ingredients.setInputs((Class)ItemStack.class, (List)this.getInputs());
        ingredients.setOutput((Class)ItemStack.class, (Object)this.recipe.getRecipeOutput());
    }
}
