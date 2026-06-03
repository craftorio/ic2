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
  
  public int func_70302_i_() {
    return this.size;
  }
  
  public ItemStack func_70463_b(int row, int column) {
    return (row >= 0 && row < this.height && column >= 0 && column < this.width) ? func_70301_a(row + column * this.height) : StackUtil.emptyStack;
  }
  
  protected abstract ItemStack get(int paramInt);
  
  protected abstract void put(int paramInt, ItemStack paramItemStack);
  
  protected void clear(int index) {
    put(index, StackUtil.emptyStack);
  }
  
  public ItemStack func_70301_a(int index) {
    return !validIndex(index) ? StackUtil.emptyStack : get(index);
  }
  
  public ItemStack func_70304_b(int index) {
    if (validIndex(index)) {
      ItemStack stack = get(index);
      clear(index);
      return stack;
    } 
    return StackUtil.emptyStack;
  }
  
  public ItemStack func_70298_a(int index, int count) {
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
  
  public void func_70299_a(int index, ItemStack stack) {
    if (validIndex(index))
      put(index, stack); 
  }
  
  public abstract void func_174888_l();
  
  public abstract boolean func_191420_l();
  
  public int func_174922_i() {
    return this.width;
  }
  
  public int func_174923_h() {
    return this.height;
  }
}
