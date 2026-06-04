// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import java.util.Iterator;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldSearchUtil
{
    public static void findTileEntities(final World world, final BlockPos center, final int range, final ITileEntityResultHandler handler) {
        final int minX = center.getX() - range;
        final int minY = center.getY() - range;
        final int minZ = center.getZ() - range;
        final int maxX = center.getX() + range;
        final int maxY = center.getY() + range;
        final int maxZ = center.getZ() + range;
        final int xS = minX >> 4;
        final int zS = minZ >> 4;
        final int xE = maxX >> 4;
        final int zE = maxZ >> 4;
        for (int x = xS; x <= xE; ++x) {
            for (int z = zS; z <= zE; ++z) {
                final Chunk chunk = world.getChunkFromChunkCoords(x, z);
                for (final TileEntity te : chunk.getTileEntityMap().values()) {
                    final BlockPos pos = te.getPos();
                    if (pos.getY() >= minY && pos.getY() <= maxY && pos.getX() >= minX && pos.getX() <= maxX && pos.getZ() >= minZ && pos.getZ() <= maxZ && handler.onMatch(te)) {
                        return;
                    }
                }
            }
        }
    }
    
    public interface ITileEntityResultHandler
    {
        boolean onMatch(final TileEntity p0);
    }
}
