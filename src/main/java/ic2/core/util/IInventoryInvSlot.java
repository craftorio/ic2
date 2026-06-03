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
  
  public int func_70302_i_() {
    return this.slot.size();
  }
  
  public int func_70297_j_() {
    return this.slot.getStackSizeLimit();
  }
  
  public boolean func_191420_l() {
    return this.slot.isEmpty();
  }
  
  public boolean func_94041_b(int index, ItemStack stack) {
    return this.slot.accepts(stack);
  }
  
  public ItemStack func_70301_a(int index) {
    return this.slot.get(index);
  }
  
  public ItemStack func_70298_a(int index, int count) {
    ItemStack stack = func_70301_a(index);
    if (!StackUtil.isEmpty(stack)) {
      int amount = Math.min(StackUtil.getSize(stack), count);
      ItemStack out = StackUtil.copyWithSize(stack, amount);
      func_70299_a(index, StackUtil.decSize(stack, amount));
      return out;
    } 
    return StackUtil.emptyStack;
  }
  
  public void func_70299_a(int index, ItemStack stack) {
    this.slot.put(index, stack);
  }
  
  public ItemStack func_70304_b(int index) {
    ItemStack stack = func_70301_a(index);
    func_70299_a(index, StackUtil.emptyStack);
    return stack;
  }
  
  public void func_174888_l() {
    this.slot.clear();
  }
  
  public void func_70296_d() {
    this.slot.onChanged();
  }
  
  public boolean func_70300_a(EntityPlayer player) {
    return true;
  }
  
  public void func_174889_b(EntityPlayer player) {}
  
  public void func_174886_c(EntityPlayer player) {}
  
  public boolean func_145818_k_() {
    assert this.slot.base != null;
    return ((IWorldNameable)this.slot.base.getParent()).func_145818_k_();
  }
  
  public String func_70005_c_() {
    assert this.slot.base != null;
    return ((IWorldNameable)this.slot.base.getParent()).func_70005_c_();
  }
  
  public ITextComponent func_145748_c_() {
    assert this.slot.base != null;
    return this.slot.base.getParent().func_145748_c_();
  }
  
  public int func_174890_g() {
    assert this.slot.base != null;
    return ((IInventory)this.slot.base.getParent()).func_174890_g();
  }
  
  public int func_174887_a_(int id) {
    assert this.slot.base != null;
    return ((IInventory)this.slot.base.getParent()).func_174887_a_(id);
  }
  
  public void func_174885_b(int id, int value) {
    assert this.slot.base != null;
    ((IInventory)this.slot.base.getParent()).func_174885_b(id, value);
  }
}
