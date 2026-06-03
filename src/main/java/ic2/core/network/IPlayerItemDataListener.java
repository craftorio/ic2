package ic2.core.network;

import net.minecraft.entity.player.EntityPlayer;

public interface IPlayerItemDataListener {
  void onPlayerItemNetworkData(EntityPlayer paramEntityPlayer, int paramInt, Object... paramVarArgs);
}
