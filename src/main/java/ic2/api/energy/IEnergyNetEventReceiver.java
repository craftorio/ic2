// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.energy;

import ic2.api.energy.tile.IEnergyTile;

public interface IEnergyNetEventReceiver
{
    void onAdd(final IEnergyTile p0);
    
    void onRemove(final IEnergyTile p0);
}
