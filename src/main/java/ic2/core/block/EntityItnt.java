// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import ic2.core.ref.TeBlock;
import ic2.core.ref.BlockName;
import net.minecraft.world.World;

public class EntityItnt extends EntityIC2Explosive
{
    public EntityItnt(final World world, final double x, final double y, final double z) {
        super(world, x, y, z, 60, 5.5f, 0.9f, 0.3f, BlockName.te.getBlockState(TeBlock.itnt), 0);
    }
    
    public EntityItnt(final World world) {
        this(world, 0.0, 0.0, 0.0);
    }
}
