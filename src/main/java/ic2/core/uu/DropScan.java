package ic2.core.uu;

import com.google.common.base.Predicate;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import ic2.core.IC2;
import ic2.core.Ic2Player;
import ic2.core.init.MainConfig;
import ic2.core.util.Config;
import ic2.core.util.ConfigUtil;
import ic2.core.util.ItemComparableItemStack;
import ic2.core.util.LogCategory;
import ic2.core.util.ReflectionUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.ServerHangWatchdog;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.commons.lang3.mutable.MutableLong;

public class DropScan {
  public DropScan(WorldServer parentWorld, int range) {
    int id;
    this.collectionsToClear = new ArrayList<>();
    this.drops = new HashMap<>();
    this.typicalDrops = new IdentityHashMap<>();
    if (parentWorld == null)
      throw new NullPointerException("null world"); 
    if (range < 4)
      throw new IllegalArgumentException("range has to be at least 4"); 
    this.parentWorld = parentWorld;
    this.range = range;
    try {
      this.tmpDir = File.createTempFile("ic2uuscan", null);
      if (!this.tmpDir.delete() || !this.tmpDir.mkdir())
        throw new IOException("Can't create a temporary directory for map storage"); 
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    IC2.log.info(LogCategory.Uu, "Using %s for temporary data.", new Object[] { this.tmpDir });
    do {
      id = parentWorld.rand.nextInt();
    } while (DimensionManager.getWorld(id) != null);
    this.dimensionId = id;
    DimensionManager.registerDimension(this.dimensionId, parentWorld.provider.getDimensionType());
    this.world = new DummyWorld();
    this.player = Ic2Player.get((World)this.world);
    updateCollectionsToClear();
  }
  
  private void updateCollectionsToClear() {
    this.collectionsToClear.add(ReflectionUtil.getFieldValue(WorldServer_pendingTickListEntriesHashSet, this.world));
    this.collectionsToClear.add(ReflectionUtil.getFieldValue(WorldServer_pendingTickListEntriesTreeSet, this.world));
    this.collectionsToClear.add(this.world.loadedEntityList);
    this.collectionsToClear.add(this.world.loadedTileEntityList);
    this.collectionsToClear.add(this.world.tickableTileEntities);
  }
  
  public void start(int area, int areaCount) {
    if (FMLCommonHandler.instance().getSide().isServer())
      stopWatchDog(); 
    long lastPrint = 0L;
    for (int i = 0; i < areaCount; i++) {
      int x = IC2.random.nextInt(area) - area / 2;
      int z = IC2.random.nextInt(area) - area / 2;
      try {
        scanArea(x, z);
      } catch (Exception e) {
        IC2.log.warn(LogCategory.Uu, e, "Scan failed.");
      } 
      if (i % 4 == 0 && lastPrint <= System.nanoTime() - 10000000000L) {
        lastPrint = System.nanoTime();
        IC2.log.info(LogCategory.Uu, "World scan progress: %.1f%%.", new Object[] { Float.valueOf(100.0F * i / areaCount) });
      } 
    } 
    analyze();
  }
  
  private void stopWatchDog() {
    if (this.world.getMinecraftServer() instanceof DedicatedServer && ((DedicatedServer)this.world.getMinecraftServer()).getMaxTickTime() > 0L)
      try {
        Method getThreads = Thread.class.getDeclaredMethod("getThreads", new Class[0]);
        getThreads.setAccessible(true);
        Thread[] threads = (Thread[])getThreads.invoke(null, new Object[0]);
        Field f = ReflectionUtil.getField(Thread.class, Runnable.class);
        for (Thread thread : threads) {
          Object target;
          if (thread.getClass() == Thread.class && target = ReflectionUtil.getFieldValue(f, thread) instanceof ServerHangWatchdog) {
            ReflectionUtil.setValue(this.watchDog = target, ReflectionUtil.getField(ServerHangWatchdog.class, long.class), Long.valueOf(Long.MAX_VALUE));
            break;
          } 
        } 
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException("Error stopping Watchdog", e);
      }  
  }
  
  public void cleanup() {
    DimensionManager.setWorld(this.dimensionId, null, this.parentWorld.getMinecraftServer());
    DimensionManager.unregisterDimension(this.dimensionId);
    deleteRecursive(this.tmpDir, false);
    if (this.watchDog != null)
      fixWatchDog(); 
  }
  
  private static void deleteRecursive(File file, boolean deleteFiles) {
    if (!file.isDirectory())
      throw new IllegalArgumentException("no dir: " + file); 
    for (File subFile : file.listFiles()) {
      if (subFile.isDirectory()) {
        deleteRecursive(subFile, deleteFiles);
      } else if (deleteFiles) {
        subFile.delete();
      } 
    } 
    file.delete();
  }
  
  private void fixWatchDog() {
    ReflectionUtil.setValue(this.watchDog, ReflectionUtil.getField(ServerHangWatchdog.class, long.class), Long.valueOf(((DedicatedServer)this.world.getMinecraftServer()).getMaxTickTime()));
    this.watchDog = null;
  }
  
  private void analyze() {
    double normalizeBy;
    ItemComparableItemStack cobblestone = new ItemComparableItemStack(new ItemStack(Blocks.COBBLESTONE), false);
    ItemComparableItemStack netherrack = new ItemComparableItemStack(new ItemStack(Blocks.NETHERRACK), false);
    if (!this.drops.containsKey(cobblestone)) {
      if (!this.drops.containsKey(netherrack)) {
        IC2.log.warn(LogCategory.Uu, "UU scan failed, there was no cobblestone or netherrack dropped");
        return;
      } 
      normalizeBy = ((MutableLong)this.drops.get(netherrack)).getValue().longValue();
    } else {
      normalizeBy = ((MutableLong)this.drops.get(cobblestone)).getValue().longValue();
      if (this.drops.containsKey(netherrack))
        normalizeBy = Math.max(normalizeBy, ((MutableLong)this.drops.get(netherrack)).getValue().longValue()); 
    } 
    Config config = MainConfig.get().getSub("balance/uu-values/world scan");
    if (config == null)
      config = MainConfig.get().getSub("balance/uu-values").addSub("world scan", "Initial uu values from scanning the world.\nRun /ic2 uu-world-scan <small|medium|large> to calibrate them for your world.\nDelete this whole section to revert to the default predefined values."); 
    List<Map.Entry<ItemComparableItemStack, MutableLong>> sorted = new ArrayList<>(this.drops.entrySet());
    this.drops.clear();
    Collections.sort(sorted, new Comparator<Map.Entry<ItemComparableItemStack, MutableLong>>() {
          public int compare(Map.Entry<ItemComparableItemStack, MutableLong> a, Map.Entry<ItemComparableItemStack, MutableLong> b) {
            return Long.compare(((MutableLong)b.getValue()).getValue().longValue(), ((MutableLong)a.getValue()).getValue().longValue());
          }
        });
    IC2.log.info(LogCategory.Uu, "total");
    for (Map.Entry<ItemComparableItemStack, MutableLong> entry : sorted) {
      ItemStack stack = ((ItemComparableItemStack)entry.getKey()).toStack();
      long count = ((MutableLong)entry.getValue()).getValue().longValue();
      IC2.log.info(LogCategory.Uu, "%d %s", new Object[] { Long.valueOf(count), stack.getItem().getItemStackDisplayName(stack) });
      config.set(ConfigUtil.fromStack(stack), Double.valueOf(normalizeBy / count));
    } 
    MainConfig.save();
  }
  
  private void scanArea(int xStart, int zStart) {
    DummyChunkProvider provider = this.world.getChunkProvider();
    List<Chunk> chunks = new ArrayList<>(Util.square(this.range));
    List<Chunk> toDecorate = new ArrayList<>(Util.square(this.range - 1));
    List<Chunk> toScan = new ArrayList<>(Util.square(this.range - 3));
    provider.enableGenerate();
    for (int x = xStart; x < xStart + this.range; x++) {
      for (int z = zStart; z < zStart + this.range; z++) {
        Chunk chunk = this.world.getChunkFromChunkCoords(x, z);
        chunks.add(chunk);
        if (x != xStart + this.range - 1 && z != zStart + this.range - 1) {
          toDecorate.add(chunk);
          if (x != xStart && x != xStart + this.range - 2 && z != zStart && z != zStart + this.range - 2)
            toScan.add(chunk); 
        } 
      } 
    } 
    provider.setChunks(chunks, xStart, zStart);
    for (Chunk chunk : toDecorate)
      MinecraftForge.EVENT_BUS.post((Event)new ChunkEvent.Load(chunk)); 
    for (Chunk chunk : toDecorate)
      chunk.populate((IChunkProvider)provider, provider.chunkGenerator); 
    provider.disableGenerate();
    for (Chunk chunk : toScan)
      scanChunk(this.world, chunk); 
    for (Chunk chunk : toDecorate)
      MinecraftForge.EVENT_BUS.post((Event)new ChunkEvent.Unload(chunk)); 
    this.world.clear();
  }
  
  private void scanChunk(DummyWorld world, Chunk chunk) {
    assert world.getChunkFromChunkCoords(chunk.x, chunk.z) == chunk;
    int xMax = (chunk.x + 1) * 16;
    int yMax = world.getHeight();
    int zMax = (chunk.z + 1) * 16;
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    for (int y = 0; y < yMax; y++) {
      for (int z = chunk.z * 16; z < zMax; z++) {
        for (int x = chunk.x * 16; x < xMax; x++) {
          pos.setPos(x, y, z);
          IBlockState state = chunk.getBlockState((BlockPos)pos);
          Block block = state.getBlock();
          if (!block.isAir(state, (IBlockAccess)world, (BlockPos)pos))
            for (ItemStack drop : getDrops(world, (BlockPos)pos, block, state))
              addDrop(drop);  
        } 
      } 
    } 
  }
  
  private List<ItemStack> getDrops(DummyWorld world, BlockPos pos, Block block, IBlockState state) {
    DropDesc typicalDrop = this.typicalDrops.get(state);
    if (typicalDrop == null || typicalDrop.dropCount.get() < 1000) {
      block.onBlockHarvested((World)world, pos, state, this.player);
      if (block.removedByPlayer(state, (World)world, pos, this.player, true)) {
        block.onBlockDestroyedByPlayer((World)world, pos, state);
        block.dropBlockAsItem((World)world, pos, state, 0);
      } else {
        IC2.log.info(LogCategory.Uu, "Can't harvest %s.", new Object[] { block });
      } 
      List<ItemStack> drops = new ArrayList<>(world.spawnedEntities.size());
      for (Entity entity : world.spawnedEntities) {
        if (entity instanceof EntityItem)
          drops.add(((EntityItem)entity).getItem()); 
      } 
      world.spawnedEntities.clear();
      if (typicalDrop == null) {
        typicalDrop = new DropDesc(drops);
        this.typicalDrops.put(state, typicalDrop);
      } 
      if (typicalDrop.dropCount.get() >= 0) {
        boolean equal = (typicalDrop.drops.size() == drops.size());
        if (equal)
          for (Iterator<ItemStack> it = drops.iterator(), it2 = typicalDrop.drops.iterator(); it.hasNext(); ) {
            ItemStack a = it.next();
            ItemStack b = it2.next();
            if (!ItemStack.areItemStacksEqual(a, b)) {
              equal = false;
              break;
            } 
          }  
        if (equal) {
          int prev = typicalDrop.dropCount.incrementAndGet();
          if (prev < 0)
            typicalDrop.dropCount.set(-2147483648); 
        } else {
          typicalDrop.dropCount.set(-2147483648);
        } 
      } 
      return drops;
    } 
    return typicalDrop.drops;
  }
  
  private void addDrop(ItemStack stack) {
    ItemComparableItemStack key = new ItemComparableItemStack(stack, false);
    MutableLong amount = this.drops.get(key);
    if (amount == null) {
      amount = new MutableLong();
      this.drops.put(key.copy(), amount);
    } 
    amount.add(StackUtil.getSize(stack));
  }
  
  class DummyWorld extends WorldServer {
    List<Entity> spawnedEntities;
    
    public DummyWorld() {
      super(DropScan.this.parentWorld.getMinecraftServer(), new DropScan.DummySaveHandler(DropScan.this, null), DropScan.this.parentWorld.getWorldInfo(), DropScan.this.dimensionId, DropScan.this.parentWorld.profiler);
      this.spawnedEntities = new ArrayList<>();
      this.lootTable = DropScan.this.parentWorld.getLootTableManager();
    }
    
    protected IChunkProvider createChunkProvider() {
      return (IChunkProvider)new DropScan.DummyChunkProvider(this, this.provider.createChunkGenerator());
    }
    
    public DropScan.DummyChunkProvider getChunkProvider() {
      return (DropScan.DummyChunkProvider)super.getChunkProvider();
    }
    
    public File getChunkSaveLocation() {
      return DropScan.this.tmpDir;
    }
    
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
      return (getChunkProvider().getLoadedChunk(x, z) != null);
    }
    
    public Entity getEntityByID(int i) {
      return null;
    }
    
    public boolean setBlockState(BlockPos pos, IBlockState state, int flags) {
      if (pos.getY() >= 256 || pos.getY() < 0)
        return false; 
      Chunk chunk = getChunkFromChunkCoords(pos.getX() >> 4, pos.getZ() >> 4);
      return (chunk.setBlockState(pos, state) != null);
    }
    
    public boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos) {
      return true;
    }
    
    public void tick() {}
    
    public boolean spawnEntity(Entity entity) {
      this.spawnedEntities.add(entity);
      return true;
    }
    
    public void clear() {
      getChunkProvider().clear();
      for (Collection<?> c : (Iterable<Collection<?>>)DropScan.this.collectionsToClear)
        c.clear(); 
    }
  }
  
  private static class EmptyChunk extends Chunk {
    public EmptyChunk(World world, int x, int z) {
      super(world, x, z);
    }
    
    public boolean isAtLocation(int x, int z) {
      return (this.x == x && this.z == z);
    }
    
    public int getHeightValue(int x, int z) {
      return 0;
    }
    
    public void generateHeightMap() {}
    
    public void generateSkylightMap() {}
    
    public IBlockState getBlockState(BlockPos pos) {
      return Blocks.AIR.getDefaultState();
    }
    
    public int getBlockLightOpacity(BlockPos pos) {
      return 255;
    }
    
    public int getLightFor(EnumSkyBlock sky, BlockPos pos) {
      return sky.defaultLightValue;
    }
    
    public void setLightFor(EnumSkyBlock sky, BlockPos pos, int value) {}
    
    public int getLightSubtracted(BlockPos pos, int amount) {
      return 0;
    }
    
    public void addEntity(Entity entity) {}
    
    public void removeEntity(Entity entity) {}
    
    public void removeEntityAtIndex(Entity entity, int index) {}
    
    public boolean canSeeSky(BlockPos pos) {
      return false;
    }
    
    @Nullable
    public TileEntity getTileEntity(BlockPos pos, Chunk.EnumCreateEntityType createType) {
      return null;
    }
    
    public void addTileEntity(TileEntity tileEntity) {}
    
    public void addTileEntity(BlockPos pos, TileEntity tileEntity) {}
    
    public void removeTileEntity(BlockPos pos) {}
    
    public void onLoad() {}
    
    public void onUnload() {}
    
    public void markDirty() {}
    
    public void getEntitiesWithinAABBForEntity(@Nullable Entity entity, AxisAlignedBB aabb, List<Entity> listToFill, Predicate<? super Entity> valid) {}
    
    public <T extends Entity> void getEntitiesOfTypeWithinAABB(Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill, Predicate<? super T> valid) {}
    
    public boolean needsSaving(boolean flag) {
      return false;
    }
    
    public Random getRandomWithSeed(long seed) {
      return new Random(getWorld().getSeed() + (this.x * this.x * 4987142) + (this.x * 5947611) + (this.z * this.z) * 4392871L + (this.z * 389711) ^ seed);
    }
    
    public boolean isEmpty() {
      return true;
    }
    
    public boolean isEmptyBetween(int startY, int endY) {
      return true;
    }
  }
  
  class DummyChunkProvider extends ChunkProviderServer {
    private final Chunk emptyChunk;
    
    private final TLongObjectMap<Chunk> extraChunks;
    
    private final Chunk[] chunks;
    
    private int xStart;
    
    private int zStart;
    
    private boolean disableGenerate;
    
    public DummyChunkProvider(WorldServer world, IChunkGenerator chunkGenerator) {
      super(world, null, chunkGenerator);
      this.extraChunks = (TLongObjectMap<Chunk>)new TLongObjectHashMap();
      this.emptyChunk = new DropScan.EmptyChunk((World)world, 0, 0);
      this.chunks = new Chunk[Util.square(DropScan.this.range)];
    }
    
    public void setChunks(List<Chunk> newChunks, int xStart, int zStart) {
      clear();
      this.xStart = xStart;
      this.zStart = zStart;
      for (Chunk chunk : newChunks) {
        int index = getIndex(chunk.x, chunk.z);
        if (index < 0)
          throw new IllegalArgumentException("out of range"); 
        this.chunks[index] = chunk;
      } 
    }
    
    public void enableGenerate() {
      this.disableGenerate = false;
    }
    
    public void disableGenerate() {
      this.disableGenerate = true;
    }
    
    public void clear() {
      this.extraChunks.clear();
      Arrays.fill((Object[])this.chunks, (Object)null);
    }
    
    public String makeString() {
      return "Dummy";
    }
    
    public Chunk getLoadedChunk(int x, int z) {
      int index = getIndex(x, z);
      if (index >= 0)
        return this.chunks[index]; 
      return (Chunk)this.extraChunks.get(ChunkPos.asLong(x, z));
    }
    
    public Chunk provideChunk(int x, int z) {
      Chunk ret = getLoadedChunk(x, z);
      if (ret == null) {
        if (this.disableGenerate)
          return this.emptyChunk; 
        ret = this.chunkGenerator.generateChunk(x, z);
        int index = getIndex(x, z);
        if (index >= 0) {
          this.chunks[index] = ret;
        } else {
          this.extraChunks.put(ChunkPos.asLong(x, z), ret);
        } 
      } 
      return ret;
    }
    
    public boolean saveChunks(boolean all) {
      return true;
    }
    
    public void flushToDisk() {}
    
    public boolean tick() {
      return false;
    }
    
    private int getIndex(int x, int z) {
      x -= this.xStart;
      z -= this.zStart;
      if (x < 0 || x >= DropScan.this.range || z < 0 || z >= DropScan.this.range)
        return -1; 
      return x * DropScan.this.range + z;
    }
  }
  
  private class DummySaveHandler implements ISaveHandler {
    public WorldInfo loadWorldInfo() {
      return DropScan.this.world.getWorldInfo();
    }
    
    public void checkSessionLock() throws MinecraftException {}
    
    public IChunkLoader getChunkLoader(WorldProvider provider) {
      throw new UnsupportedOperationException();
    }
    
    public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {}
    
    public void saveWorldInfo(WorldInfo worldInformation) {}
    
    public IPlayerFileData getPlayerNBTManager() {
      throw new UnsupportedOperationException();
    }
    
    public void flush() {}
    
    public File getWorldDirectory() {
      throw new UnsupportedOperationException();
    }
    
    public File getMapFileFromName(String mapName) {
      throw new UnsupportedOperationException();
    }
    
    public TemplateManager getStructureTemplateManager() {
      return this.templateManager;
    }
    
    private final TemplateManager templateManager = new TemplateManager(DropScan.this.tmpDir.toString(), new DataFixer(0));
    
    private DummySaveHandler() {}
  }
  
  private static final class DropDesc {
    List<ItemStack> drops;
    
    AtomicInteger dropCount;
    
    DropDesc(List<ItemStack> drops) {
      this.dropCount = new AtomicInteger();
      this.drops = drops;
    }
  }
  
  private static final Field WorldServer_pendingTickListEntriesHashSet = ReflectionUtil.getField(WorldServer.class, new String[] { "pendingTickListEntriesHashSet", "pendingTickListEntriesHashSet" });
  
  private static final Field WorldServer_pendingTickListEntriesTreeSet = ReflectionUtil.getField(WorldServer.class, new String[] { "pendingTickListEntriesTreeSet", "pendingTickListEntriesTreeSet" });
  
  private final WorldServer parentWorld;
  
  private final int range;
  
  private final List<Collection<?>> collectionsToClear;
  
  private final File tmpDir;
  
  private final int dimensionId;
  
  private final DummyWorld world;
  
  private final EntityPlayer player;
  
  private Object watchDog;
  
  private final Map<ItemComparableItemStack, MutableLong> drops;
  
  private final Map<IBlockState, DropDesc> typicalDrops;
}
