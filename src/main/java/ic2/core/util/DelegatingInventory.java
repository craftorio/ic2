// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.inventory.IInventory;

public class DelegatingInventory implements IInventory
{
    private final IInventory parent;
    
    public DelegatingInventory(final IInventory parent) {
        this.parent = parent;
    }
    
    public String getName() {
        return this.parent.getName();
    }
    
    public boolean hasCustomName() {
        return this.parent.hasCustomName();
    }
    
    public ITextComponent getDisplayName() {
        return this.parent.getDisplayName();
    }
    
    public int getSizeInventory() {
        return this.parent.getSizeInventory();
    }
    
    public boolean isEmpty() {
        return this.parent.isEmpty();
    }
    
    public ItemStack getStackInSlot(final int index) {
        return this.parent.getStackInSlot(index);
    }
    
    public ItemStack decrStackSize(final int index, final int count) {
        return this.parent.decrStackSize(index, count);
    }
    
    public ItemStack removeStackFromSlot(final int index) {
        return this.parent.removeStackFromSlot(index);
    }
    
    public void setInventorySlotContents(final int index, final ItemStack stack) {
        this.parent.setInventorySlotContents(index, stack);
    }
    
    public int getInventoryStackLimit() {
        return this.parent.getInventoryStackLimit();
    }
    
    public void markDirty() {
        this.parent.markDirty();
    }
    
    public boolean isUsableByPlayer(final EntityPlayer player) {
        return this.parent.isUsableByPlayer(player);
    }
    
    public void openInventory(final EntityPlayer player) {
        this.parent.openInventory(player);
    }
    
    public void closeInventory(final EntityPlayer player) {
        this.parent.closeInventory(player);
    }
    
    public boolean isItemValidForSlot(final int index, final ItemStack stack) {
        return this.parent.isItemValidForSlot(index, stack);
    }
    
    public int getField(final int id) {
        return this.parent.getField(id);
    }
    
    public void setField(final int id, final int value) {
        this.parent.setField(id, value);
    }
    
    public int getFieldCount() {
        return this.parent.getFieldCount();
    }
    
    public void clear() {
        this.parent.clear();
    }
}
