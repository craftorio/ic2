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
  
  public String func_70005_c_() {
    return this.parent.func_70005_c_();
  }
  
  public boolean func_145818_k_() {
    return this.parent.func_145818_k_();
  }
  
  public ITextComponent func_145748_c_() {
    return this.parent.func_145748_c_();
  }
  
  public int func_70302_i_() {
    return this.parent.func_70302_i_();
  }
  
  public boolean func_191420_l() {
    return this.parent.func_191420_l();
  }
  
  public ItemStack func_70301_a(int index) {
    return this.parent.func_70301_a(index);
  }
  
  public ItemStack func_70298_a(int index, int count) {
    return this.parent.func_70298_a(index, count);
  }
  
  public ItemStack func_70304_b(int index) {
    return this.parent.func_70304_b(index);
  }
  
  public void func_70299_a(int index, ItemStack stack) {
    this.parent.func_70299_a(index, stack);
  }
  
  public int func_70297_j_() {
    return this.parent.func_70297_j_();
  }
  
  public void func_70296_d() {
    this.parent.func_70296_d();
  }
  
  public boolean func_70300_a(EntityPlayer player) {
    return this.parent.func_70300_a(player);
  }
  
  public void func_174889_b(EntityPlayer player) {
    this.parent.func_174889_b(player);
  }
  
  public void func_174886_c(EntityPlayer player) {
    this.parent.func_174886_c(player);
  }
  
  public boolean func_94041_b(int index, ItemStack stack) {
    return this.parent.func_94041_b(index, stack);
  }
  
  public int func_174887_a_(int id) {
    return this.parent.func_174887_a_(id);
  }
  
  public void func_174885_b(int id, int value) {
    this.parent.func_174885_b(id, value);
  }
  
  public int func_174890_g() {
    return this.parent.func_174890_g();
  }
  
  public void func_174888_l() {
    this.parent.func_174888_l();
  }
}
