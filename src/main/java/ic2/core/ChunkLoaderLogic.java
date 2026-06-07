package ic2.core;

import ic2.core.event.WorldData;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Comparator;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;

public final class ChunkLoaderLogic
{
	private static final String savedStateId = IC2.getIdentifier("loaded_chunks").toString().replace(':', '_');
	private static final TicketType<ChunkPos> ticketType = TicketType.m_9462_(
		IC2.getIdentifier("chunk_loader").toString(), Comparator.comparingLong(ChunkPos::m_45588_)
	);

	public static void addChunkLoader(ServerLevel world, BlockPos pos, LongSet chunks)
	{
		long loaderChunk = ChunkPos.m_151388_(pos);
		if (!chunks.contains(loaderChunk))
		{
			throw new IllegalArgumentException("missing own position");
		}

		ChunkLoaderLogic.SavedState state = (ChunkLoaderLogic.SavedState) world.m_8895_()
			.m_164861_(ChunkLoaderLogic.SavedState::new, ChunkLoaderLogic.SavedState::new, savedStateId);
		((Set) state.chunksToChunkLoaders.computeIfAbsent(loaderChunk, ignore -> new ObjectOpenHashSet(1))).add(pos);
		WorldData worldData = WorldData.get(world);
		LongIterator var7 = chunks.iterator();

		while (var7.hasNext())
		{
			long chunk = (Long) var7.next();
			Set<BlockPos> loaders = (Set<BlockPos>) worldData.loadedChunks.computeIfAbsent(chunk, ignore -> new ObjectOpenHashSet(1));
			if (loaders.isEmpty())
			{
				addChunkTicket(world, new ChunkPos(chunk));
			}

			loaders.add(pos);
		}

		worldData.chunkLoaders.put(pos, new LongOpenHashSet(chunks));
	}

	public static void removeChunkLoader(ServerLevel world, BlockPos pos)
	{
		ChunkLoaderLogic.SavedState state = (ChunkLoaderLogic.SavedState) world.m_8895_().m_164858_(ChunkLoaderLogic.SavedState::new, savedStateId);
		if (state != null)
		{
			long chunkPos = ChunkPos.m_151388_(pos);
			Set<BlockPos> positions = (Set<BlockPos>) state.chunksToChunkLoaders.get(chunkPos);
			if (positions != null && positions.remove(pos) && positions.isEmpty())
			{
				state.chunksToChunkLoaders.remove(chunkPos);
			}
		}

		WorldData worldData = WorldData.get(world, false);
		if (worldData != null)
		{
			disableChunkLoader(world, pos, worldData);
		}
	}

	private static void disableChunkLoader(ServerLevel world, BlockPos pos, WorldData worldData)
	{
		LongSet positions = (LongSet) worldData.chunkLoaders.remove(pos);
		if (positions != null)
		{
			LongIterator var4 = positions.iterator();

			while (var4.hasNext())
			{
				long chunkPos = (Long) var4.next();
				Set<BlockPos> loaders = (Set<BlockPos>) worldData.loadedChunks.get(chunkPos);
				if (loaders != null && loaders.remove(pos) && loaders.isEmpty())
				{
					worldData.loadedChunks.remove(chunkPos);
					removeChunkTicket(world, new ChunkPos(chunkPos));
				}
			}
		}
	}

	public static void updateChunkLoader(ServerLevel world, BlockPos pos, LongSet chunks)
	{
		long loaderChunk = ChunkPos.m_151388_(pos);
		if (!chunks.contains(loaderChunk))
		{
			throw new IllegalArgumentException("missing own position");
		}

		WorldData worldData = WorldData.get(world);
		LongSet prev = (LongSet) worldData.chunkLoaders.get(pos);
		if (prev == null)
		{
			addChunkLoader(world, pos, chunks);
		} else
		{
			LongIterator it = prev.longIterator();

			while (it.hasNext())
			{
				long chunk = it.nextLong();
				if (!chunks.contains(chunk))
				{
					it.remove();
					Set<BlockPos> loaders = (Set<BlockPos>) worldData.loadedChunks.get(chunk);
					loaders.remove(pos);
					if (loaders.isEmpty())
					{
						removeChunkTicket(world, new ChunkPos(chunk));
						worldData.loadedChunks.remove(chunk);
					}
				}
			}

			it = chunks.iterator();

			while (it.hasNext())
			{
				long chunk = (Long) it.next();
				if (prev.add(chunk))
				{
					Set<BlockPos> loaders = (Set<BlockPos>) worldData.loadedChunks.computeIfAbsent(chunk, ignore -> new ObjectOpenHashSet());
					if (loaders.isEmpty())
					{
						addChunkTicket(world, new ChunkPos(chunk));
					}

					loaders.add(pos);
				}
			}
		}
	}

	public static void onWorldLoad(ServerLevel world)
	{
		ChunkLoaderLogic.SavedState state = (ChunkLoaderLogic.SavedState) world.m_8895_().m_164858_(ChunkLoaderLogic.SavedState::new, savedStateId);
		if (state != null && !state.chunksToChunkLoaders.isEmpty())
		{
			WorldData worldData = WorldData.get(world);
			ObjectIterator var3 = state.chunksToChunkLoaders.long2ObjectEntrySet().iterator();

			while (var3.hasNext())
			{
				Entry<Set<BlockPos>> entry = (Entry<Set<BlockPos>>) var3.next();
				long chunkPos = entry.getLongKey();
				Set<BlockPos> loaders = (Set<BlockPos>) entry.getValue();
				worldData.loadedChunks.put(chunkPos, new ObjectOpenHashSet(loaders));

				for (BlockPos pos : loaders)
				{
					((LongSet) worldData.chunkLoaders.computeIfAbsent(pos, ignore -> new LongOpenHashSet(1))).add(chunkPos);
				}

				addChunkTicket(world, new ChunkPos(chunkPos));
			}
		}
	}

	public static void onChunkUnload(LevelChunk chunk)
	{
		assert !chunk.m_62953_().isClientSide;
		ServerLevel world = (ServerLevel) chunk.m_62953_();
		ChunkLoaderLogic.SavedState state = (ChunkLoaderLogic.SavedState) world.m_8895_().m_164858_(ChunkLoaderLogic.SavedState::new, savedStateId);
		if (state != null && !state.chunksToChunkLoaders.isEmpty())
		{
			Set<BlockPos> loaders = (Set<BlockPos>) state.chunksToChunkLoaders.get(chunk.m_7697_().m_45588_());
			if (loaders != null && !loaders.isEmpty())
			{
				WorldData worldData = WorldData.get(world, false);
				if (worldData != null)
				{
					for (BlockPos pos : loaders)
					{
						disableChunkLoader(world, pos, worldData);
					}
				}
			}
		}
	}

	private static void addChunkTicket(ServerLevel world, ChunkPos pos)
	{
		world.m_7726_().m_8387_(ticketType, pos, 2, pos);
	}

	private static void removeChunkTicket(ServerLevel world, ChunkPos pos)
	{
		world.m_7726_().m_8438_(ticketType, pos, 2, pos);
	}

	private static final class SavedState extends SavedData
	{
		final Long2ObjectMap<Set<BlockPos>> chunksToChunkLoaders = new Long2ObjectOpenHashMap();

		SavedState()
		{
		}

		SavedState(CompoundTag nbt)
		{
			ListTag loaders = nbt.m_128437_("loaders", 10);

			for (int i = 0; i < loaders.size(); i++)
			{
				CompoundTag contentTag = loaders.m_128728_(i);
				BlockPos pos = new BlockPos(contentTag.getInt("x"), contentTag.getInt("y"), contentTag.getInt("z"));
				((Set) this.chunksToChunkLoaders.computeIfAbsent(ChunkPos.m_151388_(pos), ignore -> new ObjectOpenHashSet(1))).add(pos);
			}
		}

		public CompoundTag m_7176_(CompoundTag nbt)
		{
			ListTag loaders = new ListTag();
			nbt.put("loaders", loaders);
			ObjectIterator var3 = this.chunksToChunkLoaders.values().iterator();

			while (var3.hasNext())
			{
				Set<BlockPos> positions = (Set<BlockPos>) var3.next();

				for (BlockPos pos : positions)
				{
					CompoundTag contentTag = new CompoundTag();
					loaders.add(contentTag);
					contentTag.putInt("x", pos.getX());
					contentTag.putInt("y", pos.getY());
					contentTag.putInt("z", pos.getZ());
				}
			}

			return nbt;
		}
	}
}
