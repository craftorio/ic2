package ic2.core.util;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

public abstract class InventorySlotCrafting extends InventoryCrafting {
  protected final int width;
  
  protected final int height;
  
  protected final int size;
  
  public InventorySlotCrafting(int width, int height) {
    super(null, 0, 0);
    this.width = width;
    this.height = height;
    this.size = width * height;
  }
  
  protected boolean validIndex(int index) {
    return (index >= 0 && index < this.size);
  }
  
  public int getSizeInventory() {
    return this.size;
  }
  
  public ItemStack getStackInRowAndColumn(int row, int column) {
    return (row >= 0 && row < this.height && column >= 0 && column < this.width) ? getStackInSlot(row + column * this.height) : StackUtil.emptyStack;
  }
  
  protected abstract ItemStack get(int paramInt);
  
  protected abstract void put(int paramInt, ItemStack paramItemStack);
  
  protected void clear(int index) {
    put(index, StackUtil.emptyStack);
  }
  
  public ItemStack getStackInSlot(int index) {
    return !validIndex(index) ? StackUtil.emptyStack : get(index);
  }
  
  public ItemStack removeStackFromSlot(int index) {
    if (validIndex(index)) {
      ItemStack stack = get(index);
      clear(index);
      return stack;
    } 
    return StackUtil.emptyStack;
  }
  
  public ItemStack decrStackSize(int index, int count) {
    ItemStack stack;
    if (validIndex(index) && !StackUtil.isEmpty(stack = get(index))) {
      ItemStack ret;
      if (count >= StackUtil.getSize(stack)) {
        ret = stack;
        clear(index);
      } else {
        ret = StackUtil.copyWithSize(stack, count);
        put(index, StackUtil.decSize(stack, count));
      } 
      return ret;
    } 
    return StackUtil.emptyStack;
  }
  
  public void setInventorySlotContents(int index, ItemStack stack) {
    if (validIndex(index))
      put(index, stack); 
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
