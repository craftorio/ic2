// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.energy.tile;

import net.minecraft.util.EnumFacing;

public interface IEnergyEmitter extends IEnergyTile
{
    boolean emitsEnergyTo(final IEnergyAcceptor p0, final EnumFacing p1);
}
