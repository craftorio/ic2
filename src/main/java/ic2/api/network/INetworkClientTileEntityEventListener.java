package ic2.api.network;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;

public interface INetworkClientTileEntityEventListener
{
	void onNetworkEvent(Player var1, int var2);
}
