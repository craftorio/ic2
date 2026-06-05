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
      return index >= 0 && index < this.size;
   }

   public int getSizeInventory() {
      return this.size;
   }

   public ItemStack getStackInRowAndColumn(int row, int column) {
      return row >= 0 && row < this.height && column >= 0 && column < this.width ? this.getStackInSlot(row + column * this.height) : StackUtil.emptyStack;
   }

   protected abstract ItemStack get(int var1);

   protected abstract void put(int var1, ItemStack var2);

   protected void clear(int index) {
      this.put(index, StackUtil.emptyStack);
   }

   public ItemStack getStackInSlot(int index) {
      return !this.validIndex(index) ? StackUtil.emptyStack : this.get(index);
   }

   public ItemStack removeStackFromSlot(int index) {
      if (this.validIndex(index)) {
         ItemStack stack = this.get(index);
         this.clear(index);
         return stack;
      } else {
         return StackUtil.emptyStack;
      }
   }

   public ItemStack decrStackSize(int index, int count) {
      ItemStack stack;
      if (this.validIndex(index) && !StackUtil.isEmpty(stack = this.get(index))) {
         ItemStack ret;
         if (count >= StackUtil.getSize(stack)) {
            ret = stack;
            this.clear(index);
         } else {
            ret = StackUtil.copyWithSize(stack, count);
            this.put(index, StackUtil.decSize(stack, count));
         }

         return ret;
      } else {
         return StackUtil.emptyStack;
      }
   }

   public void setInventorySlotContents(int index, ItemStack stack) {
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
