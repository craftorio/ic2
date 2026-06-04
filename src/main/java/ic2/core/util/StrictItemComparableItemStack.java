package ic2.core.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class StrictItemComparableItemStack {
  private final Item item;
  
  private final int meta;
  
  private final NBTTagCompound nbt;
  
  private final int hashCode;
  
  public StrictItemComparableItemStack(ItemStack stack, boolean copyNbt) {
    this.item = stack.getItem();
    this.meta = StackUtil.getRawMeta(stack);
    NBTTagCompound nbt = stack.func_77978_p();
    if (nbt != null)
      if (nbt.hasNoTags()) {
        nbt = null;
      } else if (copyNbt) {
        nbt = nbt.func_74737_b();
      }  
    this.nbt = nbt;
    this.hashCode = calculateHashCode();
  }
  
  public boolean equals(Object obj) {
    if (!(obj instanceof StrictItemComparableItemStack))
      return false; 
    StrictItemComparableItemStack cmp = (StrictItemComparableItemStack)obj;
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
}
