// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.energy.tile;

import net.minecraft.util.EnumFacing;

public interface IHeatSource
{
    @Deprecated
    int maxrequestHeatTick(final EnumFacing p0);
    
    default int getConnectionBandwidth(final EnumFacing side) {
        return this.maxrequestHeatTick(side);
    }
    
    @Deprecated
    int requestHeat(final EnumFacing p0, final int p1);
    
    default int drawHeat(final EnumFacing side, final int request, final boolean simulate) {
        return simulate ? this.maxrequestHeatTick(side) : this.requestHeat(side, request);
    }
}
