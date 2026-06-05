package ic2.api.transport;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public interface IPipe {
   TileEntity getTile();

   boolean isConnected(EnumFacing var1);

   void flipConnection(EnumFacing var1);
}
