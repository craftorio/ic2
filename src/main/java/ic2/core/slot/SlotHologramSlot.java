// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.slot;

import net.minecraft.inventory.ClickType;
import ic2.core.util.Util;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Slot;

public class SlotHologramSlot extends Slot
{
    protected final ItemStack[] stacks;
    protected final int index;
    protected final int stackSizeLimit;
    protected final ChangeCallback changeCallback;
    
    public SlotHologramSlot(final ItemStack[] stacks, final int index, final int x, final int y, final int stackSizeLimit, final ChangeCallback changeCallback) {
        super((IInventory)null, 0, x, y);
        if (index >= stacks.length) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        this.stacks = stacks;
        this.index = index;
        this.stackSizeLimit = stackSizeLimit;
        this.changeCallback = changeCallback;
    }
    
    public boolean canTakeStack(final EntityPlayer player) {
        return false;
    }
    
    public int getSlotStackLimit() {
        return this.stackSizeLimit;
    }
    
    public boolean isItemValid(final ItemStack stack) {
        return false;
    }
    
    public ItemStack getStack() {
        return StackUtil.wrapEmpty(this.stacks[this.index]);
    }
    
    public void putStack(final ItemStack stack) {
        this.stacks[this.index] = stack;
    }
    
    public void onSlotChanged() {
        if (Util.inDev()) {
            System.out.println(StackUtil.toStringSafe(this.stacks));
        }
        if (this.changeCallback != null) {
            this.changeCallback.onChanged(this.index);
        }
    }
    
    public ItemStack decrStackSize(final int amount) {
        return StackUtil.emptyStack;
    }
    
    public boolean isHere(final IInventory inventory, final int index) {
        return false;
    }
    
    public ItemStack slotClick(final int dragType, final ClickType clickType, final EntityPlayer player) {
        if (Util.inDev() && player.getEntityWorld().isRemote) {
            System.out.printf("dragType=%d clickType=%s stack=%s%n", dragType, clickType, player.inventory.getItemStack());
        }
        if (clickType == ClickType.PICKUP && (dragType == 0 || dragType == 1)) {
            final ItemStack playerStack = player.inventory.getItemStack();
            final ItemStack slotStack = this.stacks[this.index];
            if (!StackUtil.isEmpty(playerStack)) {
                final int curSize = StackUtil.getSize(slotStack);
                int extraSize = (dragType == 0) ? StackUtil.getSize(playerStack) : 1;
                final int limit = Math.min(playerStack.getMaxStackSize(), this.stackSizeLimit);
                if (curSize + extraSize > limit) {
                    extraSize = Math.max(0, limit - curSize);
                }
                if (curSize == 0) {
                    this.stacks[this.index] = StackUtil.copyWithSize(playerStack, extraSize);
                }
                else if (StackUtil.checkItemEquality(playerStack, slotStack)) {
                    if (Util.inDev()) {
                        System.out.println("add " + extraSize + " to " + slotStack + " -> " + (curSize + extraSize));
                    }
                    this.stacks[this.index] = StackUtil.incSize(slotStack, extraSize);
                }
                else {
                    this.stacks[this.index] = StackUtil.copyWithSize(playerStack, Math.min(StackUtil.getSize(playerStack), limit));
                }
            }
            else if (!StackUtil.isEmpty(slotStack)) {
                if (dragType == 0) {
                    this.stacks[this.index] = StackUtil.emptyStack;
                }
                else {
                    final int newSize = StackUtil.getSize(slotStack) / 2;
                    if (newSize <= 0) {
                        this.stacks[this.index] = StackUtil.emptyStack;
                    }
                    else {
                        this.stacks[this.index] = StackUtil.setSize(slotStack, newSize);
                    }
                }
            }
            this.onSlotChanged();
        }
        return StackUtil.emptyStack;
    }
    
    public interface ChangeCallback
    {
        void onChanged(final int p0);
    }
}
