package ic2.core.util;

import ic2.core.block.invslot.InvSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorldNameable;

public class IInventoryInvSlot implements IInventory {
   public final InvSlot slot;

   public IInventoryInvSlot(InvSlot slot) {
      this.slot = slot;
   }

   public int getSizeInventory() {
      return this.slot.size();
   }

   public int getInventoryStackLimit() {
      return this.slot.getStackSizeLimit();
   }

   public boolean isEmpty() {
      return this.slot.isEmpty();
   }

   public boolean isItemValidForSlot(int index, ItemStack stack) {
      return this.slot.accepts(stack);
   }

   public ItemStack getStackInSlot(int index) {
      return this.slot.get(index);
   }

   public ItemStack decrStackSize(int index, int count) {
      ItemStack stack = this.getStackInSlot(index);
      if (!StackUtil.isEmpty(stack)) {
         int amount = Math.min(StackUtil.getSize(stack), count);
         ItemStack out = StackUtil.copyWithSize(stack, amount);
         this.setInventorySlotContents(index, StackUtil.decSize(stack, amount));
         return out;
      } else {
         return StackUtil.emptyStack;
      }
   }

   public void setInventorySlotContents(int index, ItemStack stack) {
      this.slot.put(index, stack);
   }

   public ItemStack removeStackFromSlot(int index) {
      ItemStack stack = this.getStackInSlot(index);
      this.setInventorySlotContents(index, StackUtil.emptyStack);
      return stack;
   }

   public void clear() {
      this.slot.clear();
   }

   public void markDirty() {
      this.slot.onChanged();
   }

   public boolean isUsableByPlayer(EntityPlayer player) {
      return true;
   }

   public void openInventory(EntityPlayer player) {
   }

   public void closeInventory(EntityPlayer player) {
   }

   public boolean hasCustomName() {
      assert this.slot.base != null;
      return ((IWorldNameable)this.slot.base.getParent()).hasCustomName();
   }

   public String getName() {
      assert this.slot.base != null;
      return ((IWorldNameable)this.slot.base.getParent()).getName();
   }

   public ITextComponent getDisplayName() {
      assert this.slot.base != null;
      return this.slot.base.getParent().getDisplayName();
   }

   public int getFieldCount() {
      assert this.slot.base != null;
      return ((IInventory)this.slot.base.getParent()).getFieldCount();
   }

   public int getField(int id) {
      assert this.slot.base != null;
      return ((IInventory)this.slot.base.getParent()).getField(id);
   }

   public void setField(int id, int value) {
      assert this.slot.base != null;
      ((IInventory)this.slot.base.getParent()).setField(id, value);
   }
}
