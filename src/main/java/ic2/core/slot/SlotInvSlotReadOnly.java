package ic2.core.slot;

import ic2.core.block.invslot.InvSlot;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class SlotInvSlotReadOnly extends SlotInvSlot {
  public SlotInvSlotReadOnly(InvSlot invSlot, int index, int xDisplayPosition, int yDisplayPosition) {
    super(invSlot, index, xDisplayPosition, yDisplayPosition);
  }
  
  public boolean func_75214_a(ItemStack stack) {
    return false;
  }
  
  public ItemStack func_190901_a(EntityPlayer player, ItemStack stack) {
    return stack;
  }
  
  public boolean func_82869_a(EntityPlayer player) {
    return false;
  }
  
  public ItemStack func_75209_a(int par1) {
    return StackUtil.emptyStack;
  }
}
