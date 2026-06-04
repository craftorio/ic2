// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.transport.cover;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.item.ItemStack;
import java.util.List;

public class CoverRegistry
{
    private static final List<ItemStack> covers;
    
    public static ItemStack register(final ItemStack stack) {
        if (!(stack.getItem() instanceof ICoverItem)) {
            throw new IllegalArgumentException("The stack must represent an ICoverItem.");
        }
        CoverRegistry.covers.add(stack);
        return stack;
    }
    
    public static Iterable<ItemStack> getCovers() {
        return (Iterable<ItemStack>)Collections.unmodifiableCollection((Collection<?>)CoverRegistry.covers);
    }
    
    static {
        covers = new ArrayList<ItemStack>();
    }
}
