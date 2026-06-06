package ic2.api.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public interface INetworkManager
{
	void updateTileEntityField(TileEntity var1, String var2);

	void initiateTileEntityEvent(TileEntity var1, int var2, boolean var3);

	void initiateItemEvent(EntityPlayer var1, ItemStack var2, int var3, boolean var4);

	void initiateClientTileEntityEvent(TileEntity var1, int var2);

	void initiateClientItemEvent(ItemStack var1, int var2);

	void sendInitialData(TileEntity var1);
}
