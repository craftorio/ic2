package ic2.core;

import ic2.core.block.personal.TradingMarket;
import ic2.core.energy.grid.EnergyNetLocal;
import ic2.core.network.TeUpdateDataServer;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class WorldData {
  private WorldData(World world) {
    if (!world.isRemote) {
      this.energyNet = EnergyNetLocal.create(world);
      this.tradeMarket = new TradingMarket(world);
      this.windSim = new WindSim(world);
    } else {
      this.energyNet = null;
      this.tradeMarket = null;
      this.windSim = null;
    } 
  }
  
  public static WorldData get(World world) {
    return get(world, true);
  }
  
  public static WorldData get(World world, boolean load) {
    if (world == null)
      throw new IllegalArgumentException("world is null"); 
    ConcurrentMap<Integer, WorldData> index = getIndex(!world.isRemote);
    WorldData ret = index.get(Integer.valueOf(world.provider.getDimension()));
    if (ret != null || !load)
      return ret; 
    ret = new WorldData(world);
    WorldData prev = index.putIfAbsent(Integer.valueOf(world.provider.getDimension()), ret);
    if (prev != null)
      ret = prev; 
    return ret;
  }
  
  public static void onWorldUnload(World world) {
    getIndex(!world.isRemote).remove(Integer.valueOf(world.provider.getDimension()));
  }
  
  private static ConcurrentMap<Integer, WorldData> getIndex(boolean simulating) {
    return simulating ? idxServer : idxClient;
  }
  
  private static ConcurrentMap<Integer, WorldData> idxClient = FMLCommonHandler.instance().getSide().isClient() ? new ConcurrentHashMap<>() : null;
  
  private static ConcurrentMap<Integer, WorldData> idxServer = new ConcurrentHashMap<>();
  
  final Queue<IWorldTickCallback> singleUpdates = new ConcurrentLinkedQueue<>();
  
  final Set<IWorldTickCallback> continuousUpdates = new HashSet<>();
  
  boolean continuousUpdatesInUse = false;
  
  final List<IWorldTickCallback> continuousUpdatesToAdd = new ArrayList<>();
  
  final List<IWorldTickCallback> continuousUpdatesToRemove = new ArrayList<>();
  
  public final EnergyNetLocal energyNet;
  
  public final Map<TileEntity, TeUpdateDataServer> tesToUpdate = new IdentityHashMap<>();
  
  public final TradingMarket tradeMarket;
  
  public final WindSim windSim;
  
  public final Map<Chunk, NBTTagCompound> worldGenData = new IdentityHashMap<>();
  
  public final Set<Chunk> chunksToDecorate = Collections.newSetFromMap(new IdentityHashMap<>());
  
  public final Set<Chunk> pendingUnloadChunks = Collections.newSetFromMap(new IdentityHashMap<>());
}
