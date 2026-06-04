// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import java.util.concurrent.ConcurrentHashMap;
import net.minecraftforge.fml.common.FMLCommonHandler;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import ic2.core.block.personal.TradingMarket;
import ic2.core.network.TeUpdateDataServer;
import net.minecraft.tileentity.TileEntity;
import java.util.Map;
import ic2.core.energy.grid.EnergyNetLocal;
import java.util.List;
import java.util.Set;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;

public class WorldData
{
    private static ConcurrentMap<Integer, WorldData> idxClient;
    private static ConcurrentMap<Integer, WorldData> idxServer;
    final Queue<IWorldTickCallback> singleUpdates;
    final Set<IWorldTickCallback> continuousUpdates;
    boolean continuousUpdatesInUse;
    final List<IWorldTickCallback> continuousUpdatesToAdd;
    final List<IWorldTickCallback> continuousUpdatesToRemove;
    public final EnergyNetLocal energyNet;
    public final Map<TileEntity, TeUpdateDataServer> tesToUpdate;
    public final TradingMarket tradeMarket;
    public final WindSim windSim;
    public final Map<Chunk, NBTTagCompound> worldGenData;
    public final Set<Chunk> chunksToDecorate;
    public final Set<Chunk> pendingUnloadChunks;
    
    private WorldData(final World world) {
        this.singleUpdates = new ConcurrentLinkedQueue<IWorldTickCallback>();
        this.continuousUpdates = new HashSet<IWorldTickCallback>();
        this.continuousUpdatesInUse = false;
        this.continuousUpdatesToAdd = new ArrayList<IWorldTickCallback>();
        this.continuousUpdatesToRemove = new ArrayList<IWorldTickCallback>();
        this.tesToUpdate = new IdentityHashMap<TileEntity, TeUpdateDataServer>();
        this.worldGenData = new IdentityHashMap<Chunk, NBTTagCompound>();
        this.chunksToDecorate = Collections.newSetFromMap(new IdentityHashMap<Chunk, Boolean>());
        this.pendingUnloadChunks = Collections.newSetFromMap(new IdentityHashMap<Chunk, Boolean>());
        if (!world.isRemote) {
            this.energyNet = EnergyNetLocal.create(world);
            this.tradeMarket = new TradingMarket(world);
            this.windSim = new WindSim(world);
        }
        else {
            this.energyNet = null;
            this.tradeMarket = null;
            this.windSim = null;
        }
    }
    
    public static WorldData get(final World world) {
        return get(world, true);
    }
    
    public static WorldData get(final World world, final boolean load) {
        if (world == null) {
            throw new IllegalArgumentException("world is null");
        }
        final ConcurrentMap<Integer, WorldData> index = getIndex(!world.isRemote);
        WorldData ret = index.get(world.provider.getDimension());
        if (ret != null || !load) {
            return ret;
        }
        ret = new WorldData(world);
        final WorldData prev = index.putIfAbsent(world.provider.getDimension(), ret);
        if (prev != null) {
            ret = prev;
        }
        return ret;
    }
    
    public static void onWorldUnload(final World world) {
        getIndex(!world.isRemote).remove(world.provider.getDimension());
    }
    
    private static ConcurrentMap<Integer, WorldData> getIndex(final boolean simulating) {
        return simulating ? WorldData.idxServer : WorldData.idxClient;
    }
    
    static {
        WorldData.idxClient = (FMLCommonHandler.instance().getSide().isClient() ? new ConcurrentHashMap<Integer, WorldData>() : null);
        WorldData.idxServer = new ConcurrentHashMap<Integer, WorldData>();
    }
}
