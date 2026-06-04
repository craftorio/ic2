package ic2.core.uu;

import ic2.core.util.StackUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

class LeanItemStack {
  private final Item item;
  
  private final int meta;
  
  private final NBTTagCompound nbt;
  
  private final int size;
  
  private int hashCode;
  
  public LeanItemStack(ItemStack stack) {
    this(stack.getItem(), 
        StackUtil.getRawMeta(stack), stack
        .getTagCompound(), 
        StackUtil.getSize(stack));
  }
  
  public LeanItemStack(ItemStack stack, int size) {
    this(stack.getItem(), 
        StackUtil.getRawMeta(stack), stack
        .getTagCompound(), size);
  }
  
  public LeanItemStack(Item item, int meta, NBTTagCompound nbt, int size) {
    if (item == null)
      throw new NullPointerException("null item"); 
    this.item = item;
    this.meta = meta;
    this.nbt = nbt;
    this.size = size;
  }
  
  public Item getItem() {
    return this.item;
  }
  
  public int getMeta() {
    return this.meta;
  }
  
  public NBTTagCompound getNbt() {
    return this.nbt;
  }
  
  public int getSize() {
    return this.size;
  }
  
  public String toString() {
    return String.format("%dx%s@%d", new Object[] { Integer.valueOf(this.size), this.item.getRegistryName(), Integer.valueOf(this.meta) });
  }
  
  public boolean hasSameItem(LeanItemStack o) {
    return (this.item == o.item && (this.meta == o.meta || 
      !this.item.getHasSubtypes()) && 
      StackUtil.checkNbtEquality(this.nbt, o.nbt));
  }
  
  public LeanItemStack copy() {
    return copyWithSize(this.size);
  }
  
  public LeanItemStack copyWithSize(int newSize) {
    LeanItemStack ret = new LeanItemStack(this.item, this.meta, this.nbt, newSize);
    ret.hashCode = this.hashCode;
    return ret;
  }
  
  public ItemStack toMcStack() {
    if (this.size <= 0)
      return StackUtil.emptyStack; 
    ItemStack ret = new ItemStack(this.item, this.size, this.meta);
    ret.setTagCompound(this.nbt);
    return ret;
  }
  
  public boolean equals(Object obj) {
    if (!(obj instanceof LeanItemStack))
      return false; 
    LeanItemStack o = (LeanItemStack)obj;
    return (this.item == o.item && this.meta == o.meta && ((this.nbt == null && o.nbt == null) || (this.nbt != null && o.nbt != null && this.nbt
      .equals(o.nbt))));
  }
  
  public int hashCode() {
    if (this.hashCode == 0)
      this.hashCode = calculateHashCode(); 
    return this.hashCode;
  }
  
  private int calculateHashCode() {
    int ret = System.identityHashCode(this.item);
    ret = ret * 31 + this.meta;
    if (this.nbt != null)
      ret = ret * 61 + this.nbt.hashCode(); 
    if (ret == 0)
      ret = -1; 
    return ret;
  }
}
