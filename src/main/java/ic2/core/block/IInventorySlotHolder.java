package ic2.core.block;

import ic2.core.block.invslot.InvSlot;
import net.minecraft.inventory.IInventory;

public interface IInventorySlotHolder<P extends TileEntityBlock & IInventory> {
   P getParent();

   InvSlot getInventorySlot(String var1);

   void addInventorySlot(InvSlot var1);

   int getBaseIndex(InvSlot var1);
}
