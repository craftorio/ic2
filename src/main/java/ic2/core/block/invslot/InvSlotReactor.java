package ic2.core.block.invslot;

import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import net.minecraft.item.ItemStack;

public class InvSlotReactor extends InvSlot {
  private final int rows = 6;
  
  private final int maxCols = 9;
  
  public InvSlotReactor(TileEntityNuclearReactorElectric base1, String name1, int count) {
    super((IInventorySlotHolder<?>)base1, name1, InvSlot.Access.IO, count);
    this.rows = 6;
    this.maxCols = 9;
    setStackSizeLimit(1);
  }
  
  public boolean accepts(ItemStack stack) {
    return ((TileEntityNuclearReactorElectric)this.base).isUsefulItem(stack, true);
  }
  
  public int size() {
    return ((TileEntityNuclearReactorElectric)this.base).getReactorSize() * 6;
  }
  
  public int rawSize() {
    return super.size();
  }
  
  public ItemStack get(int index) {
    return super.get(mapIndex(index));
  }
  
  public ItemStack get(int x, int y) {
    return super.get(y * 9 + x);
  }
  
  protected void putFromNBT(int index, ItemStack content) {
    super.putFromNBT(mapIndex(index), content);
  }
  
  public void put(int index, ItemStack content) {
    super.put(mapIndex(index), content);
  }
  
  public void put(int x, int y, ItemStack content) {
    super.put(y * 9 + x, content);
  }
  
  private int mapIndex(int index) {
    int size = size();
    int cols = size / 6;
    if (index < size) {
      int i = index / cols;
      int j = index % cols;
      return i * 9 + j;
    } 
    index -= size;
    int remCols = 9 - cols;
    int row = index / remCols;
    int col = cols + index % remCols;
    return row * 9 + col;
  }
}
