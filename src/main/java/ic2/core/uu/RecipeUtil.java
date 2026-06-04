// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.uu;

import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import java.util.List;

class RecipeUtil
{
    public static List<List<LeanItemStack>> fixIngredientSize(final List<ItemStack>[] x) {
        final List<List<LeanItemStack>> ret = new ArrayList<List<LeanItemStack>>(x.length);
        for (int i = 0; i < x.length; ++i) {
            final List<ItemStack> listIn = x[i];
            if (listIn != null) {
                final List<LeanItemStack> listOut = new ArrayList<LeanItemStack>(listIn.size());
                for (final ItemStack stack : x[i]) {
                    listOut.add(new LeanItemStack(stack, 1));
                }
                ret.add(listOut);
            }
        }
        return ret;
    }
    
    public static List<List<LeanItemStack>> convertIngredients(final List<ItemStack> x) {
        return Collections.singletonList(convertOutputs(x));
    }
    
    public static List<LeanItemStack> convertOutputs(final Collection<ItemStack> x) {
        final List<LeanItemStack> ret = new ArrayList<LeanItemStack>(x.size());
        for (final ItemStack stack : x) {
            ret.add(new LeanItemStack(stack));
        }
        return ret;
    }
}
