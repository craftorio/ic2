package ic2.core.network;

import net.minecraft.entity.player.EntityPlayer;

public interface IPlayerItemDataListener {
   void onPlayerItemNetworkData(EntityPlayer var1, int var2, Object... var3);
}
