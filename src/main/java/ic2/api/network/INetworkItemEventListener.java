package ic2.api.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface INetworkItemEventListener {
  void onNetworkEvent(ItemStack paramItemStack, EntityPlayer paramEntityPlayer, int paramInt);
}
