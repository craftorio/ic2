// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.energy.tile;

import net.minecraft.util.EnumFacing;

public interface IEnergySink extends IEnergyAcceptor
{
    double getDemandedEnergy();
    
    int getSinkTier();
    
    double injectEnergy(final EnumFacing p0, final double p1, final double p2);
}
