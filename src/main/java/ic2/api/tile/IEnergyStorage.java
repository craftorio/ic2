// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.tile;

import net.minecraft.util.EnumFacing;

public interface IEnergyStorage
{
    int getStored();
    
    void setStored(final int p0);
    
    int addEnergy(final int p0);
    
    int getCapacity();
    
    int getOutput();
    
    double getOutputEnergyUnitsPerTick();
    
    boolean isTeleporterCompatible(final EnumFacing p0);
}
