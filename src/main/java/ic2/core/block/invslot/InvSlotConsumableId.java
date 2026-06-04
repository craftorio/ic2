// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import net.minecraft.item.ItemStack;
import java.util.Collection;
import java.util.Arrays;
import java.util.HashSet;
import ic2.core.block.IInventorySlotHolder;
import net.minecraft.item.Item;
import java.util.Set;

public class InvSlotConsumableId extends InvSlotConsumable
{
    private final Set<Item> items;
    
    public InvSlotConsumableId(final IInventorySlotHolder<?> base1, final String name1, final int count, final Item... items) {
        this(base1, name1, Access.I, count, InvSide.TOP, items);
    }
    
    public InvSlotConsumableId(final IInventorySlotHolder<?> base1, final String name1, final Access access1, final int count, final InvSide preferredSide1, final Item... items) {
        super(base1, name1, access1, count, preferredSide1);
        (this.items = new HashSet<Item>()).addAll(Arrays.asList(items));
    }
    
    @Override
    public boolean accepts(final ItemStack stack) {
        return this.items.contains(stack.getItem());
    }
}
