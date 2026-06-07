package ic2.api.network;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface INetworkManager
{
	void updateTileEntityField(BlockEntity var1, String var2);

	void initiateTileEntityEvent(BlockEntity var1, int var2, boolean var3);

	void initiateItemEvent(Player var1, ItemStack var2, int var3, boolean var4);

	void initiateClientTileEntityEvent(BlockEntity var1, int var2);

	void initiateClientItemEvent(ItemStack var1, int var2);

	void sendInitialData(BlockEntity var1);
}
