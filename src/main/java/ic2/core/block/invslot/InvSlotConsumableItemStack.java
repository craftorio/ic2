// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import java.util.HashSet;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.util.ItemComparableItemStack;
import java.util.Set;

public class InvSlotConsumableItemStack extends InvSlotConsumable
{
    private final Set<ItemComparableItemStack> stacks;
    
    public InvSlotConsumableItemStack(final IInventorySlotHolder<?> base1, final String name1, final int count, final ItemStack... stacks) {
        this(base1, name1, Access.I, count, InvSide.TOP, stacks);
    }
    
    public InvSlotConsumableItemStack(final IInventorySlotHolder<?> base1, final String name1, final Access access1, final int count, final InvSide preferredSide1, final ItemStack... stacks) {
        super(base1, name1, access1, count, preferredSide1);
        this.stacks = new HashSet<ItemComparableItemStack>();
        for (final ItemStack stack : stacks) {
            this.stacks.add(new ItemComparableItemStack(stack, true));
        }
    }
    
    @Override
    public boolean accepts(final ItemStack stack) {
        return this.stacks.contains(new ItemComparableItemStack(stack, false));
    }
}
