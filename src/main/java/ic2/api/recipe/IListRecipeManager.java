// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import java.util.List;
import net.minecraft.item.ItemStack;

public interface IListRecipeManager extends Iterable<IRecipeInput>
{
    void add(final IRecipeInput p0);
    
    boolean contains(final ItemStack p0);
    
    boolean isEmpty();
    
    List<IRecipeInput> getInputs();
}
