// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.transport;

import net.minecraft.util.EnumFacing;
import net.minecraft.tileentity.TileEntity;

public interface IPipe
{
    TileEntity getTile();
    
    boolean isConnected(final EnumFacing p0);
    
    void flipConnection(final EnumFacing p0);
}
