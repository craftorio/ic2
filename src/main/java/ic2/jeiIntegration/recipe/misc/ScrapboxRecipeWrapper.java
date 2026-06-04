// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.misc;

import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import mezz.jei.api.ingredients.IIngredients;
import java.util.Iterator;
import ic2.api.recipe.Recipes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import java.util.Map;
import mezz.jei.api.recipe.BlankRecipeWrapper;

public class ScrapboxRecipeWrapper extends BlankRecipeWrapper
{
    private final Map.Entry<ItemStack, Float> entry;
    
    public ScrapboxRecipeWrapper(final Map.Entry<ItemStack, Float> entry) {
        this.entry = entry;
    }
    
    public void drawInfo(final Minecraft minecraft, final int recipeWidth, final int recipeHeight, final int mouseX, final int mouseY) {
        final float value = this.entry.getValue();
        String text;
        if (value < 0.001) {
            text = "< 0.01";
        }
        else {
            text = "  " + String.format("%.2f", value * 100.0f);
        }
        minecraft.fontRenderer.drawString(text + "%", 86, 9, 4210752);
    }
    
    public static List<ScrapboxRecipeWrapper> createRecipes() {
        final List<ScrapboxRecipeWrapper> recipes = new ArrayList<ScrapboxRecipeWrapper>();
        for (final Map.Entry<ItemStack, Float> e : Recipes.scrapboxDrops.getDrops().entrySet()) {
            recipes.add(new ScrapboxRecipeWrapper(e));
        }
        return recipes;
    }
    
    public void getIngredients(final IIngredients ingredients) {
        ingredients.setInput((Class)ItemStack.class, (Object)ItemName.crafting.getItemStack(CraftingItemType.scrap_box));
        ingredients.setOutput((Class)ItemStack.class, (Object)this.entry.getKey());
    }
}
