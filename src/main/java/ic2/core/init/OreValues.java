// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.init;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import ic2.core.util.ItemComparableItemStack;
import java.util.Map;

public class OreValues
{
    private static final Map<ItemComparableItemStack, Integer> stackValues;
    
    public static void add(final ItemStack stack, final int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("value has to be > 0");
        }
        final ItemComparableItemStack key = new ItemComparableItemStack(stack, true);
        final Integer prev = OreValues.stackValues.put(key, value);
        if (prev != null && prev > value) {
            OreValues.stackValues.put(key, prev);
        }
    }
    
    public static int get(final ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            return 0;
        }
        final Integer ret = OreValues.stackValues.get(new ItemComparableItemStack(stack, false));
        return (ret != null) ? (ret * StackUtil.getSize(stack)) : 0;
    }
    
    public static int get(final List<ItemStack> stacks) {
        int ret = 0;
        for (final ItemStack stack : stacks) {
            ret += get(stack);
        }
        return ret;
    }
    
    static {
        stackValues = new HashMap<ItemComparableItemStack, Integer>();
    }
}
