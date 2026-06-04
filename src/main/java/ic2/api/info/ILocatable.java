// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.info;

import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;

public interface ILocatable
{
    BlockPos getPosition();
    
    World getWorldObj();
}
