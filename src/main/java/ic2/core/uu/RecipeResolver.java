// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.uu;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import java.util.Iterator;
import net.minecraft.item.crafting.Ingredient;
import ic2.core.util.StackUtil;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import java.util.ArrayList;
import java.util.List;

public class RecipeResolver implements IRecipeResolver
{
    private static final double transformCost = 1.0;
    
    @Override
    public List<RecipeTransformation> getTransformations() {
        final List<RecipeTransformation> ret = new ArrayList<RecipeTransformation>();
        for (final IRecipe irecipe : ForgeRegistries.RECIPES) {
            final NonNullList<Ingredient> inputs = (NonNullList<Ingredient>)irecipe.getIngredients();
            final ItemStack output = irecipe.getRecipeOutput();
            if (!StackUtil.isEmpty(output)) {
                if (inputs.isEmpty()) {
                    continue;
                }
                ret.add(new RecipeTransformation(1.0, toDoubleStackList((List<Ingredient>)inputs), new LeanItemStack[] { new LeanItemStack(output) }));
            }
        }
        return ret;
    }
    
    private static List<List<LeanItemStack>> toDoubleStackList(final List<Ingredient> list) {
        final List<List<LeanItemStack>> ret = new ArrayList<List<LeanItemStack>>(list.size());
        for (final Ingredient ingredient : list) {
            final ItemStack[] arr = ingredient.getMatchingStacks();
            final List<LeanItemStack> toAdd = new ArrayList<LeanItemStack>(arr.length);
            for (final ItemStack stack : arr) {
                toAdd.add(new LeanItemStack(stack));
            }
            ret.add(toAdd);
        }
        return ret;
    }
}
