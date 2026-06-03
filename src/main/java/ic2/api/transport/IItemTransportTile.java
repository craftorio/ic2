package ic2.api.transport;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface IItemTransportTile extends IPipe {
  int putItems(ItemStack paramItemStack, EnumFacing paramEnumFacing, boolean paramBoolean);
  
  ItemStack getContents();
  
  void setContents(ItemStack paramItemStack);
  
  int getMaxStackSizeAllowed();
  
  int getTransferRate();
}
