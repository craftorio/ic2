package ic2.api.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface INetworkItemEventListener
{
	void onNetworkEvent(ItemStack var1, EntityPlayer var2, int var3);
}
