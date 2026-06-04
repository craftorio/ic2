// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.energy.tile;

import net.minecraft.util.EnumFacing;

public interface IKineticSource
{
    @Deprecated
    int maxrequestkineticenergyTick(final EnumFacing p0);
    
    default int getConnectionBandwidth(final EnumFacing side) {
        return this.maxrequestkineticenergyTick(side);
    }
    
    @Deprecated
    int requestkineticenergy(final EnumFacing p0, final int p1);
    
    default int drawKineticEnergy(final EnumFacing side, final int request, final boolean simulate) {
        return simulate ? this.maxrequestkineticenergyTick(side) : this.requestkineticenergy(side, request);
    }
}
