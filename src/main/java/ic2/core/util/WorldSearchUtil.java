package ic2.core.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class WorldSearchUtil
{
	public static void findTileEntities(World world, BlockPos center, int range, WorldSearchUtil.ITileEntityResultHandler handler)
	{
		int minX = center.getX() - range;
		int minY = center.getY() - range;
		int minZ = center.getZ() - range;
		int maxX = center.getX() + range;
		int maxY = center.getY() + range;
		int maxZ = center.getZ() + range;
		int xS = minX >> 4;
		int zS = minZ >> 4;
		int xE = maxX >> 4;
		int zE = maxZ >> 4;

		for (int x = xS; x <= xE; x++)
		{
			for (int z = zS; z <= zE; z++)
			{
				Chunk chunk = world.getChunkFromChunkCoords(x, z);

				for (TileEntity te : chunk.getTileEntityMap().values())
				{
					BlockPos pos = te.getPos();
					if (pos.getY() >= minY
						&& pos.getY() <= maxY
						&& pos.getX() >= minX
						&& pos.getX() <= maxX
						&& pos.getZ() >= minZ
						&& pos.getZ() <= maxZ
						&& handler.onMatch(te))
					{
						return;
					}
				}
			}
		}
	}

	public interface ITileEntityResultHandler
	{
		boolean onMatch(TileEntity var1);
	}
}
