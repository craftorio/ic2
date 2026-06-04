// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.uu;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.util.math.ChunkPos;
import java.util.Arrays;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.IChunkGenerator;
import gnu.trove.map.TLongObjectMap;
import java.util.Random;
import com.google.common.base.Predicate;
import net.minecraft.util.math.AxisAlignedBB;
import javax.annotation.Nullable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.storage.ISaveHandler;
import ic2.core.util.StackUtil;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.Entity;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.world.chunk.Chunk;
import ic2.core.util.Util;
import java.util.Iterator;
import ic2.core.util.Config;
import ic2.core.util.ConfigUtil;
import java.util.Collections;
import java.util.Comparator;
import ic2.core.init.MainConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import java.lang.reflect.Method;
import net.minecraft.server.dedicated.ServerHangWatchdog;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import ic2.core.util.ReflectionUtil;
import net.minecraft.world.World;
import ic2.core.Ic2Player;
import net.minecraftforge.common.DimensionManager;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.ArrayList;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.mutable.MutableLong;
import ic2.core.util.ItemComparableItemStack;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import java.io.File;
import java.util.Collection;
import java.util.List;
import net.minecraft.world.WorldServer;
import java.lang.reflect.Field;

public class DropScan
{
    private static final Field WorldServer_pendingTickListEntriesHashSet;
    private static final Field WorldServer_pendingTickListEntriesTreeSet;
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
    
    public DropScan(final WorldServer parentWorld, final int range) {
        this.collectionsToClear = new ArrayList<Collection<?>>();
        this.drops = new HashMap<ItemComparableItemStack, MutableLong>();
        this.typicalDrops = new IdentityHashMap<IBlockState, DropDesc>();
        if (parentWorld == null) {
            throw new NullPointerException("null world");
        }
        if (range < 4) {
            throw new IllegalArgumentException("range has to be at least 4");
        }
        this.parentWorld = parentWorld;
        this.range = range;
        try {
            this.tmpDir = File.createTempFile("ic2uuscan", null);
            if (!this.tmpDir.delete() || !this.tmpDir.mkdir()) {
                throw new IOException("Can't create a temporary directory for map storage");
            }
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
        IC2.log.info(LogCategory.Uu, "Using %s for temporary data.", this.tmpDir);
        int id;
        do {
            id = parentWorld.rand.nextInt();
        } while (DimensionManager.getWorld(id) != null);
        DimensionManager.registerDimension(this.dimensionId = id, parentWorld.provider.getDimensionType());
        this.world = new DummyWorld();
        this.player = Ic2Player.get((World)this.world);
        this.updateCollectionsToClear();
    }
    
    private void updateCollectionsToClear() {
        this.collectionsToClear.add(ReflectionUtil.getFieldValue(DropScan.WorldServer_pendingTickListEntriesHashSet, this.world));
        this.collectionsToClear.add(ReflectionUtil.getFieldValue(DropScan.WorldServer_pendingTickListEntriesTreeSet, this.world));
        this.collectionsToClear.add(this.world.loadedEntityList);
        this.collectionsToClear.add(this.world.loadedTileEntityList);
        this.collectionsToClear.add(this.world.tickableTileEntities);
    }
    
    public void start(final int area, final int areaCount) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            this.stopWatchDog();
        }
        long lastPrint = 0L;
        for (int i = 0; i < areaCount; ++i) {
            final int x = IC2.random.nextInt(area) - area / 2;
            final int z = IC2.random.nextInt(area) - area / 2;
            try {
                this.scanArea(x, z);
            }
            catch (final Exception e) {
                IC2.log.warn(LogCategory.Uu, e, "Scan failed.");
            }
            if (i % 4 == 0 && lastPrint <= System.nanoTime() - 10000000000L) {
                lastPrint = System.nanoTime();
                IC2.log.info(LogCategory.Uu, "World scan progress: %.1f%%.", 100.0f * i / areaCount);
            }
        }
        this.analyze();
    }
    
    private void stopWatchDog() {
        if (this.world.getMinecraftServer() instanceof DedicatedServer && ((DedicatedServer)this.world.getMinecraftServer()).getMaxTickTime() > 0L) {
            try {
                final Method getThreads = Thread.class.getDeclaredMethod("getThreads", (Class<?>[])new Class[0]);
                getThreads.setAccessible(true);
                final Thread[] threads = (Thread[])getThreads.invoke(null, new Object[0]);
                final Field f = ReflectionUtil.getField(Thread.class, Runnable.class);
                for (final Thread thread : threads) {
                    final Object target;
                    if (thread.getClass() == Thread.class && (target = ReflectionUtil.getFieldValue(f, thread)) instanceof ServerHangWatchdog) {
                        ReflectionUtil.setValue(this.watchDog = target, ReflectionUtil.getField(ServerHangWatchdog.class, Long.TYPE), Long.MAX_VALUE);
                        break;
                    }
                }
            }
            catch (final ReflectiveOperationException e) {
                throw new RuntimeException("Error stopping Watchdog", e);
            }
        }
    }
    
    public void cleanup() {
        DimensionManager.setWorld(this.dimensionId, (WorldServer)null, this.parentWorld.getMinecraftServer());
        DimensionManager.unregisterDimension(this.dimensionId);
        deleteRecursive(this.tmpDir, false);
        if (this.watchDog != null) {
            this.fixWatchDog();
        }
    }
    
    private static void deleteRecursive(final File file, final boolean deleteFiles) {
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("no dir: " + file);
        }
        for (final File subFile : file.listFiles()) {
            if (subFile.isDirectory()) {
                deleteRecursive(subFile, deleteFiles);
            }
            else if (deleteFiles) {
                subFile.delete();
            }
        }
        file.delete();
    }
    
    private void fixWatchDog() {
        ReflectionUtil.setValue(this.watchDog, ReflectionUtil.getField(ServerHangWatchdog.class, Long.TYPE), ((DedicatedServer)this.world.getMinecraftServer()).getMaxTickTime());
        this.watchDog = null;
    }
    
    private void analyze() {
        final ItemComparableItemStack cobblestone = new ItemComparableItemStack(new ItemStack(Blocks.COBBLESTONE), false);
        final ItemComparableItemStack netherrack = new ItemComparableItemStack(new ItemStack(Blocks.NETHERRACK), false);
        double normalizeBy;
        if (!this.drops.containsKey(cobblestone)) {
            if (!this.drops.containsKey(netherrack)) {
                IC2.log.warn(LogCategory.Uu, "UU scan failed, there was no cobblestone or netherrack dropped");
                return;
            }
            normalizeBy = this.drops.get(netherrack).getValue();
        }
        else {
            normalizeBy = this.drops.get(cobblestone).getValue();
            if (this.drops.containsKey(netherrack)) {
                normalizeBy = Math.max(normalizeBy, this.drops.get(netherrack).getValue());
            }
        }
        Config config = MainConfig.get().getSub("balance/uu-values/world scan");
        if (config == null) {
            config = MainConfig.get().getSub("balance/uu-values").addSub("world scan", "Initial uu values from scanning the world.\nRun /ic2 uu-world-scan <small|medium|large> to calibrate them for your world.\nDelete this whole section to revert to the default predefined values.");
        }
        final List<Map.Entry<ItemComparableItemStack, MutableLong>> sorted = new ArrayList<Map.Entry<ItemComparableItemStack, MutableLong>>(this.drops.entrySet());
        this.drops.clear();
        Collections.sort(sorted, new Comparator<Map.Entry<ItemComparableItemStack, MutableLong>>() {
            @Override
            public int compare(final Map.Entry<ItemComparableItemStack, MutableLong> a, final Map.Entry<ItemComparableItemStack, MutableLong> b) {
                return Long.compare(b.getValue().getValue(), a.getValue().getValue());
            }
        });
        IC2.log.info(LogCategory.Uu, "total");
        for (final Map.Entry<ItemComparableItemStack, MutableLong> entry : sorted) {
            final ItemStack stack = entry.getKey().toStack();
            final long count = entry.getValue().getValue();
            IC2.log.info(LogCategory.Uu, "%d %s", count, stack.getItem().getItemStackDisplayName(stack));
            config.set(ConfigUtil.fromStack(stack), normalizeBy / count);
        }
        MainConfig.save();
    }
    
    private void scanArea(final int xStart, final int zStart) {
        final DummyChunkProvider provider = this.world.getChunkProvider();
        final List<Chunk> chunks = new ArrayList<Chunk>(Util.square(this.range));
        final List<Chunk> toDecorate = new ArrayList<Chunk>(Util.square(this.range - 1));
        final List<Chunk> toScan = new ArrayList<Chunk>(Util.square(this.range - 3));
        provider.enableGenerate();
        for (int x = xStart; x < xStart + this.range; ++x) {
            for (int z = zStart; z < zStart + this.range; ++z) {
                final Chunk chunk = this.world.getChunkFromChunkCoords(x, z);
                chunks.add(chunk);
                if (x != xStart + this.range - 1 && z != zStart + this.range - 1) {
                    toDecorate.add(chunk);
                    if (x != xStart && x != xStart + this.range - 2 && z != zStart && z != zStart + this.range - 2) {
                        toScan.add(chunk);
                    }
                }
            }
        }
        provider.setChunks(chunks, xStart, zStart);
        for (final Chunk chunk2 : toDecorate) {
            MinecraftForge.EVENT_BUS.post((Event)new ChunkEvent.Load(chunk2));
        }
        for (final Chunk chunk2 : toDecorate) {
            chunk2.populate((IChunkProvider)provider, provider.chunkGenerator);
        }
        provider.disableGenerate();
        for (final Chunk chunk2 : toScan) {
            this.scanChunk(this.world, chunk2);
        }
        for (final Chunk chunk2 : toDecorate) {
            MinecraftForge.EVENT_BUS.post((Event)new ChunkEvent.Unload(chunk2));
        }
        this.world.clear();
    }
    
    private void scanChunk(final DummyWorld world, final Chunk chunk) {
        assert world.getChunkFromChunkCoords(chunk.x, chunk.z) == chunk;
        final int xMax = (chunk.x + 1) * 16;
        final int yMax = world.getHeight();
        final int zMax = (chunk.z + 1) * 16;
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 0; y < yMax; ++y) {
            for (int z = chunk.z * 16; z < zMax; ++z) {
                for (int x = chunk.x * 16; x < xMax; ++x) {
                    pos.setPos(x, y, z);
                    final IBlockState state = chunk.getBlockState((BlockPos)pos);
                    final Block block = state.getBlock();
                    if (!block.isAir(state, (IBlockAccess)world, (BlockPos)pos)) {
                        for (final ItemStack drop : this.getDrops(world, (BlockPos)pos, block, state)) {
                            this.addDrop(drop);
                        }
                    }
                }
            }
        }
    }
    
    private List<ItemStack> getDrops(final DummyWorld world, final BlockPos pos, final Block block, final IBlockState state) {
        DropDesc typicalDrop = this.typicalDrops.get(state);
        if (typicalDrop == null || typicalDrop.dropCount.get() < 1000) {
            block.onBlockHarvested((World)world, pos, state, this.player);
            if (block.removedByPlayer(state, (World)world, pos, this.player, true)) {
                block.onBlockDestroyedByPlayer((World)world, pos, state);
                block.dropBlockAsItem((World)world, pos, state, 0);
            }
            else {
                IC2.log.info(LogCategory.Uu, "Can't harvest %s.", block);
            }
            final List<ItemStack> drops = new ArrayList<ItemStack>(world.spawnedEntities.size());
            for (final Entity entity : world.spawnedEntities) {
                if (entity instanceof EntityItem) {
                    drops.add(((EntityItem)entity).getItem());
                }
            }
            world.spawnedEntities.clear();
            if (typicalDrop == null) {
                typicalDrop = new DropDesc(drops);
                this.typicalDrops.put(state, typicalDrop);
            }
            if (typicalDrop.dropCount.get() >= 0) {
                boolean equal = typicalDrop.drops.size() == drops.size();
                if (equal) {
                    final Iterator<ItemStack> it = drops.iterator();
                    final Iterator<ItemStack> it2 = typicalDrop.drops.iterator();
                    while (it.hasNext()) {
                        final ItemStack a = it.next();
                        final ItemStack b = it2.next();
                        if (!ItemStack.areItemStacksEqual(a, b)) {
                            equal = false;
                            break;
                        }
                    }
                }
                if (equal) {
                    final int prev = typicalDrop.dropCount.incrementAndGet();
                    if (prev < 0) {
                        typicalDrop.dropCount.set(Integer.MIN_VALUE);
                    }
                }
                else {
                    typicalDrop.dropCount.set(Integer.MIN_VALUE);
                }
            }
            return drops;
        }
        return typicalDrop.drops;
    }
    
    private void addDrop(final ItemStack stack) {
        final ItemComparableItemStack key = new ItemComparableItemStack(stack, false);
        MutableLong amount = this.drops.get(key);
        if (amount == null) {
            amount = new MutableLong();
            this.drops.put(key.copy(), amount);
        }
        amount.add((long)StackUtil.getSize(stack));
    }
    
    static {
        WorldServer_pendingTickListEntriesHashSet = ReflectionUtil.getField(WorldServer.class, "pendingTickListEntriesHashSet", "pendingTickListEntriesHashSet");
        WorldServer_pendingTickListEntriesTreeSet = ReflectionUtil.getField(WorldServer.class, "pendingTickListEntriesTreeSet", "pendingTickListEntriesTreeSet");
    }
    
    class DummyWorld extends WorldServer
    {
        List<Entity> spawnedEntities;
        
        public DummyWorld() {
            super(DropScan.this.parentWorld.getMinecraftServer(), (ISaveHandler)new DummySaveHandler(), DropScan.this.parentWorld.getWorldInfo(), DropScan.this.dimensionId, DropScan.this.parentWorld.profiler);
            this.spawnedEntities = new ArrayList<Entity>();
            this.lootTable = DropScan.this.parentWorld.getLootTableManager();
        }
        
        protected IChunkProvider createChunkProvider() {
            return (IChunkProvider)new DummyChunkProvider(this, this.provider.createChunkGenerator());
        }
        
        public DummyChunkProvider getChunkProvider() {
            return (DummyChunkProvider)super.getChunkProvider();
        }
        
        public File getChunkSaveLocation() {
            return DropScan.this.tmpDir;
        }
        
        protected boolean isChunkLoaded(final int x, final int z, final boolean allowEmpty) {
            return this.getChunkProvider().getLoadedChunk(x, z) != null;
        }
        
        public Entity getEntityByID(final int i) {
            return null;
        }
        
        public boolean setBlockState(final BlockPos pos, final IBlockState state, final int flags) {
            if (pos.getY() >= 256 || pos.getY() < 0) {
                return false;
            }
            final Chunk chunk = this.getChunkFromChunkCoords(pos.getX() >> 4, pos.getZ() >> 4);
            return chunk.setBlockState(pos, state) != null;
        }
        
        public boolean checkLightFor(final EnumSkyBlock lightType, final BlockPos pos) {
            return true;
        }
        
        public void tick() {
        }
        
        public boolean spawnEntity(final Entity entity) {
            this.spawnedEntities.add(entity);
            return true;
        }
        
        public void clear() {
            this.getChunkProvider().clear();
            for (final Collection<?> c : DropScan.this.collectionsToClear) {
                c.clear();
            }
        }
    }
    
    private static class EmptyChunk extends Chunk
    {
        public EmptyChunk(final World world, final int x, final int z) {
            super(world, x, z);
        }
        
        public boolean isAtLocation(final int x, final int z) {
            return this.x == x && this.z == z;
        }
        
        public int getHeightValue(final int x, final int z) {
            return 0;
        }
        
        public void generateHeightMap() {
        }
        
        public void generateSkylightMap() {
        }
        
        public IBlockState getBlockState(final BlockPos pos) {
            return Blocks.AIR.getDefaultState();
        }
        
        public int getBlockLightOpacity(final BlockPos pos) {
            return 255;
        }
        
        public int getLightFor(final EnumSkyBlock sky, final BlockPos pos) {
            return sky.defaultLightValue;
        }
        
        public void setLightFor(final EnumSkyBlock sky, final BlockPos pos, final int value) {
        }
        
        public int getLightSubtracted(final BlockPos pos, final int amount) {
            return 0;
        }
        
        public void addEntity(final Entity entity) {
        }
        
        public void removeEntity(final Entity entity) {
        }
        
        public void removeEntityAtIndex(final Entity entity, final int index) {
        }
        
        public boolean canSeeSky(final BlockPos pos) {
            return false;
        }
        
        @Nullable
        public TileEntity getTileEntity(final BlockPos pos, final Chunk.EnumCreateEntityType createType) {
            return null;
        }
        
        public void addTileEntity(final TileEntity tileEntity) {
        }
        
        public void addTileEntity(final BlockPos pos, final TileEntity tileEntity) {
        }
        
        public void removeTileEntity(final BlockPos pos) {
        }
        
        public void onLoad() {
        }
        
        public void onUnload() {
        }
        
        public void markDirty() {
        }
        
        public void getEntitiesWithinAABBForEntity(@Nullable final Entity entity, final AxisAlignedBB aabb, final List<Entity> listToFill, final Predicate<? super Entity> valid) {
        }
        
        public <T extends Entity> void getEntitiesOfTypeWithinAABB(final Class<? extends T> entityClass, final AxisAlignedBB aabb, final List<T> listToFill, final Predicate<? super T> valid) {
        }
        
        public boolean needsSaving(final boolean flag) {
            return false;
        }
        
        public Random getRandomWithSeed(final long seed) {
            return new Random(this.getWorld().getSeed() + this.x * this.x * 4987142 + this.x * 5947611 + this.z * this.z * 4392871L + this.z * 389711 ^ seed);
        }
        
        public boolean isEmpty() {
            return true;
        }
        
        public boolean isEmptyBetween(final int startY, final int endY) {
            return true;
        }
    }
    
    class DummyChunkProvider extends ChunkProviderServer
    {
        private final Chunk emptyChunk;
        private final TLongObjectMap<Chunk> extraChunks;
        private final Chunk[] chunks;
        private int xStart;
        private int zStart;
        private boolean disableGenerate;
        
        public DummyChunkProvider(final WorldServer world, final IChunkGenerator chunkGenerator) {
            super(world, (IChunkLoader)null, chunkGenerator);
            this.extraChunks = (TLongObjectMap<Chunk>)new TLongObjectHashMap();
            this.emptyChunk = new EmptyChunk((World)world, 0, 0);
            this.chunks = new Chunk[Util.square(DropScan.this.range)];
        }
        
        public void setChunks(final List<Chunk> newChunks, final int xStart, final int zStart) {
            this.clear();
            this.xStart = xStart;
            this.zStart = zStart;
            for (final Chunk chunk : newChunks) {
                final int index = this.getIndex(chunk.x, chunk.z);
                if (index < 0) {
                    throw new IllegalArgumentException("out of range");
                }
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
            Arrays.fill(this.chunks, null);
        }
        
        public String makeString() {
            return "Dummy";
        }
        
        public Chunk getLoadedChunk(final int x, final int z) {
            final int index = this.getIndex(x, z);
            if (index >= 0) {
                return this.chunks[index];
            }
            return (Chunk)this.extraChunks.get(ChunkPos.asLong(x, z));
        }
        
        public Chunk provideChunk(final int x, final int z) {
            Chunk ret = this.getLoadedChunk(x, z);
            if (ret == null) {
                if (this.disableGenerate) {
                    return this.emptyChunk;
                }
                ret = this.chunkGenerator.generateChunk(x, z);
                final int index = this.getIndex(x, z);
                if (index >= 0) {
                    this.chunks[index] = ret;
                }
                else {
                    this.extraChunks.put(ChunkPos.asLong(x, z), (Object)ret);
                }
            }
            return ret;
        }
        
        public boolean saveChunks(final boolean all) {
            return true;
        }
        
        public void flushToDisk() {
        }
        
        public boolean tick() {
            return false;
        }
        
        private int getIndex(int x, int z) {
            x -= this.xStart;
            z -= this.zStart;
            if (x < 0 || x >= DropScan.this.range || z < 0 || z >= DropScan.this.range) {
                return -1;
            }
            return x * DropScan.this.range + z;
        }
    }
    
    private class DummySaveHandler implements ISaveHandler
    {
        private final TemplateManager templateManager;
        
        private DummySaveHandler() {
            this.templateManager = new TemplateManager(DropScan.this.tmpDir.toString(), new DataFixer(0));
        }
        
        public WorldInfo loadWorldInfo() {
            return DropScan.this.world.getWorldInfo();
        }
        
        public void checkSessionLock() throws MinecraftException {
        }
        
        public IChunkLoader getChunkLoader(final WorldProvider provider) {
            throw new UnsupportedOperationException();
        }
        
        public void saveWorldInfoWithPlayer(final WorldInfo worldInformation, final NBTTagCompound tagCompound) {
        }
        
        public void saveWorldInfo(final WorldInfo worldInformation) {
        }
        
        public IPlayerFileData getPlayerNBTManager() {
            throw new UnsupportedOperationException();
        }
        
        public void flush() {
        }
        
        public File getWorldDirectory() {
            throw new UnsupportedOperationException();
        }
        
        public File getMapFileFromName(final String mapName) {
            throw new UnsupportedOperationException();
        }
        
        public TemplateManager getStructureTemplateManager() {
            return this.templateManager;
        }
    }
    
    private static final class DropDesc
    {
        List<ItemStack> drops;
        AtomicInteger dropCount;
        
        DropDesc(final List<ItemStack> drops) {
            this.dropCount = new AtomicInteger();
            this.drops = drops;
        }
    }
}
