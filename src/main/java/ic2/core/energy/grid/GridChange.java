// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy.grid;

import java.util.List;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraft.util.math.BlockPos;

class GridChange
{
    final Type type;
    final BlockPos pos;
    final IEnergyTile ioTile;
    List<IEnergyTile> subTiles;
    
    GridChange(final Type type, final BlockPos pos, final IEnergyTile ioTile) {
        this.type = type;
        this.pos = pos;
        this.ioTile = ioTile;
    }
    
    enum Type
    {
        ADDITION, 
        REMOVAL;
    }
}
