// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.crafting;

import mezz.jei.api.ingredients.IIngredients;
import java.util.ListIterator;
import java.util.Iterator;
import ic2.core.util.StackUtil;
import ic2.api.item.IElectricItem;
import java.util.Collection;
import java.util.Collections;
import ic2.api.recipe.IRecipeInput;
import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import java.util.List;
import ic2.core.recipe.AdvRecipe;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import mezz.jei.api.recipe.BlankRecipeWrapper;

public class AdvRecipeWrapper extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper
{
    private final AdvRecipe recipe;
    
    public AdvRecipeWrapper(final AdvRecipe recipe) {
        this.recipe = recipe;
    }
    
    public List<List<ItemStack>> getInputs() {
        final int mask = this.recipe.masks[0];
        int itemIndex = 0;
        final List<IRecipeInput> ret = new ArrayList<IRecipeInput>();
        for (int i = 0; i < 9; ++i) {
            if (i % 3 < this.recipe.inputWidth) {
                if (i / 3 < this.recipe.inputHeight) {
                    if ((mask >>> 8 - i & 0x1) != 0x0) {
                        ret.add(this.recipe.input[itemIndex++]);
                    }
                    else {
                        ret.add(null);
                    }
                }
            }
        }
        return replaceRecipeInputs(ret);
    }
    
    public int getWidth() {
        return this.recipe.inputWidth;
    }
    
    public int getHeight() {
        return this.recipe.inputHeight;
    }
    
    public static List<List<ItemStack>> replaceRecipeInputs(final List<IRecipeInput> list) {
        final List<List<ItemStack>> out = new ArrayList<List<ItemStack>>(list.size());
        for (final IRecipeInput recipe : list) {
            if (recipe == null) {
                out.add(Collections.emptyList());
            }
            else {
                final List<ItemStack> replace = new ArrayList<ItemStack>(recipe.getInputs());
                final ListIterator<ItemStack> it = replace.listIterator();
                while (it.hasNext()) {
                    final ItemStack stack = it.next();
                    if (stack != null && stack.getItem() instanceof IElectricItem) {
                        it.set(StackUtil.copyWithWildCard(stack));
                    }
                }
                out.add(replace);
            }
        }
        return out;
    }
    
    public void getIngredients(final IIngredients ingredients) {
        ingredients.setInputLists((Class)ItemStack.class, (List)this.getInputs());
        ingredients.setOutput((Class)ItemStack.class, (Object)this.recipe.getRecipeOutput());
    }
}
