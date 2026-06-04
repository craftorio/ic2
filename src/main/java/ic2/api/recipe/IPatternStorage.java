// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import java.util.List;
import net.minecraft.item.ItemStack;

public interface IPatternStorage
{
    boolean addPattern(final ItemStack p0);
    
    List<ItemStack> getPatterns();
}
