package ic2.core;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import ic2.core.block.WorldGenRubTree;
import ic2.core.block.type.ResourceBlock;
import ic2.core.init.MainConfig;
import ic2.core.ref.BlockName;
import ic2.core.util.BiomeUtil;
import ic2.core.util.Config;
import ic2.core.util.ConfigUtil;
import ic2.core.util.Ic2BlockPos;
import ic2.core.util.Util;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Ic2WorldDecorator implements IWorldGenerator {
   private static final boolean DEBUG = false;
   private static final String chunkDataTag = "ic2WorldGen";
   private static final String keyRubberTree = "rubberTree";
   private static final String keyCopperOre = "copperOre";
   private static final String keyLeadOre = "leadOre";
   private static final String keyTinOre = "tinOre";
   private static final String keyUraniumOre = "uraniumOre";
   public static final int chunkSize = 16;
   public static final int chunkOffset = 8;
   private static final int referenceHeight = 64;
   private static final TIntSet rubberTreeBlacklist = new TIntHashSet(ConfigUtil.asIntArray(MainConfig.get(), "worldgen/rubberTreeBlacklist"));

   public Ic2WorldDecorator() {
      MinecraftForge.EVENT_BUS.register(this);
   }

   @SubscribeEvent
   public void onChunkLoad(ChunkDataEvent.Load event) {
      assert !event.getWorld().isRemote;
      Chunk chunk = event.getChunk();
      WorldData worldData = WorldData.get(event.getWorld());
      if (!worldData.pendingUnloadChunks.remove(chunk)) {
         NBTTagCompound nbt = event.getData().getCompoundTag("ic2WorldGen");
         worldData.worldGenData.put(chunk, nbt);
         checkRetroGen(chunk, nbt);
      }
   }

   private static void checkRetroGen(Chunk chunk, NBTTagCompound nbt) {
      if (chunk.isTerrainPopulated()) {
         Config config = MainConfig.get().getSub("worldgen");
         if (getCheckLimit(config) > 0 && getUpdateLimit(config) > 0) {
            float epsilon = 1.0E-5F;
            float treeScale = getTreeScale(config) - epsilon;
            float oreScale = getOreScale(config, getBaseHeight(config, chunk.getWorld())) - epsilon;
            if (!(treeScale <= 0.0F) || !(oreScale <= 0.0F)) {
               if (rubberTreeGenEnabled(config, chunk.getWorld()) && nbt.getFloat("rubberTree") < treeScale
                  || ConfigUtil.getBool(config, "copper/enabled") && nbt.getFloat("copperOre") < oreScale
                  || ConfigUtil.getBool(config, "lead/enabled") && nbt.getFloat("leadOre") < oreScale
                  || ConfigUtil.getBool(config, "tin/enabled") && nbt.getFloat("tinOre") < oreScale
                  || ConfigUtil.getBool(config, "uranium/enabled") && nbt.getFloat("uraniumOre") < oreScale) {
                  WorldData.get(chunk.getWorld()).chunksToDecorate.add(chunk);
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onChunkSave(ChunkDataEvent.Save event) {
      assert !event.getWorld().isRemote;
      Chunk chunk = event.getChunk();
      NBTTagCompound nbt = WorldData.get(event.getWorld()).worldGenData.get(chunk);
      if (nbt != null && !nbt.hasNoTags()) {
         nbt = nbt.copy();
         event.getData().setTag("ic2WorldGen", nbt);
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void onChunkUnload(ChunkEvent.Unload event) {
      if (!event.getWorld().isRemote) {
         Chunk chunk = event.getChunk();
         WorldData worldData = WorldData.get(event.getWorld(), false);
         if (worldData != null) {
            worldData.pendingUnloadChunks.add(chunk);
         }
      }
   }

   private static void applyPendingUnloads(WorldData worldData) {
      Collection<Chunk> chunks = worldData.pendingUnloadChunks;
      if (!chunks.isEmpty()) {
         for (Chunk chunk : chunks) {
            worldData.worldGenData.remove(chunk);
            if (worldData.chunksToDecorate.remove(chunk)) {
            }
         }

         chunks.clear();
      }
   }

   public static void onTick(World world, WorldData worldData) {
      applyPendingUnloads(worldData);
      if (!worldData.chunksToDecorate.isEmpty()) {
         Config config = MainConfig.get().getSub("worldgen");
         int chunksToCheck = getCheckLimit(config);
         int chunksToDecorate = getUpdateLimit(config);
         long worldSeed = world.getSeed();
         Random rnd = new Random(worldSeed);
         long xSeed = rnd.nextLong() >> 3;
         long zSeed = rnd.nextLong() >> 3;
         int baseHeight = getBaseHeight(config, world);
         int worldHeight = world.getHeight();
         float treeScale = getTreeScale(config);
         float oreScale = getOreScale(config, baseHeight);
         int skip = worldData.chunksToDecorate.size() - chunksToCheck;
         if (skip > 0) {
            skip = IC2.random.nextInt(skip + 1);
         }

         Iterator<Chunk> it = worldData.chunksToDecorate.iterator();

         while (skip > 0) {
            skip--;
            it.next();
         }

         while (it.hasNext()) {
            Chunk chunk = it.next();
            if (hasNeighborChunks(chunk)) {
               NBTTagCompound nbt = worldData.worldGenData.get(chunk);
               if (nbt == null) {
                  nbt = new NBTTagCompound();
               }

               long chunkSeed = xSeed * chunk.x + zSeed * chunk.z ^ worldSeed;
               rnd.setSeed(chunkSeed);
               long rubberTreeSeed = rnd.nextLong();
               long copperOreSeed = rnd.nextLong();
               long tinOreSeed = rnd.nextLong();
               long uraniumOreSeed = rnd.nextLong();
               long leadOreSeed = rnd.nextLong();
               float extra;
               if (rubberTreeGenEnabled(config, world) && (extra = treeScale - nbt.getFloat("rubberTree")) > 0.0F) {
                  genRubberTree(rnd, rubberTreeSeed, chunk, extra);
               }

               if ((extra = oreScale - nbt.getFloat("copperOre")) > 0.0F) {
                  genOre(
                     rnd,
                     copperOreSeed,
                     chunk,
                     BlockName.resource.getBlockState(ResourceBlock.copper_ore),
                     "copperOre",
                     config.getSub("copper"),
                     baseHeight,
                     worldHeight,
                     extra
                  );
               }

               if ((extra = oreScale - nbt.getFloat("leadOre")) > 0.0F) {
                  genOre(
                     rnd,
                     leadOreSeed,
                     chunk,
                     BlockName.resource.getBlockState(ResourceBlock.lead_ore),
                     "leadOre",
                     config.getSub("lead"),
                     baseHeight,
                     worldHeight,
                     extra
                  );
               }

               if ((extra = oreScale - nbt.getFloat("tinOre")) > 0.0F) {
                  genOre(
                     rnd,
                     tinOreSeed,
                     chunk,
                     BlockName.resource.getBlockState(ResourceBlock.tin_ore),
                     "tinOre",
                     config.getSub("tin"),
                     baseHeight,
                     worldHeight,
                     extra
                  );
               }

               if ((extra = oreScale - nbt.getFloat("uraniumOre")) > 0.0F) {
                  genOre(
                     rnd,
                     uraniumOreSeed,
                     chunk,
                     BlockName.resource.getBlockState(ResourceBlock.uranium_ore),
                     "uraniumOre",
                     config.getSub("uranium"),
                     baseHeight,
                     worldHeight,
                     extra
                  );
               }

               it.remove();
               if (--chunksToDecorate == 0) {
                  break;
               }
            }

            if (--chunksToCheck == 0) {
               break;
            }
         }
      }
   }

   private static boolean hasNeighborChunks(Chunk chunk) {
      World world = chunk.getWorld();
      Ic2BlockPos pos = new Ic2BlockPos();

      for (int dx = 0; dx <= 1; dx++) {
         for (int dz = 0; dz <= 1; dz++) {
            if (dx != 0 || dz != 0) {
               pos.set((chunk.x + dx) * 16, 0, (chunk.z + dz) * 16);
               if (!world.isBlockLoaded(pos, false)) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   public void generate(Random rnd, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
      Chunk chunk = chunkProvider.provideChunk(chunkX, chunkZ);
      assert hasNeighborChunks(chunk);
      long rubberTreeSeed = rnd.nextLong();
      long copperOreSeed = rnd.nextLong();
      long tinOreSeed = rnd.nextLong();
      long uraniumOreSeed = rnd.nextLong();
      long leadOreSeed = rnd.nextLong();
      Config config = MainConfig.get().getSub("worldgen");
      int baseHeight = getBaseHeight(config, world);
      float treeScale = getTreeScale(config);
      float oreScale = getOreScale(config, baseHeight);
      if (rubberTreeGenEnabled(config, world) && treeScale > 0.0F) {
         genRubberTree(rnd, rubberTreeSeed, chunk, treeScale);
      }

      if (oreScale > 0.0F) {
         int worldHeight = world.getHeight();
         genOre(
            rnd,
            copperOreSeed,
            chunk,
            BlockName.resource.getBlockState(ResourceBlock.copper_ore),
            "copperOre",
            config.getSub("copper"),
            baseHeight,
            worldHeight,
            oreScale
         );
         genOre(
            rnd,
            leadOreSeed,
            chunk,
            BlockName.resource.getBlockState(ResourceBlock.lead_ore),
            "leadOre",
            config.getSub("lead"),
            baseHeight,
            worldHeight,
            oreScale
         );
         genOre(
            rnd, tinOreSeed, chunk, BlockName.resource.getBlockState(ResourceBlock.tin_ore), "tinOre", config.getSub("tin"), baseHeight, worldHeight, oreScale
         );
         genOre(
            rnd,
            uraniumOreSeed,
            chunk,
            BlockName.resource.getBlockState(ResourceBlock.uranium_ore),
            "uraniumOre",
            config.getSub("uranium"),
            baseHeight,
            worldHeight,
            oreScale
         );
      }
   }

   private static void genRubberTree(Random rnd, long seed, Chunk chunk, float baseScale) {
      rnd.setSeed(seed);
      Biome[] biomes = new Biome[4];

      for (int i = 0; i < 4; i++) {
         int x = chunk.x * 16 + 8 + (i & 1) * 15;
         int z = chunk.z * 16 + 8 + ((i & 2) >>> 1) * 15;
         BlockPos pos = new BlockPos(x, chunk.getWorld().getSeaLevel(), z);
         biomes[i] = BiomeUtil.getOriginalBiome(chunk.getWorld(), pos);
      }

      int rubberTrees = 0;

      for (Biome biome : biomes) {
         if (biome != null) {
            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP)) {
               rubberTrees += rnd.nextInt(10) + 5;
            }

            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.FOREST) || BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)) {
               rubberTrees += rnd.nextInt(5) + 1;
            }
         }
      }

      rubberTrees = Math.round(rubberTrees * baseScale);
      rubberTrees /= 2;
      if (rubberTrees > 0 && rnd.nextInt(100) < rubberTrees) {
         WorldGenRubTree gen = new WorldGenRubTree(false);

         for (int i = 0; i < rubberTrees; i++) {
            if (!gen.generate(chunk.getWorld(), rnd, new BlockPos(randomX(chunk, rnd), chunk.getWorld().getSeaLevel(), randomZ(chunk, rnd)))) {
               rubberTrees -= 3;
            }
         }
      }

      updateScale(chunk, "rubberTree", baseScale);
   }

   private static void genOre(
      Random rnd, long seed, Chunk chunk, IBlockState ore, String oreScaleKey, Config config, int baseHeight, int worldHeight, float baseScale
   ) {
      if (ConfigUtil.getBool(config, "enabled")) {
         rnd.setSeed(seed);
         int count = ConfigUtil.getInt(config, "count");
         int size = ConfigUtil.getInt(config, "size");
         int minHeight = ConfigUtil.getInt(config, "minHeight");
         int maxHeight = ConfigUtil.getInt(config, "maxHeight");
         Ic2WorldDecorator.OreDistribution distribution = Ic2WorldDecorator.OreDistribution.of(ConfigUtil.getString(config, "distribution"));
         float baseCount = count * baseScale / 64.0F;
         count = (int)Math.round(rnd.nextGaussian() * Math.sqrt(baseCount) + baseCount);
         minHeight = Util.limit(minHeight * baseHeight / 64, 0, worldHeight - 1);
         maxHeight = Util.limit(maxHeight * baseHeight / 64, minHeight + 1, worldHeight - 1);
         int heightSpan = maxHeight - minHeight;
         if (heightSpan != 0) {
            WorldGenerator gen = new WorldGenMinable(ore, size);
            MutableBlockPos pos = new MutableBlockPos();

            for (int n = 0; n < count; n++) {
               int x = randomX(chunk, rnd);
               int z = randomZ(chunk, rnd);
               int y = minHeight;
               switch (distribution) {
                  case UNIFORM:
                     y += rnd.nextInt(heightSpan);
                     break;
                  case TRIANGLE:
                     int halfHeightSpan = heightSpan >>> 1;
                     y += rnd.nextInt(halfHeightSpan + 1) + rnd.nextInt(heightSpan - halfHeightSpan);
                     break;
                  case RAMP:
                     y += heightSpan - 1 - (int)Math.sqrt(rnd.nextInt(heightSpan * heightSpan));
                     break;
                  case REVRAMP:
                     y += (int)Math.sqrt(rnd.nextInt(heightSpan * heightSpan));
                     break;
                  case SMOOTH:
                     int maxA = (heightSpan * 4 + 6) / 7;
                     y += rnd.nextInt(maxA);
                     int maxB = ((heightSpan - maxA + 1) * 2 + 2) / 3;
                     y += rnd.nextInt(maxB);
                     int maxC = heightSpan - maxA - maxB + 2;
                     y += rnd.nextInt(maxC);
                     break;
                  default:
                     throw new IllegalStateException();
               }

               pos.setPos(x, y, z);
               gen.generate(chunk.getWorld(), rnd, pos);
            }

            updateScale(chunk, oreScaleKey, baseScale);
         }
      }
   }

   private static int getBaseHeight(Config config, World world) {
      return ConfigUtil.getBool(config, "normalizeHeight") ? world.getSeaLevel() + 1 : 64;
   }

   private static boolean rubberTreeGenEnabled(Config config, World world) {
      return ConfigUtil.getBool(config, "rubberTree") && !rubberTreeBlacklist.contains(world.provider.getDimension());
   }

   private static float getTreeScale(Config config) {
      return ConfigUtil.getFloat(config, "treeDensityFactor");
   }

   private static float getOreScale(Config config, int baseHeight) {
      return ConfigUtil.getFloat(config, "oreDensityFactor") * baseHeight;
   }

   private static int getCheckLimit(Config config) {
      return ConfigUtil.getInt(config, "retrogenCheckLimit");
   }

   private static int getUpdateLimit(Config config) {
      return ConfigUtil.getInt(config, "retrogenUpdateLimit");
   }

   private static void updateScale(Chunk chunk, String key, float scale) {
      WorldData worldData = WorldData.get(chunk.getWorld());
      NBTTagCompound nbt = worldData.worldGenData.get(chunk);
      if (nbt == null) {
         nbt = new NBTTagCompound();
         worldData.worldGenData.put(chunk, nbt);
      }

      nbt.setFloat(key, nbt.getFloat(key) + scale);
      chunk.setModified(true);
   }

   private static int zeroRnd(Random rnd, int limit) {
      if (limit < 0) {
         throw new IllegalArgumentException("The limit must not be negative: " + limit);
      } else {
         return limit == 0 ? 0 : rnd.nextInt(limit);
      }
   }

   private static int randomX(Chunk chunk, Random rnd) {
      return chunk.x * 16 + rnd.nextInt(16);
   }

   private static int randomZ(Chunk chunk, Random rnd) {
      return chunk.z * 16 + rnd.nextInt(16);
   }

   private enum OreDistribution {
      UNIFORM("uniform"),
      TRIANGLE("triangle"),
      RAMP("ramp"),
      REVRAMP("revramp"),
      SMOOTH("smooth");

      private static final Ic2WorldDecorator.OreDistribution[] values = values();
      final String name;

      OreDistribution(String name) {
         this.name = name;
      }

      public static Ic2WorldDecorator.OreDistribution of(String name) {
         for (Ic2WorldDecorator.OreDistribution value : values) {
            if (value.name.equalsIgnoreCase(name)) {
               return value;
            }
         }

         throw new RuntimeException("Invalid/unknown worldgen distribution configured: " + name);
      }
   }
}
