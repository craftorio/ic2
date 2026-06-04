// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.comp;

import ic2.core.block.TileEntityBlock;
import net.minecraft.util.EnumFacing;
import java.util.Set;

public class Kinetic extends TileEntityComponent
{
    private Set<EnumFacing> sinkDirections;
    private Set<EnumFacing> sourceDirections;
    
    public Kinetic(final TileEntityBlock parent, final double capacity, final Set<EnumFacing> sinkDirections, final Set<EnumFacing> sourceDirections, final int sinkTier, final int sourceTier, final boolean fullEnergy) {
        super(parent);
    }
}
