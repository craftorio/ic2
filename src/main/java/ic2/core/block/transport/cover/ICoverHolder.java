// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.transport.cover;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.Set;

public interface ICoverHolder
{
    Set<CoverProperty> getCoverProperties();
    
    boolean canPlaceCover(final World p0, final BlockPos p1, final EnumFacing p2, final ItemStack p3);
    
    void placeCover(final World p0, final BlockPos p1, final EnumFacing p2, final ItemStack p3);
    
    boolean canRemoveCover(final World p0, final BlockPos p1, final EnumFacing p2);
    
    void removeCover(final World p0, final BlockPos p1, final EnumFacing p2);
}
