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
import java.util.Map.Entry;
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
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
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
import org.apache.commons.lang3.mutable.MutableLong;

public class DropScan {
   private static final Field WorldServer_pendingTickListEntriesHashSet = ReflectionUtil.getField(
      WorldServer.class, "pendingTickListEntriesHashSet", "pendingTickListEntriesHashSet"
   );
   private static final Field WorldServer_pendingTickListEntriesTreeSet = ReflectionUtil.getField(
      WorldServer.class, "pendingTickListEntriesTreeSet", "pendingTickListEntriesTreeSet"
   );
   private final WorldServer parentWorld;
   private final int range;
   private final List<Collection<?>> collectionsToClear = new ArrayList<>();
   private final File tmpDir;
   private final int dimensionId;
   private final DropScan.DummyWorld world;
   private final EntityPlayer player;
   private Object watchDog;
   private final Map<ItemComparableItemStack, MutableLong> drops = new HashMap<>();
   private final Map<IBlockState, DropScan.DropDesc> typicalDrops = new IdentityHashMap<>();

   public DropScan(WorldServer parentWorld, int range) {
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
      } catch (IOException e) {
         throw new RuntimeException(e);
      }

      IC2.log.info(LogCategory.Uu, "Using %s for temporary data.", this.tmpDir);

      int id;
      do {
         id = parentWorld.rand.nextInt();
      } while (DimensionManager.getWorld(id) != null);

      this.dimensionId = id;
      DimensionManager.registerDimension(this.dimensionId, parentWorld.provider.getDimensionType());
      this.world = new DropScan.DummyWorld();
      this.player = Ic2Player.get(this.world);
      this.updateCollectionsToClear();
   }

   private void updateCollectionsToClear() {
      this.collectionsToClear.add(ReflectionUtil.getFieldValue(WorldServer_pendingTickListEntriesHashSet, this.world));
      this.collectionsToClear.add(ReflectionUtil.getFieldValue(WorldServer_pendingTickListEntriesTreeSet, this.world));
      this.collectionsToClear.add(this.world.loadedEntityList);
      this.collectionsToClear.add(this.world.loadedTileEntityList);
      this.collectionsToClear.add(this.world.tickableTileEntities);
   }

   public void start(int area, int areaCount) {
      if (FMLCommonHandler.instance().getSide().isServer()) {
         this.stopWatchDog();
      }

      long lastPrint = 0L;

      for (int i = 0; i < areaCount; i++) {
         int x = IC2.random.nextInt(area) - area / 2;
         int z = IC2.random.nextInt(area) - area / 2;

         try {
            this.scanArea(x, z);
         } catch (Exception e) {
            IC2.log.warn(LogCategory.Uu, e, "Scan failed.");
         }

         if (i % 4 == 0 && lastPrint <= System.nanoTime() - 10000000000L) {
            lastPrint = System.nanoTime();
            IC2.log.info(LogCategory.Uu, "World scan progress: %.1f%%.", 100.0F * i / areaCount);
         }
      }

      this.analyze();
   }

   private void stopWatchDog() {
      if (this.world.getMinecraftServer() instanceof DedicatedServer && ((DedicatedServer)this.world.getMinecraftServer()).getMaxTickTime() > 0L) {
         try {
            Method getThreads = Thread.class.getDeclaredMethod("getThreads");
            getThreads.setAccessible(true);
            Thread[] threads = (Thread[])getThreads.invoke(null);
            Field f = ReflectionUtil.getField(Thread.class, Runnable.class);

            for (Thread thread : threads) {
               Object target;
               if (thread.getClass() == Thread.class && (target = ReflectionUtil.getFieldValue(f, thread)) instanceof ServerHangWatchdog) {
                  ReflectionUtil.setValue(this.watchDog = target, ReflectionUtil.getField(ServerHangWatchdog.class, long.class), Long.MAX_VALUE);
                  break;
               }
            }
         } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Error stopping Watchdog", e);
         }
      }
   }

   public void cleanup() {
      DimensionManager.setWorld(this.dimensionId, null, this.parentWorld.getMinecraftServer());
      DimensionManager.unregisterDimension(this.dimensionId);
      deleteRecursive(this.tmpDir, false);
      if (this.watchDog != null) {
         this.fixWatchDog();
      }
   }

   private static void deleteRecursive(File file, boolean deleteFiles) {
      if (!file.isDirectory()) {
         throw new IllegalArgumentException("no dir: " + file);
      }

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
      ReflectionUtil.setValue(
         this.watchDog, ReflectionUtil.getField(ServerHangWatchdog.class, long.class), ((DedicatedServer)this.world.getMinecraftServer()).getMaxTickTime()
      );
      this.watchDog = null;
   }

   private void analyze() {
      ItemComparableItemStack cobblestone = new ItemComparableItemStack(new ItemStack(Blocks.COBBLESTONE), false);
      ItemComparableItemStack netherrack = new ItemComparableItemStack(new ItemStack(Blocks.NETHERRACK), false);
      double normalizeBy;
      if (!this.drops.containsKey(cobblestone)) {
         if (!this.drops.containsKey(netherrack)) {
            IC2.log.warn(LogCategory.Uu, "UU scan failed, there was no cobblestone or netherrack dropped");
            return;
         }

         normalizeBy = this.drops.get(netherrack).getValue().longValue();
      } else {
         normalizeBy = this.drops.get(cobblestone).getValue().longValue();
         if (this.drops.containsKey(netherrack)) {
            normalizeBy = Math.max(normalizeBy, this.drops.get(netherrack).getValue().longValue());
         }
      }

      Config config = MainConfig.get().getSub("balance/uu-values/world scan");
      if (config == null) {
         config = MainConfig.get()
            .getSub("balance/uu-values")
            .addSub(
               "world scan",
               "Initial uu values from scanning the world.\nRun /ic2 uu-world-scan <small|medium|large> to calibrate them for your world.\nDelete this whole section to revert to the default predefined values."
            );
      }

      List<Entry<ItemComparableItemStack, MutableLong>> sorted = new ArrayList<>(this.drops.entrySet());
      this.drops.clear();
      Collections.sort(sorted, new Comparator<Entry<ItemComparableItemStack, MutableLong>>() {
         public int compare(Entry<ItemComparableItemStack, MutableLong> a, Entry<ItemComparableItemStack, MutableLong> b) {
            return Long.compare(b.getValue().getValue(), a.getValue().getValue());
         }
      });
      IC2.log.info(LogCategory.Uu, "total");

      for (Entry<ItemComparableItemStack, MutableLong> entry : sorted) {
         ItemStack stack = entry.getKey().toStack();
         long count = entry.getValue().getValue();
         IC2.log.info(LogCategory.Uu, "%d %s", count, stack.getItem().getItemStackDisplayName(stack));
         config.set(ConfigUtil.fromStack(stack), normalizeBy / count);
      }

      MainConfig.save();
   }

   private void scanArea(int xStart, int zStart) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.RuntimeException: Constructor net/minecraftforge/event/world/ChunkEvent$Load.<init>(Lnet/minecraft/world/chunk/Chunk;)V not found
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.ExprUtil.getSyntheticParametersMask(ExprUtil.java:49)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.appendParamList(InvocationExprent.java:982)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.NewExprent.toJava(NewExprent.java:462)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.getCastedExprent(ExprProcessor.java:1054)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.appendParamList(InvocationExprent.java:1151)
      //   at org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent.toJava(InvocationExprent.java:921)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.listToJava(ExprProcessor.java:925)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement.toJava(BasicBlockStatement.java:87)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:860)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.DoStatement.toJava(DoStatement.java:149)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.jmpWrapper(ExprProcessor.java:860)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement.toJava(SequenceStatement.java:107)
      //   at org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement.toJava(RootStatement.java:36)
      //   at org.jetbrains.java.decompiler.main.ClassWriter.writeMethod(ClassWriter.java:1351)
      //
      // Bytecode:
      // 000: aload 0
      // 001: getfield ic2/core/uu/DropScan.world Lic2/core/uu/DropScan$DummyWorld;
      // 004: invokevirtual ic2/core/uu/DropScan$DummyWorld.getChunkProvider ()Lic2/core/uu/DropScan$DummyChunkProvider;
      // 007: astore 3
      // 008: new java/util/ArrayList
      // 00b: dup
      // 00c: aload 0
      // 00d: getfield ic2/core/uu/DropScan.range I
      // 010: invokestatic ic2/core/util/Util.square (I)I
      // 013: invokespecial java/util/ArrayList.<init> (I)V
      // 016: astore 4
      // 018: new java/util/ArrayList
      // 01b: dup
      // 01c: aload 0
      // 01d: getfield ic2/core/uu/DropScan.range I
      // 020: bipush 1
      // 021: isub
      // 022: invokestatic ic2/core/util/Util.square (I)I
      // 025: invokespecial java/util/ArrayList.<init> (I)V
      // 028: astore 5
      // 02a: new java/util/ArrayList
      // 02d: dup
      // 02e: aload 0
      // 02f: getfield ic2/core/uu/DropScan.range I
      // 032: bipush 3
      // 033: isub
      // 034: invokestatic ic2/core/util/Util.square (I)I
      // 037: invokespecial java/util/ArrayList.<init> (I)V
      // 03a: astore 6
      // 03c: aload 3
      // 03d: invokevirtual ic2/core/uu/DropScan$DummyChunkProvider.enableGenerate ()V
      // 040: iload 1
      // 041: istore 7
      // 043: iload 7
      // 045: iload 1
      // 046: aload 0
      // 047: getfield ic2/core/uu/DropScan.range I
      // 04a: iadd
      // 04b: if_icmpge 0d3
      // 04e: iload 2
      // 04f: istore 8
      // 051: iload 8
      // 053: iload 2
      // 054: aload 0
      // 055: getfield ic2/core/uu/DropScan.range I
      // 058: iadd
      // 059: if_icmpge 0cd
      // 05c: aload 0
      // 05d: getfield ic2/core/uu/DropScan.world Lic2/core/uu/DropScan$DummyWorld;
      // 060: iload 7
      // 062: iload 8
      // 064: invokevirtual ic2/core/uu/DropScan$DummyWorld.getChunkFromChunkCoords (II)Lnet/minecraft/world/chunk/Chunk;
      // 067: astore 9
      // 069: aload 4
      // 06b: aload 9
      // 06d: invokeinterface java/util/List.add (Ljava/lang/Object;)Z 2
      // 072: pop
      // 073: iload 7
      // 075: iload 1
      // 076: aload 0
      // 077: getfield ic2/core/uu/DropScan.range I
      // 07a: iadd
      // 07b: bipush 1
      // 07c: isub
      // 07d: if_icmpeq 0c7
      // 080: iload 8
      // 082: iload 2
      // 083: aload 0
      // 084: getfield ic2/core/uu/DropScan.range I
      // 087: iadd
      // 088: bipush 1
      // 089: isub
      // 08a: if_icmpeq 0c7
      // 08d: aload 5
      // 08f: aload 9
      // 091: invokeinterface java/util/List.add (Ljava/lang/Object;)Z 2
      // 096: pop
      // 097: iload 7
      // 099: iload 1
      // 09a: if_icmpeq 0c7
      // 09d: iload 7
      // 09f: iload 1
      // 0a0: aload 0
      // 0a1: getfield ic2/core/uu/DropScan.range I
      // 0a4: iadd
      // 0a5: bipush 2
      // 0a6: isub
      // 0a7: if_icmpeq 0c7
      // 0aa: iload 8
      // 0ac: iload 2
      // 0ad: if_icmpeq 0c7
      // 0b0: iload 8
      // 0b2: iload 2
      // 0b3: aload 0
      // 0b4: getfield ic2/core/uu/DropScan.range I
      // 0b7: iadd
      // 0b8: bipush 2
      // 0b9: isub
      // 0ba: if_icmpeq 0c7
      // 0bd: aload 6
      // 0bf: aload 9
      // 0c1: invokeinterface java/util/List.add (Ljava/lang/Object;)Z 2
      // 0c6: pop
      // 0c7: iinc 8 1
      // 0ca: goto 051
      // 0cd: iinc 7 1
      // 0d0: goto 043
      // 0d3: aload 3
      // 0d4: aload 4
      // 0d6: iload 1
      // 0d7: iload 2
      // 0d8: invokevirtual ic2/core/uu/DropScan$DummyChunkProvider.setChunks (Ljava/util/List;II)V
      // 0db: aload 5
      // 0dd: invokeinterface java/util/List.iterator ()Ljava/util/Iterator; 1
      // 0e2: astore 7
      // 0e4: aload 7
      // 0e6: invokeinterface java/util/Iterator.hasNext ()Z 1
      // 0eb: ifeq 10d
      // 0ee: aload 7
      // 0f0: invokeinterface java/util/Iterator.next ()Ljava/lang/Object; 1
      // 0f5: checkcast net/minecraft/world/chunk/Chunk
      // 0f8: astore 8
      // 0fa: getstatic net/minecraftforge/common/MinecraftForge.EVENT_BUS Lnet/minecraftforge/fml/common/eventhandler/EventBus;
      // 0fd: new net/minecraftforge/event/world/ChunkEvent$Load
      // 100: dup
      // 101: aload 8
      // 103: invokespecial net/minecraftforge/event/world/ChunkEvent$Load.<init> (Lnet/minecraft/world/chunk/Chunk;)V
      // 106: invokevirtual net/minecraftforge/fml/common/eventhandler/EventBus.post (Lnet/minecraftforge/fml/common/eventhandler/Event;)Z
      // 109: pop
      // 10a: goto 0e4
      // 10d: aload 5
      // 10f: invokeinterface java/util/List.iterator ()Ljava/util/Iterator; 1
      // 114: astore 7
      // 116: aload 7
      // 118: invokeinterface java/util/Iterator.hasNext ()Z 1
      // 11d: ifeq 139
      // 120: aload 7
      // 122: invokeinterface java/util/Iterator.next ()Ljava/lang/Object; 1
      // 127: checkcast net/minecraft/world/chunk/Chunk
      // 12a: astore 8
      // 12c: aload 8
      // 12e: aload 3
      // 12f: aload 3
      // 130: getfield ic2/core/uu/DropScan$DummyChunkProvider.chunkGenerator Lnet/minecraft/world/gen/IChunkGenerator;
      // 133: invokevirtual net/minecraft/world/chunk/Chunk.populate (Lnet/minecraft/world/chunk/IChunkProvider;Lnet/minecraft/world/gen/IChunkGenerator;)V
      // 136: goto 116
      // 139: aload 3
      // 13a: invokevirtual ic2/core/uu/DropScan$DummyChunkProvider.disableGenerate ()V
      // 13d: aload 6
      // 13f: invokeinterface java/util/List.iterator ()Ljava/util/Iterator; 1
      // 144: astore 7
      // 146: aload 7
      // 148: invokeinterface java/util/Iterator.hasNext ()Z 1
      // 14d: ifeq 169
      // 150: aload 7
      // 152: invokeinterface java/util/Iterator.next ()Ljava/lang/Object; 1
      // 157: checkcast net/minecraft/world/chunk/Chunk
      // 15a: astore 8
      // 15c: aload 0
      // 15d: aload 0
      // 15e: getfield ic2/core/uu/DropScan.world Lic2/core/uu/DropScan$DummyWorld;
      // 161: aload 8
      // 163: invokespecial ic2/core/uu/DropScan.scanChunk (Lic2/core/uu/DropScan$DummyWorld;Lnet/minecraft/world/chunk/Chunk;)V
      // 166: goto 146
      // 169: aload 5
      // 16b: invokeinterface java/util/List.iterator ()Ljava/util/Iterator; 1
      // 170: astore 7
      // 172: aload 7
      // 174: invokeinterface java/util/Iterator.hasNext ()Z 1
      // 179: ifeq 19b
      // 17c: aload 7
      // 17e: invokeinterface java/util/Iterator.next ()Ljava/lang/Object; 1
      // 183: checkcast net/minecraft/world/chunk/Chunk
      // 186: astore 8
      // 188: getstatic net/minecraftforge/common/MinecraftForge.EVENT_BUS Lnet/minecraftforge/fml/common/eventhandler/EventBus;
      // 18b: new net/minecraftforge/event/world/ChunkEvent$Unload
      // 18e: dup
      // 18f: aload 8
      // 191: invokespecial net/minecraftforge/event/world/ChunkEvent$Unload.<init> (Lnet/minecraft/world/chunk/Chunk;)V
      // 194: invokevirtual net/minecraftforge/fml/common/eventhandler/EventBus.post (Lnet/minecraftforge/fml/common/eventhandler/Event;)Z
      // 197: pop
      // 198: goto 172
      // 19b: aload 0
      // 19c: getfield ic2/core/uu/DropScan.world Lic2/core/uu/DropScan$DummyWorld;
      // 19f: invokevirtual ic2/core/uu/DropScan$DummyWorld.clear ()V
      // 1a2: return
   }

   private void scanChunk(DropScan.DummyWorld world, Chunk chunk) {
      assert world.getChunkFromChunkCoords(chunk.x, chunk.z) == chunk;
      int xMax = (chunk.x + 1) * 16;
      int yMax = world.getHeight();
      int zMax = (chunk.z + 1) * 16;
      MutableBlockPos pos = new MutableBlockPos();

      for (int y = 0; y < yMax; y++) {
         for (int z = chunk.z * 16; z < zMax; z++) {
            for (int x = chunk.x * 16; x < xMax; x++) {
               pos.setPos(x, y, z);
               IBlockState state = chunk.getBlockState(pos);
               Block block = state.getBlock();
               if (!block.isAir(state, world, pos)) {
                  for (ItemStack drop : this.getDrops(world, pos, block, state)) {
                     this.addDrop(drop);
                  }
               }
            }
         }
      }
   }

   private List<ItemStack> getDrops(DropScan.DummyWorld world, BlockPos pos, Block block, IBlockState state) {
      DropScan.DropDesc typicalDrop = this.typicalDrops.get(state);
      if (typicalDrop != null && typicalDrop.dropCount.get() >= 1000) {
         return typicalDrop.drops;
      }

      block.onBlockHarvested(world, pos, state, this.player);
      if (block.removedByPlayer(state, world, pos, this.player, true)) {
         block.onBlockDestroyedByPlayer(world, pos, state);
         block.dropBlockAsItem(world, pos, state, 0);
      } else {
         IC2.log.info(LogCategory.Uu, "Can't harvest %s.", block);
      }

      List<ItemStack> drops = new ArrayList<>(world.spawnedEntities.size());

      for (Entity entity : world.spawnedEntities) {
         if (entity instanceof EntityItem) {
            drops.add(((EntityItem)entity).getItem());
         }
      }

      world.spawnedEntities.clear();
      if (typicalDrop == null) {
         typicalDrop = new DropScan.DropDesc(drops);
         this.typicalDrops.put(state, typicalDrop);
      }

      if (typicalDrop.dropCount.get() >= 0) {
         boolean equal = typicalDrop.drops.size() == drops.size();
         if (equal) {
            Iterator<ItemStack> it = drops.iterator();
            Iterator<ItemStack> it2 = typicalDrop.drops.iterator();

            while (it.hasNext()) {
               ItemStack a = it.next();
               ItemStack b = it2.next();
               if (!ItemStack.areItemStacksEqual(a, b)) {
                  equal = false;
                  break;
               }
            }
         }

         if (equal) {
            int prev = typicalDrop.dropCount.incrementAndGet();
            if (prev < 0) {
               typicalDrop.dropCount.set(Integer.MIN_VALUE);
            }
         } else {
            typicalDrop.dropCount.set(Integer.MIN_VALUE);
         }
      }

      return drops;
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

   private static final class DropDesc {
      List<ItemStack> drops;
      AtomicInteger dropCount = new AtomicInteger();

      DropDesc(List<ItemStack> drops) {
         this.drops = drops;
      }
   }

   class DummyChunkProvider extends ChunkProviderServer {
      private final Chunk emptyChunk;
      private final TLongObjectMap<Chunk> extraChunks = new TLongObjectHashMap();
      private final Chunk[] chunks;
      private int xStart;
      private int zStart;
      private boolean disableGenerate;

      public DummyChunkProvider(WorldServer world, IChunkGenerator chunkGenerator) {
         super(world, null, chunkGenerator);
         this.emptyChunk = new DropScan.EmptyChunk(world, 0, 0);
         this.chunks = new Chunk[Util.square(DropScan.this.range)];
      }

      public void setChunks(List<Chunk> newChunks, int xStart, int zStart) {
         this.clear();
         this.xStart = xStart;
         this.zStart = zStart;

         for (Chunk chunk : newChunks) {
            int index = this.getIndex(chunk.x, chunk.z);
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

      public Chunk getLoadedChunk(int x, int z) {
         int index = this.getIndex(x, z);
         return index >= 0 ? this.chunks[index] : (Chunk)this.extraChunks.get(ChunkPos.asLong(x, z));
      }

      public Chunk provideChunk(int x, int z) {
         Chunk ret = this.getLoadedChunk(x, z);
         if (ret == null) {
            if (this.disableGenerate) {
               return this.emptyChunk;
            }

            ret = this.chunkGenerator.generateChunk(x, z);
            int index = this.getIndex(x, z);
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

      public void flushToDisk() {
      }

      public boolean tick() {
         return false;
      }

      private int getIndex(int x, int z) {
         x -= this.xStart;
         z -= this.zStart;
         return x >= 0 && x < DropScan.this.range && z >= 0 && z < DropScan.this.range ? x * DropScan.this.range + z : -1;
      }
   }

   private class DummySaveHandler implements ISaveHandler {
      private final TemplateManager templateManager = new TemplateManager(DropScan.this.tmpDir.toString(), new DataFixer(0));

      private DummySaveHandler() {
      }

      public WorldInfo loadWorldInfo() {
         return DropScan.this.world.getWorldInfo();
      }

      public void checkSessionLock() throws MinecraftException {
      }

      public IChunkLoader getChunkLoader(WorldProvider provider) {
         throw new UnsupportedOperationException();
      }

      public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {
      }

      public void saveWorldInfo(WorldInfo worldInformation) {
      }

      public IPlayerFileData getPlayerNBTManager() {
         throw new UnsupportedOperationException();
      }

      public void flush() {
      }

      public File getWorldDirectory() {
         throw new UnsupportedOperationException();
      }

      public File getMapFileFromName(String mapName) {
         throw new UnsupportedOperationException();
      }

      public TemplateManager getStructureTemplateManager() {
         return this.templateManager;
      }
   }

   class DummyWorld extends WorldServer {
      List<Entity> spawnedEntities = new ArrayList<>();

      public DummyWorld() {
         super(
            DropScan.this.parentWorld.getMinecraftServer(),
            DropScan.this.new DummySaveHandler(),
            DropScan.this.parentWorld.getWorldInfo(),
            DropScan.this.dimensionId,
            DropScan.this.parentWorld.profiler
         );
         this.lootTable = DropScan.this.parentWorld.getLootTableManager();
      }

      protected IChunkProvider createChunkProvider() {
         return DropScan.this.new DummyChunkProvider(this, this.provider.createChunkGenerator());
      }

      public DropScan.DummyChunkProvider getChunkProvider() {
         return (DropScan.DummyChunkProvider)super.getChunkProvider();
      }

      public File getChunkSaveLocation() {
         return DropScan.this.tmpDir;
      }

      protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
         return this.getChunkProvider().getLoadedChunk(x, z) != null;
      }

      public Entity getEntityByID(int i) {
         return null;
      }

      public boolean setBlockState(BlockPos pos, IBlockState state, int flags) {
         if (pos.getY() < 256 && pos.getY() >= 0) {
            Chunk chunk = this.getChunkFromChunkCoords(pos.getX() >> 4, pos.getZ() >> 4);
            return chunk.setBlockState(pos, state) != null;
         } else {
            return false;
         }
      }

      public boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos) {
         return true;
      }

      public void tick() {
      }

      public boolean spawnEntity(Entity entity) {
         this.spawnedEntities.add(entity);
         return true;
      }

      public void clear() {
         this.getChunkProvider().clear();

         for (Collection<?> c : DropScan.this.collectionsToClear) {
            c.clear();
         }
      }
   }

   private static class EmptyChunk extends Chunk {
      public EmptyChunk(World world, int x, int z) {
         super(world, x, z);
      }

      public boolean isAtLocation(int x, int z) {
         return this.x == x && this.z == z;
      }

      public int getHeightValue(int x, int z) {
         return 0;
      }

      public void generateHeightMap() {
      }

      public void generateSkylightMap() {
      }

      public IBlockState getBlockState(BlockPos pos) {
         return Blocks.AIR.getDefaultState();
      }

      public int getBlockLightOpacity(BlockPos pos) {
         return 255;
      }

      public int getLightFor(EnumSkyBlock sky, BlockPos pos) {
         return sky.defaultLightValue;
      }

      public void setLightFor(EnumSkyBlock sky, BlockPos pos, int value) {
      }

      public int getLightSubtracted(BlockPos pos, int amount) {
         return 0;
      }

      public void addEntity(Entity entity) {
      }

      public void removeEntity(Entity entity) {
      }

      public void removeEntityAtIndex(Entity entity, int index) {
      }

      public boolean canSeeSky(BlockPos pos) {
         return false;
      }

      @Nullable
      public TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType createType) {
         return null;
      }

      public void addTileEntity(TileEntity tileEntity) {
      }

      public void addTileEntity(BlockPos pos, TileEntity tileEntity) {
      }

      public void removeTileEntity(BlockPos pos) {
      }

      public void onLoad() {
      }

      public void onUnload() {
      }

      public void markDirty() {
      }

      public void getEntitiesWithinAABBForEntity(@Nullable Entity entity, AxisAlignedBB aabb, List<Entity> listToFill, Predicate<? super Entity> valid) {
      }

      public <T extends Entity> void getEntitiesOfTypeWithinAABB(Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill, Predicate<? super T> valid) {
      }

      public boolean needsSaving(boolean flag) {
         return false;
      }

      public Random getRandomWithSeed(long seed) {
         return new Random(
            this.getWorld().getSeed()
                  + this.x * this.x * 4987142
                  + this.x * 5947611
                  + this.z * this.z * 4392871L
                  + this.z * 389711
               ^ seed
         );
      }

      public boolean isEmpty() {
         return true;
      }

      public boolean isEmptyBetween(int startY, int endY) {
         return true;
      }
   }
}
