package ic2.core.slot;

import ic2.core.block.invslot.InvSlot;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotInvSlot extends Slot {
  public final InvSlot invSlot;
  
  public final int index;
  
  public SlotInvSlot(InvSlot invSlot, int index, int x, int y) {
    super((IInventory)invSlot.base.getParent(), invSlot.base.getBaseIndex(invSlot) + index, x, y);
    this.invSlot = invSlot;
    this.index = index;
  }
  
  public boolean func_75214_a(ItemStack stack) {
    return this.invSlot.accepts(stack);
  }
  
  public ItemStack func_75211_c() {
    return this.invSlot.get(this.index);
  }
  
  public void func_75215_d(ItemStack stack) {
    this.invSlot.put(this.index, stack);
    func_75218_e();
  }
  
  public ItemStack func_75209_a(int amount) {
    ItemStack ret;
    if (amount <= 0)
      return StackUtil.emptyStack; 
    ItemStack stack = this.invSlot.get(this.index);
    if (StackUtil.isEmpty(stack))
      return StackUtil.emptyStack; 
    amount = Math.min(amount, StackUtil.getSize(stack));
    if (StackUtil.getSize(stack) == amount) {
      ret = stack;
      this.invSlot.clear(this.index);
    } else {
      ret = StackUtil.copyWithSize(stack, amount);
      this.invSlot.put(this.index, StackUtil.decSize(stack, amount));
    } 
    func_75218_e();
    return ret;
  }
  
  public boolean func_75217_a(IInventory inventory, int index) {
    if (inventory != this.invSlot.base)
      return false; 
    int baseIndex = this.invSlot.base.getBaseIndex(this.invSlot);
    if (baseIndex == -1)
      return false; 
    return (baseIndex + this.index == index);
  }
  
  public int func_75219_a() {
    return this.invSlot.getStackSizeLimit();
  }
  
  public ItemStack func_190901_a(EntityPlayer player, ItemStack stack) {
    stack = super.func_190901_a(player, stack);
    this.invSlot.onPickupFromSlot(player, stack);
    return stack;
  }
}
