package ic2.api.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public interface INetworkManager {
  void updateTileEntityField(TileEntity paramTileEntity, String paramString);
  
  void initiateTileEntityEvent(TileEntity paramTileEntity, int paramInt, boolean paramBoolean);
  
  void initiateItemEvent(EntityPlayer paramEntityPlayer, ItemStack paramItemStack, int paramInt, boolean paramBoolean);
  
  void initiateClientTileEntityEvent(TileEntity paramTileEntity, int paramInt);
  
  void initiateClientItemEvent(ItemStack paramItemStack, int paramInt);
  
  void sendInitialData(TileEntity paramTileEntity);
}
