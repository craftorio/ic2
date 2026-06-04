// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;

public class InvSlotConsumableClass extends InvSlotConsumable
{
    private final Class<?> clazz;
    
    public InvSlotConsumableClass(final IInventorySlotHolder<?> base1, final String name1, final Access access1, final int count, final InvSide preferredSide1, final Class<?> clazz) {
        super(base1, name1, access1, count, preferredSide1);
        this.clazz = clazz;
    }
    
    public InvSlotConsumableClass(final IInventorySlotHolder<?> base1, final String name1, final int count, final Class<?> clazz) {
        super(base1, name1, count);
        this.clazz = clazz;
    }
    
    @Override
    public boolean accepts(final ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            return false;
        }
        if (stack.getItem() instanceof ItemBlock) {
            return this.clazz.isInstance(Block.getBlockFromItem(stack.getItem()));
        }
        return this.clazz.isInstance(stack.getItem());
    }
}
