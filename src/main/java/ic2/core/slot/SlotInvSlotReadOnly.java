package ic2.core.slot;

import ic2.core.block.invslot.InvSlot;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class SlotInvSlotReadOnly extends SlotInvSlot {
   public SlotInvSlotReadOnly(InvSlot invSlot, int index, int xDisplayPosition, int yDisplayPosition) {
      super(invSlot, index, xDisplayPosition, yDisplayPosition);
   }

   @Override
   public boolean isItemValid(ItemStack stack) {
      return false;
   }

   @Override
   public ItemStack onTake(EntityPlayer player, ItemStack stack) {
      return stack;
   }

   public boolean canTakeStack(EntityPlayer player) {
      return false;
   }

   @Override
   public ItemStack decrStackSize(int par1) {
      return StackUtil.emptyStack;
   }
}
