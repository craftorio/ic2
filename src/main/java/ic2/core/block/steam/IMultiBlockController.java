// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.steam;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMultiBlockController
{
    World getWorld();
    
    BlockPos getPos();
    
    boolean isInvalid();
    
    boolean hasValidStructure();
    
    boolean isFormed();
}
