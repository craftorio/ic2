// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.uu;

import java.util.Iterator;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import java.util.Collections;
import java.util.Collection;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.Recipes;
import java.util.ArrayList;
import java.util.List;

public class CannerBottleSolidResolver implements IRecipeResolver
{
    private static final double transformCost = 0.0;
    
    @Override
    public List<RecipeTransformation> getTransformations() {
        final List<RecipeTransformation> ret = new ArrayList<RecipeTransformation>();
        for (final MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack> recipe : Recipes.cannerBottle.getRecipes()) {
            try {
                final List<LeanItemStack> container = RecipeUtil.convertOutputs(recipe.getInput().container.getInputs());
                final List<LeanItemStack> fill = RecipeUtil.convertOutputs(recipe.getInput().fill.getInputs());
                if (container.isEmpty() || fill.isEmpty()) {
                    continue;
                }
                final List<List<LeanItemStack>> inputs = new ArrayList<List<LeanItemStack>>(2);
                inputs.add(container);
                inputs.add(fill);
                ret.add(new RecipeTransformation(0.0, inputs, RecipeUtil.convertOutputs(Collections.singletonList(recipe.getOutput()))));
            }
            catch (final IllegalArgumentException e) {
                IC2.log.warn(LogCategory.Uu, e, "invalid recipe");
            }
        }
        return ret;
    }
}
