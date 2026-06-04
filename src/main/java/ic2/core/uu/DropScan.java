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
    DimensionManager.registerDimension(this.dimensionId, parentWorld.provider.func_186058_p());
    this.world = new DummyWorld();
    this.player = Ic2Player.get((World)this.world);
    updateCollectionsToClear();
  }
  
  private void updateCollectionsToClear() {
    this.collectionsToClear.add(ReflectionUtil.getFieldValue(WorldServer_pendingTickListEntriesHashSet, this.world));
    this.collectionsToClear.add(ReflectionUtil.getFieldValue(WorldServer_pendingTickListEntriesTreeSet, this.world));
    this.collectionsToClear.add(this.world.field_72996_f);
    this.collectionsToClear.add(this.world.field_147482_g);
    this.collectionsToClear.add(this.world.field_175730_i);
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
    if (this.world.func_73046_m() instanceof DedicatedServer && ((DedicatedServer)this.world.func_73046_m()).func_175593_aQ() > 0L)
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
    DimensionManager.setWorld(this.dimensionId, null, this.parentWorld.func_73046_m());
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
    ReflectionUtil.setValue(this.watchDog, ReflectionUtil.getField(ServerHangWatchdog.class, long.class), Long.valueOf(((DedicatedServer)this.world.func_73046_m()).func_175593_aQ()));
    this.watchDog = null;
  }
  
  private void analyze() {
    double normalizeBy;
    ItemComparableItemStack cobblestone = new ItemComparableItemStack(new ItemStack(Blocks.field_150347_e), false);
    ItemComparableItemStack netherrack = new ItemComparableItemStack(new ItemStack(Blocks.field_150424_aL), false);
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
      IC2.log.info(LogCategory.Uu, "%d %s", new Object[] { Long.valueOf(count), stack.getItem().func_77653_i(stack) });
      config.set(ConfigUtil.fromStack(stack), Double.valueOf(normalizeBy / count));
    } 
    MainConfig.save();
  }
  
  private void scanArea(int xStart, int zStart) {
    DummyChunkProvider provider = this.world.func_72863_F();
    List<Chunk> chunks = new ArrayList<>(Util.square(this.range));
    List<Chunk> toDecorate = new ArrayList<>(Util.square(this.range - 1));
    List<Chunk> toScan = new ArrayList<>(Util.square(this.range - 3));
    provider.enableGenerate();
    for (int x = xStart; x < xStart + this.range; x++) {
      for (int z = zStart; z < zStart + this.range; z++) {
        Chunk chunk = this.world.func_72964_e(x, z);
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
      chunk.func_186030_a((IChunkProvider)provider, provider.field_186029_c); 
    provider.disableGenerate();
    for (Chunk chunk : toScan)
      scanChunk(this.world, chunk); 
    for (Chunk chunk : toDecorate)
      MinecraftForge.EVENT_BUS.post((Event)new ChunkEvent.Unload(chunk)); 
    this.world.clear();
  }
  
  private void scanChunk(DummyWorld world, Chunk chunk) {
    assert world.func_72964_e(chunk.field_76635_g, chunk.field_76647_h) == chunk;
    int xMax = (chunk.field_76635_g + 1) * 16;
    int yMax = world.func_72800_K();
    int zMax = (chunk.field_76647_h + 1) * 16;
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    for (int y = 0; y < yMax; y++) {
      for (int z = chunk.field_76647_h * 16; z < zMax; z++) {
        for (int x = chunk.field_76635_g * 16; x < xMax; x++) {
          pos.setPos(x, y, z);
          IBlockState state = chunk.func_177435_g((BlockPos)pos);
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
      block.func_176208_a((World)world, pos, state, this.player);
      if (block.removedByPlayer(state, (World)world, pos, this.player, true)) {
        block.func_176206_d((World)world, pos, state);
        block.func_176226_b((World)world, pos, state, 0);
      } else {
        IC2.log.info(LogCategory.Uu, "Can't harvest %s.", new Object[] { block });
      } 
      List<ItemStack> drops = new ArrayList<>(world.spawnedEntities.size());
      for (Entity entity : world.spawnedEntities) {
        if (entity instanceof EntityItem)
          drops.add(((EntityItem)entity).func_92059_d()); 
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
      super(DropScan.this.parentWorld.func_73046_m(), new DropScan.DummySaveHandler(DropScan.this, null), DropScan.this.parentWorld.func_72912_H(), DropScan.this.dimensionId, DropScan.this.parentWorld.field_72984_F);
      this.spawnedEntities = new ArrayList<>();
      this.field_184151_B = DropScan.this.parentWorld.func_184146_ak();
    }
    
    protected IChunkProvider func_72970_h() {
      return (IChunkProvider)new DropScan.DummyChunkProvider(this, this.provider.func_186060_c());
    }
    
    public DropScan.DummyChunkProvider func_72863_F() {
      return (DropScan.DummyChunkProvider)super.func_72863_F();
    }
    
    public File getChunkSaveLocation() {
      return DropScan.this.tmpDir;
    }
    
    protected boolean func_175680_a(int x, int z, boolean allowEmpty) {
      return (func_72863_F().func_186026_b(x, z) != null);
    }
    
    public Entity func_73045_a(int i) {
      return null;
    }
    
    public boolean func_180501_a(BlockPos pos, IBlockState state, int flags) {
      if (pos.getY() >= 256 || pos.getY() < 0)
        return false; 
      Chunk chunk = func_72964_e(pos.getX() >> 4, pos.getZ() >> 4);
      return (chunk.func_177436_a(pos, state) != null);
    }
    
    public boolean func_180500_c(EnumSkyBlock lightType, BlockPos pos) {
      return true;
    }
    
    public void func_72835_b() {}
    
    public boolean spawnEntity(Entity entity) {
      this.spawnedEntities.add(entity);
      return true;
    }
    
    public void clear() {
      func_72863_F().clear();
      for (Collection<?> c : (Iterable<Collection<?>>)DropScan.this.collectionsToClear)
        c.clear(); 
    }
  }
  
  private static class EmptyChunk extends Chunk {
    public EmptyChunk(World world, int x, int z) {
      super(world, x, z);
    }
    
    public boolean func_76600_a(int x, int z) {
      return (this.field_76635_g == x && this.field_76647_h == z);
    }
    
    public int func_76611_b(int x, int z) {
      return 0;
    }
    
    public void func_76590_a() {}
    
    public void func_76603_b() {}
    
    public IBlockState func_177435_g(BlockPos pos) {
      return Blocks.AIR.getDefaultState();
    }
    
    public int func_177437_b(BlockPos pos) {
      return 255;
    }
    
    public int func_177413_a(EnumSkyBlock sky, BlockPos pos) {
      return sky.field_77198_c;
    }
    
    public void func_177431_a(EnumSkyBlock sky, BlockPos pos, int value) {}
    
    public int func_177443_a(BlockPos pos, int amount) {
      return 0;
    }
    
    public void func_76612_a(Entity entity) {}
    
    public void func_76622_b(Entity entity) {}
    
    public void func_76608_a(Entity entity, int index) {}
    
    public boolean func_177444_d(BlockPos pos) {
      return false;
    }
    
    @Nullable
    public TileEntity func_177424_a(BlockPos pos, Chunk.EnumCreateEntityType createType) {
      return null;
    }
    
    public void func_150813_a(TileEntity tileEntity) {}
    
    public void func_177426_a(BlockPos pos, TileEntity tileEntity) {}
    
    public void func_177425_e(BlockPos pos) {}
    
    public void func_76631_c() {}
    
    public void func_76623_d() {}
    
    public void func_76630_e() {}
    
    public void func_177414_a(@Nullable Entity entity, AxisAlignedBB aabb, List<Entity> listToFill, Predicate<? super Entity> valid) {}
    
    public <T extends Entity> void func_177430_a(Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill, Predicate<? super T> valid) {}
    
    public boolean func_76601_a(boolean flag) {
      return false;
    }
    
    public Random func_76617_a(long seed) {
      return new Random(func_177412_p().func_72905_C() + (this.field_76635_g * this.field_76635_g * 4987142) + (this.field_76635_g * 5947611) + (this.field_76647_h * this.field_76647_h) * 4392871L + (this.field_76647_h * 389711) ^ seed);
    }
    
    public boolean func_76621_g() {
      return true;
    }
    
    public boolean func_76606_c(int startY, int endY) {
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
        int index = getIndex(chunk.field_76635_g, chunk.field_76647_h);
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
    
    public String func_73148_d() {
      return "Dummy";
    }
    
    public Chunk func_186026_b(int x, int z) {
      int index = getIndex(x, z);
      if (index >= 0)
        return this.chunks[index]; 
      return (Chunk)this.extraChunks.get(ChunkPos.func_77272_a(x, z));
    }
    
    public Chunk func_186025_d(int x, int z) {
      Chunk ret = func_186026_b(x, z);
      if (ret == null) {
        if (this.disableGenerate)
          return this.emptyChunk; 
        ret = this.field_186029_c.func_185932_a(x, z);
        int index = getIndex(x, z);
        if (index >= 0) {
          this.chunks[index] = ret;
        } else {
          this.extraChunks.put(ChunkPos.func_77272_a(x, z), ret);
        } 
      } 
      return ret;
    }
    
    public boolean func_186027_a(boolean all) {
      return true;
    }
    
    public void func_104112_b() {}
    
    public boolean func_73156_b() {
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
    public WorldInfo func_75757_d() {
      return DropScan.this.world.func_72912_H();
    }
    
    public void func_75762_c() throws MinecraftException {}
    
    public IChunkLoader func_75763_a(WorldProvider provider) {
      throw new UnsupportedOperationException();
    }
    
    public void func_75755_a(WorldInfo worldInformation, NBTTagCompound tagCompound) {}
    
    public void func_75761_a(WorldInfo worldInformation) {}
    
    public IPlayerFileData func_75756_e() {
      throw new UnsupportedOperationException();
    }
    
    public void func_75759_a() {}
    
    public File func_75765_b() {
      throw new UnsupportedOperationException();
    }
    
    public File func_75758_b(String mapName) {
      throw new UnsupportedOperationException();
    }
    
    public TemplateManager func_186340_h() {
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
  
  private static final Field WorldServer_pendingTickListEntriesHashSet = ReflectionUtil.getField(WorldServer.class, new String[] { "field_73064_N", "pendingTickListEntriesHashSet" });
  
  private static final Field WorldServer_pendingTickListEntriesTreeSet = ReflectionUtil.getField(WorldServer.class, new String[] { "field_73065_O", "pendingTickListEntriesTreeSet" });
  
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
