package ic2.core.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class DelegatingInventory implements IInventory {
  private final IInventory parent;
  
  public DelegatingInventory(IInventory parent) {
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
  
  public ItemStack getStackInSlot(int index) {
    return this.parent.getStackInSlot(index);
  }
  
  public ItemStack decrStackSize(int index, int count) {
    return this.parent.decrStackSize(index, count);
  }
  
  public ItemStack removeStackFromSlot(int index) {
    return this.parent.removeStackFromSlot(index);
  }
  
  public void setInventorySlotContents(int index, ItemStack stack) {
    this.parent.setInventorySlotContents(index, stack);
  }
  
  public int getInventoryStackLimit() {
    return this.parent.getInventoryStackLimit();
  }
  
  public void markDirty() {
    this.parent.markDirty();
  }
  
  public boolean isUsableByPlayer(EntityPlayer player) {
    return this.parent.isUsableByPlayer(player);
  }
  
  public void openInventory(EntityPlayer player) {
    this.parent.openInventory(player);
  }
  
  public void closeInventory(EntityPlayer player) {
    this.parent.closeInventory(player);
  }
  
  public boolean isItemValidForSlot(int index, ItemStack stack) {
    return this.parent.isItemValidForSlot(index, stack);
  }
  
  public int getField(int id) {
    return this.parent.getField(id);
  }
  
  public void setField(int id, int value) {
    this.parent.setField(id, value);
  }
  
  public int getFieldCount() {
    return this.parent.getFieldCount();
  }
  
  public void clear() {
    this.parent.clear();
  }
}
