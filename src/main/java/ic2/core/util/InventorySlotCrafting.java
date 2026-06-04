// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;

public abstract class InventorySlotCrafting extends InventoryCrafting
{
    protected final int width;
    protected final int height;
    protected final int size;
    
    public InventorySlotCrafting(final int width, final int height) {
        super((Container)null, 0, 0);
        this.width = width;
        this.height = height;
        this.size = width * height;
    }
    
    protected boolean validIndex(final int index) {
        return index >= 0 && index < this.size;
    }
    
    public int getSizeInventory() {
        return this.size;
    }
    
    public ItemStack getStackInRowAndColumn(final int row, final int column) {
        return (row >= 0 && row < this.height && column >= 0 && column < this.width) ? this.getStackInSlot(row + column * this.height) : StackUtil.emptyStack;
    }
    
    protected abstract ItemStack get(final int p0);
    
    protected abstract void put(final int p0, final ItemStack p1);
    
    protected void clear(final int index) {
        this.put(index, StackUtil.emptyStack);
    }
    
    public ItemStack getStackInSlot(final int index) {
        return this.validIndex(index) ? this.get(index) : StackUtil.emptyStack;
    }
    
    public ItemStack removeStackFromSlot(final int index) {
        if (this.validIndex(index)) {
            final ItemStack stack = this.get(index);
            this.clear(index);
            return stack;
        }
        return StackUtil.emptyStack;
    }
    
    public ItemStack decrStackSize(final int index, final int count) {
        final ItemStack stack;
        if (this.validIndex(index) && !StackUtil.isEmpty(stack = this.get(index))) {
            ItemStack ret;
            if (count >= StackUtil.getSize(stack)) {
                ret = stack;
                this.clear(index);
            }
            else {
                ret = StackUtil.copyWithSize(stack, count);
                this.put(index, StackUtil.decSize(stack, count));
            }
            return ret;
        }
        return StackUtil.emptyStack;
    }
    
    public void setInventorySlotContents(final int index, final ItemStack stack) {
        if (this.validIndex(index)) {
            this.put(index, stack);
        }
    }
    
    public abstract void clear();
    
    public abstract boolean isEmpty();
    
    public int getWidth() {
        return this.width;
    }
    
    public int getHeight() {
        return this.height;
    }
}
