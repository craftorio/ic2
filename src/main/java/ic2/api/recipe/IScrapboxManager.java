// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import java.util.Map;
import net.minecraft.item.ItemStack;

public interface IScrapboxManager extends IBasicMachineRecipeManager
{
    void addDrop(final ItemStack p0, final float p1);
    
    ItemStack getDrop(final ItemStack p0, final boolean p1);
    
    Map<ItemStack, Float> getDrops();
}
