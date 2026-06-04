// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.uu;

import java.util.Iterator;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.core.recipe.AdvRecipe;
import net.minecraft.item.ItemStack;
import java.util.Map;
import net.minecraft.item.crafting.FurnaceRecipes;
import java.util.ArrayList;
import java.util.List;

public class VanillaSmeltingResolver implements IRecipeResolver
{
    private static final double transformCost = 14.0;
    
    @Override
    public List<RecipeTransformation> getTransformations() {
        final List<RecipeTransformation> ret = new ArrayList<RecipeTransformation>();
        for (final Map.Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet()) {
            try {
                final List<List<LeanItemStack>> inputs = RecipeUtil.convertIngredients(AdvRecipe.expand(entry.getKey()));
                final LeanItemStack output = new LeanItemStack(entry.getValue());
                ret.add(new RecipeTransformation(14.0, inputs, new LeanItemStack[] { output }));
            }
            catch (final IllegalArgumentException e) {
                IC2.log.warn(LogCategory.Uu, e, "invalid recipe");
            }
        }
        return ret;
    }
}
