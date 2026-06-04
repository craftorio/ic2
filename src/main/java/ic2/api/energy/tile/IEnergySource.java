// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.energy.tile;

public interface IEnergySource extends IEnergyEmitter
{
    double getOfferedEnergy();
    
    void drawEnergy(final double p0);
    
    int getSourceTier();
}
