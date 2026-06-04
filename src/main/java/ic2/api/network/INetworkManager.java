// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.network;

import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public interface INetworkManager
{
    void updateTileEntityField(final TileEntity p0, final String p1);
    
    void initiateTileEntityEvent(final TileEntity p0, final int p1, final boolean p2);
    
    void initiateItemEvent(final EntityPlayer p0, final ItemStack p1, final int p2, final boolean p3);
    
    void initiateClientTileEntityEvent(final TileEntity p0, final int p1);
    
    void initiateClientItemEvent(final ItemStack p0, final int p1);
    
    void sendInitialData(final TileEntity p0);
}
