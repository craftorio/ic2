package ic2.core.network;

import net.minecraft.world.entity.player.Player;

public interface IPlayerItemDataListener
{
	void onPlayerItemNetworkData(Player var1, int var2, Object... var3);
}
