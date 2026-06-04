// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;

public class ItemCoke extends ItemIC2
{
    public ItemCoke() {
        super(ItemName.coke);
        this.setMaxStackSize(64);
    }
    
    public int getItemBurnTime(final ItemStack itemStack) {
        return 3200;
    }
}
