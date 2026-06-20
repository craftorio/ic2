package ic2.core.util;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
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
				LevelChunk chunk = world.getChunk(x, z);

				for (BlockEntity te : chunk.getBlockEntities().values())
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
		world.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
		if (player instanceof ServerPlayer)
		{
			CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, pos, mainHandItem);
		}

		world.setBlock(pos, strippedBlockState, 11);
		mainHandItem.hurtAndBreak(1, player, p -> p.onEquippedItemBroken(player.getUsedItemHand()));
	}

	public interface ITileEntityResultHandler
	{
		boolean onMatch(BlockEntity var1);
	}
}
