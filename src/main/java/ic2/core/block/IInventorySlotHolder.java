package ic2.core.block;

import ic2.core.block.invslot.InvSlot;

public interface IInventorySlotHolder<P extends TileEntityBlock & net.minecraft.inventory.IInventory> {
  P getParent();
  
  InvSlot getInventorySlot(String paramString);
  
  void addInventorySlot(InvSlot paramInvSlot);
  
  int getBaseIndex(InvSlot paramInvSlot);
}
