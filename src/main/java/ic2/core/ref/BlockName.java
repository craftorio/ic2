package ic2.core.ref;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum BlockName {
  te, resource, leaves, rubber_wood, sapling, scaffold, foam, fence, sheet, glass, wall, mining_pipe, reinforced_door, dynamite, refractory_bricks;
  
  private Block instance;
  
  public static final BlockName[] values;
  
  public boolean hasInstance() {
    return (this.instance != null);
  }
  
  public <T extends Block & IBlockModelProvider> T getInstance() {
    if (this.instance == null)
      throw new IllegalStateException("the requested block instance for " + name() + " isn't set (yet)"); 
    return (T)this.instance;
  }
  
  public <T extends Block & IBlockModelProvider> void setInstance(T instance) {
    if (this.instance != null)
      throw new IllegalStateException("conflicting instance"); 
    this.instance = (Block)instance;
  }
  
  public <T extends ic2.core.block.state.IIdProvider> IBlockState getBlockState(T variant) {
    if (this.instance == null)
      return null; 
    if (this.instance instanceof IMultiBlock) {
      IMultiBlock<T> block = (IMultiBlock<T>)this.instance;
      return block.getState(variant);
    } 
    if (variant == null)
      return this.instance.func_176223_P(); 
    throw new IllegalArgumentException("not applicable");
  }
  
  public boolean hasItemStack() {
    if (this.instance == null)
      return false; 
    if (this.instance instanceof IMultiItem)
      return true; 
    Item item = Item.func_150898_a(this.instance);
    return (item != null && item != Items.field_190931_a);
  }
  
  public <T extends Enum<T> & ic2.core.block.state.IIdProvider> ItemStack getItemStack() {
    return getItemStack((String)null);
  }
  
  public <T extends Enum<T> & ic2.core.block.state.IIdProvider> ItemStack getItemStack(T variant) {
    if (this.instance == null)
      return null; 
    if (this.instance instanceof IMultiItem) {
      IMultiItem<T> multiItem = (IMultiItem<T>)this.instance;
      return multiItem.getItemStack(variant);
    } 
    if (variant == null)
      return getItemStack((String)null); 
    throw new IllegalArgumentException("not applicable");
  }
  
  public <T extends Enum<T> & ic2.core.block.state.IIdProvider> ItemStack getItemStack(String variant) {
    if (this.instance == null)
      return null; 
    if (this.instance instanceof IMultiItem) {
      IMultiItem<T> multiItem = (IMultiItem<T>)this.instance;
      return multiItem.getItemStack(variant);
    } 
    if (variant == null) {
      Item item = Item.func_150898_a(this.instance);
      if (item == null || item == Items.field_190931_a)
        throw new IllegalArgumentException("No item found for " + this.instance); 
      return new ItemStack(item);
    } 
    throw new IllegalArgumentException("not applicable");
  }
  
  public String getVariant(ItemStack stack) {
    if (this.instance == null)
      return null; 
    if (this.instance instanceof IMultiItem)
      return ((IMultiItem)this.instance).getVariant(stack); 
    return null;
  }
  
  static {
    values = values();
  }
}
