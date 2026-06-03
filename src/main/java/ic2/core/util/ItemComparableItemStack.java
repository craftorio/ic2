package ic2.core.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemComparableItemStack {
  private final Item item;
  
  private final int meta;
  
  private final NBTTagCompound nbt;
  
  private final int hashCode;
  
  public ItemComparableItemStack(ItemStack stack, boolean copyNbt) {
    this.item = stack.func_77973_b();
    this.meta = stack.func_77981_g() ? stack.func_77960_j() : 0;
    NBTTagCompound nbt = stack.func_77978_p();
    if (nbt != null)
      if (nbt.func_82582_d()) {
        nbt = null;
      } else {
        if (copyNbt)
          nbt = nbt.func_74737_b(); 
        boolean copied = copyNbt;
        for (String key : StackUtil.ignoredNbtKeys) {
          if (!copied && nbt.func_74764_b(key)) {
            nbt = nbt.func_74737_b();
            copied = true;
          } 
          nbt.func_82580_o(key);
        } 
        if (nbt.func_82582_d())
          nbt = null; 
      }  
    this.nbt = nbt;
    this.hashCode = calculateHashCode();
  }
  
  private ItemComparableItemStack(ItemComparableItemStack src) {
    this.item = src.item;
    this.meta = src.meta;
    this.nbt = (src.nbt != null) ? src.nbt.func_74737_b() : null;
    this.hashCode = src.hashCode;
  }
  
  public boolean equals(Object obj) {
    if (!(obj instanceof ItemComparableItemStack))
      return false; 
    ItemComparableItemStack cmp = (ItemComparableItemStack)obj;
    if (cmp.hashCode != this.hashCode)
      return false; 
    if (cmp == this)
      return true; 
    return (cmp.item == this.item && cmp.meta == this.meta && ((cmp.nbt == null && this.nbt == null) || (cmp.nbt != null && this.nbt != null && cmp.nbt
      
      .equals(this.nbt))));
  }
  
  public int hashCode() {
    return this.hashCode;
  }
  
  private int calculateHashCode() {
    int ret = 0;
    if (this.item != null)
      ret = System.identityHashCode(this.item); 
    ret = ret * 31 + this.meta;
    if (this.nbt != null)
      ret = ret * 61 + this.nbt.hashCode(); 
    return ret;
  }
  
  public ItemComparableItemStack copy() {
    if (this.nbt == null)
      return this; 
    return new ItemComparableItemStack(this);
  }
  
  public ItemStack toStack() {
    return toStack(1);
  }
  
  public ItemStack toStack(int size) {
    if (this.item == null)
      return null; 
    ItemStack ret = new ItemStack(this.item, size, this.meta);
    ret.func_77982_d(this.nbt);
    return ret;
  }
}
