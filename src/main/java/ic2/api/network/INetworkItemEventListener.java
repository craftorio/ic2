// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface INetworkItemEventListener
{
    void onNetworkEvent(final ItemStack p0, final EntityPlayer p1, final int p2);
}
