package ic2.core.util;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public class WorldUtil
{
	public static void findTileEntities(Level world, BlockPos center, int range, WorldUtil.ITileEntityResultHandler handler)
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
				LevelChunk chunk = world.m_6325_(x, z);

				for (BlockEntity te : chunk.m_62954_().values())
				{
					BlockPos pos = te.getBlockPos();
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

	public static void strip(BlockState state, Level world, BlockPos pos, Player player, ItemStack mainHandItem, BlockState strippedBlockState)
	{
		world.playSound(player, pos, SoundEvents.f_11688_, SoundSource.BLOCKS, 1.0F, 1.0F);
		if (player instanceof ServerPlayer)
		{
			CriteriaTriggers.f_10562_.m_220040_((ServerPlayer) player, pos, mainHandItem);
		}

		world.m_7731_(pos, strippedBlockState, 11);
		mainHandItem.m_41622_(1, player, p -> p.m_21190_(player.m_7655_()));
	}

	public interface ITileEntityResultHandler
	{
		boolean onMatch(BlockEntity var1);
	}
}
