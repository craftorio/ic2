// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;

public class InvSlotConsumableLinked extends InvSlotConsumable
{
    public final InvSlot linkedSlot;
    
    public InvSlotConsumableLinked(final IInventorySlotHolder<?> base1, final String name1, final int count, final InvSlot linkedSlot1) {
        super(base1, name1, count);
        this.linkedSlot = linkedSlot1;
    }
    
    @Override
    public boolean accepts(final ItemStack stack) {
        final ItemStack required = this.linkedSlot.get();
        return !StackUtil.isEmpty(required) && StackUtil.checkItemEqualityStrict(required, stack);
    }
    
    public ItemStack consumeLinked(final boolean simulate) {
        final ItemStack required = this.linkedSlot.get();
        if (StackUtil.isEmpty(required)) {
            return null;
        }
        final int reqAmount = StackUtil.getSize(required);
        final ItemStack available = this.consume(reqAmount, true, true);
        if (!StackUtil.isEmpty(available) && StackUtil.getSize(available) == reqAmount) {
            return this.consume(reqAmount, simulate, true);
        }
        return null;
    }
}
