// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import java.util.Iterator;
import ic2.core.util.StackUtil;
import java.util.Collections;
import java.util.Collection;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;

public class InvSlotOutput extends InvSlot
{
    public InvSlotOutput(final IInventorySlotHolder<?> base1, final String name1, final int count) {
        this(base1, name1, count, InvSide.BOTTOM);
    }
    
    public InvSlotOutput(final IInventorySlotHolder<?> base1, final String name1, final int count, final InvSide side) {
        super(base1, name1, Access.O, count, side);
    }
    
    @Override
    public boolean accepts(final ItemStack stack) {
        return false;
    }
    
    public int add(final Collection<ItemStack> stacks) {
        return this.add(stacks, false);
    }
    
    public int add(final ItemStack stack) {
        if (stack == null) {
            throw new NullPointerException("null ItemStack");
        }
        return this.add(Collections.singletonList(stack), false);
    }
    
    public boolean canAdd(final Collection<ItemStack> stacks) {
        return this.add(stacks, true) == 0;
    }
    
    public boolean canAdd(final ItemStack stack) {
        if (stack == null) {
            throw new NullPointerException("null ItemStack");
        }
        return this.add(Collections.singletonList(stack), true) == 0;
    }
    
    private int add(final Collection<ItemStack> stacks, final boolean simulate) {
        if (stacks == null || stacks.isEmpty()) {
            return 0;
        }
        final ItemStack[] backup = (ItemStack[])(simulate ? this.backup() : null);
        int totalAmount = 0;
        for (final ItemStack stack : stacks) {
            int amount = StackUtil.getSize(stack);
            if (amount <= 0) {
                continue;
            }
        Label_0289:
            for (int pass = 0; pass < 2; ++pass) {
                for (int i = 0; i < this.size(); ++i) {
                    final ItemStack existingStack = this.get(i);
                    int space = this.getStackSizeLimit();
                    if (!StackUtil.isEmpty(existingStack)) {
                        space = Math.min(space, existingStack.getMaxStackSize()) - StackUtil.getSize(existingStack);
                    }
                    if (space > 0) {
                        if (pass == 0 && !StackUtil.isEmpty(existingStack) && StackUtil.checkItemEqualityStrict(stack, existingStack)) {
                            if (space >= amount) {
                                this.put(i, StackUtil.incSize(existingStack, amount));
                                amount = 0;
                                break Label_0289;
                            }
                            this.put(i, StackUtil.incSize(existingStack, space));
                            amount -= space;
                        }
                        else if (pass == 1 && StackUtil.isEmpty(existingStack)) {
                            if (space >= amount) {
                                this.put(i, StackUtil.copyWithSize(stack, amount));
                                amount = 0;
                                break Label_0289;
                            }
                            this.put(i, StackUtil.copyWithSize(stack, space));
                            amount -= space;
                        }
                    }
                }
            }
            totalAmount += amount;
        }
        if (simulate) {
            this.restore(backup);
        }
        return totalAmount;
    }
}
