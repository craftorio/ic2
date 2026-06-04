// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.ref;

import java.util.Iterator;
import ic2.core.util.StackUtil;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.item.ItemStack;
import ic2.core.block.state.IIdProvider;

public interface IMultiItem<T extends IIdProvider>
{
    ItemStack getItemStack(final T p0);
    
    ItemStack getItemStack(final String p0);
    
    String getVariant(final ItemStack p0);
    
    Set<T> getAllTypes();
    
    default Set<ItemStack> getAllStacks() {
        final Set<ItemStack> ret = new HashSet<ItemStack>();
        for (final T type : this.getAllTypes()) {
            ret.add(this.getItemStack(type));
        }
        ret.remove(null);
        ret.remove(StackUtil.emptyStack);
        return ret;
    }
}
