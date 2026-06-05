package ic2.api.transport;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public interface IItemTransportTile extends IPipe {
   int putItems(ItemStack var1, EnumFacing var2, boolean var3);

   ItemStack getContents();

   void setContents(ItemStack var1);

   int getMaxStackSizeAllowed();

   int getTransferRate();
}
