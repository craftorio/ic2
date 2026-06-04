package ic2.core.block.invslot;

import ic2.core.block.IInventorySlotHolder;
import ic2.core.util.StackUtil;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class InvSlotConsumableClass extends InvSlotConsumable {
  private final Class<?> clazz;
  
  public InvSlotConsumableClass(IInventorySlotHolder<?> base1, String name1, InvSlot.Access access1, int count, InvSlot.InvSide preferredSide1, Class<?> clazz) {
    super(base1, name1, access1, count, preferredSide1);
    this.clazz = clazz;
  }
  
  public InvSlotConsumableClass(IInventorySlotHolder<?> base1, String name1, int count, Class<?> clazz) {
    super(base1, name1, count);
    this.clazz = clazz;
  }
  
  public boolean accepts(ItemStack stack) {
    if (StackUtil.isEmpty(stack))
      return false; 
    if (stack.getItem() instanceof net.minecraft.item.ItemBlock)
      return this.clazz.isInstance(Block.func_149634_a(stack.getItem())); 
    return this.clazz.isInstance(stack.getItem());
  }
}
