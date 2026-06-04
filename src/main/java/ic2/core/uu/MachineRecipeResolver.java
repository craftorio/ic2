// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.uu;

import java.util.Iterator;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.api.recipe.MachineRecipe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.IMachineRecipeManager;

public class MachineRecipeResolver implements IRecipeResolver
{
    private static final double transformCost = 14.0;
    private final IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?> manager;
    
    public MachineRecipeResolver(final IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?> manager) {
        this.manager = manager;
    }
    
    @Override
    public List<RecipeTransformation> getTransformations() {
        if (!this.manager.isIterable()) {
            return Collections.emptyList();
        }
        final List<RecipeTransformation> ret = new ArrayList<RecipeTransformation>();
        for (final MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe : this.manager.getRecipes()) {
            try {
                final List<List<LeanItemStack>> inputs = RecipeUtil.convertIngredients(recipe.getInput().getInputs());
                final List<LeanItemStack> outputs = RecipeUtil.convertOutputs(recipe.getOutput());
                ret.add(new RecipeTransformation(14.0, inputs, outputs));
            }
            catch (final IllegalArgumentException e) {
                IC2.log.warn(LogCategory.Uu, e, "invalid recipe");
            }
        }
        return ret;
    }
}
