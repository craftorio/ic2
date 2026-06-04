// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.crafting;

import mezz.jei.api.ingredients.IIngredients;
import ic2.api.recipe.IRecipeInput;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.Arrays;
import ic2.core.util.Ic2Color;
import ic2.core.ref.ItemName;
import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import java.util.List;
import ic2.core.recipe.AdvShapelessRecipe;
import mezz.jei.api.recipe.BlankRecipeWrapper;

public class AdvShapelessRecipeWrapper extends BlankRecipeWrapper
{
    private final AdvShapelessRecipe recipe;
    
    public AdvShapelessRecipeWrapper(final AdvShapelessRecipe recipe) {
        this.recipe = recipe;
    }
    
    public List<List<ItemStack>> getInputs() {
        final List<List<ItemStack>> ret = new ArrayList<List<ItemStack>>(this.recipe.input.length);
        for (final IRecipeInput input : this.recipe.input) {
            ret.add(input.getInputs());
        }
        if (ret.size() == 1 && ret.get(0).size() == 1) {
            final ItemStack stack = ret.get(0).get(0);
            if (stack.getItem() == ItemName.painter.getInstance() && stack.getMetadata() == 32767) {
                ret.set(0, Arrays.stream(Ic2Color.values).map((Function<? super Ic2Color, ?>)ItemName.painter::getItemStack).collect((Collector<? super Object, ?, List<ItemStack>>)Collectors.toList()));
            }
        }
        return ret;
    }
    
    public void getIngredients(final IIngredients ingredients) {
        ingredients.setInputLists((Class)ItemStack.class, (List)this.getInputs());
        ingredients.setOutput((Class)ItemStack.class, (Object)this.recipe.getRecipeOutput());
    }
}
