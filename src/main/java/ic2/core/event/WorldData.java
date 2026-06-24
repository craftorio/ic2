package ic2.core.event;

import ic2.core.IC2;
import ic2.core.WindSim;
import ic2.core.block.personal.TradingMarket;
import ic2.core.energy.grid.EnergyNetLocal;
import ic2.core.network.TeUpdateDataServer;
import ic2.core.util.Util;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public class WorldData
{
	private static ConcurrentMap<ResourceLocation, WorldData> idxClient = IC2.envProxy.isClientEnv() ? new ConcurrentHashMap<>() : null;
	private static ConcurrentMap<ResourceLocation, WorldData> idxServer = new ConcurrentHashMap<>();
	public final EnergyNetLocal energyNet;
	public final Map<BlockEntity, TeUpdateDataServer> tesToUpdate = new IdentityHashMap<>();
	public final TradingMarket tradeMarket;
	public final WindSim windSim;
	public final Map<LevelChunk, CompoundTag> worldGenData = new IdentityHashMap<>();
	public final Set<LevelChunk> chunksToDecorate = Collections.newSetFromMap(new IdentityHashMap<>());
	public final Set<LevelChunk> pendingUnloadChunks = Collections.newSetFromMap(new IdentityHashMap<>());
	public final Long2ObjectMap<Set<BlockPos>> loadedChunks;
	public final Object2ObjectMap<BlockPos, LongSet> chunkLoaders;
	final Queue<IWorldTickCallback> singleUpdates = new ConcurrentLinkedQueue<>();
	final Set<IWorldTickCallback> continuousUpdates = new HashSet<>();
	final List<IWorldTickCallback> continuousUpdatesToAdd = new ArrayList<>();
	final List<IWorldTickCallback> continuousUpdatesToRemove = new ArrayList<>();
	boolean continuousUpdatesInUse = false;

	private WorldData(Level world)
	{
		if (!world.isClientSide)
		{
			this.energyNet = EnergyNetLocal.create(world);
			this.tradeMarket = new TradingMarket(world);
			this.windSim = new WindSim(world);
			this.loadedChunks = new Long2ObjectOpenHashMap<>();
			this.chunkLoaders = new Object2ObjectOpenHashMap<>();
		} else
		{
			this.energyNet = null;
			this.tradeMarket = null;
			this.windSim = null;
			this.loadedChunks = null;
			this.chunkLoaders = null;
		}
	}

	public static WorldData get(Level world)
	{
		return get(world, true);
	}

	public static WorldData get(Level world, boolean load)
	{
		if (world == null)
		{
			throw new IllegalArgumentException("world is null");
		}

		ConcurrentMap<ResourceLocation, WorldData> index = getIndex(!world.isClientSide);
		WorldData ret = index.get(getKey(world));
		if (ret == null && load)
		{
			ret = new WorldData(world);
			WorldData prev = index.putIfAbsent(getKey(world), ret);
			if (prev != null)
			{
				ret = prev;
			}

			return ret;
		} else
		{
			return ret;
		}
	}

	public static void onWorldUnload(Level world)
	{
		getIndex(!world.isClientSide).remove(getKey(world));
	}

	private static ResourceLocation getKey(Level world)
	{
		return Util.getDimId(world);
	}

	private static ConcurrentMap<ResourceLocation, WorldData> getIndex(boolean simulating)
	{
		return simulating ? idxServer : idxClient;
	}
}
