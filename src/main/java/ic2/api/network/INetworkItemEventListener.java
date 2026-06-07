package ic2.api.network;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface INetworkItemEventListener
{
	void onNetworkEvent(ItemStack var1, Player var2, int var3);
}
