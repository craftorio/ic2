package ic2.core;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import ic2.core.block.WorldGenRubTree;
import ic2.core.block.state.IIdProvider;
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
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
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
  
  public Ic2WorldDecorator() {
    MinecraftForge.EVENT_BUS.register(this);
  }
  
  @SubscribeEvent
  public void onChunkLoad(ChunkDataEvent.Load event) {
    assert !(event.getWorld()).field_72995_K;
    Chunk chunk = event.getChunk();
    WorldData worldData = WorldData.get(event.getWorld());
    if (!worldData.pendingUnloadChunks.remove(chunk)) {
      NBTTagCompound nbt = event.getData().func_74775_l("ic2WorldGen");
      worldData.worldGenData.put(chunk, nbt);
      checkRetroGen(chunk, nbt);
    } 
  }
  
  private static void checkRetroGen(Chunk chunk, NBTTagCompound nbt) {
    if (!chunk.func_177419_t())
      return; 
    Config config = MainConfig.get().getSub("worldgen");
    if (getCheckLimit(config) <= 0 || getUpdateLimit(config) <= 0)
      return; 
    float epsilon = 1.0E-5F;
    float treeScale = getTreeScale(config) - epsilon;
    float oreScale = getOreScale(config, getBaseHeight(config, chunk.func_177412_p())) - epsilon;
    if (treeScale <= 0.0F && oreScale <= 0.0F)
      return; 
    if ((rubberTreeGenEnabled(config, chunk.func_177412_p()) && nbt.func_74760_g("rubberTree") < treeScale) || (
      ConfigUtil.getBool(config, "copper/enabled") && nbt.func_74760_g("copperOre") < oreScale) || (
      ConfigUtil.getBool(config, "lead/enabled") && nbt.func_74760_g("leadOre") < oreScale) || (
      ConfigUtil.getBool(config, "tin/enabled") && nbt.func_74760_g("tinOre") < oreScale) || (
      ConfigUtil.getBool(config, "uranium/enabled") && nbt.func_74760_g("uraniumOre") < oreScale))
      (WorldData.get(chunk.func_177412_p())).chunksToDecorate.add(chunk); 
  }
  
  @SubscribeEvent
  public void onChunkSave(ChunkDataEvent.Save event) {
    assert !(event.getWorld()).field_72995_K;
    Chunk chunk = event.getChunk();
    NBTTagCompound nbt = (WorldData.get(event.getWorld())).worldGenData.get(chunk);
    if (nbt != null && !nbt.func_82582_d()) {
      nbt = nbt.func_74737_b();
      event.getData().func_74782_a("ic2WorldGen", (NBTBase)nbt);
    } 
  }
  
  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onChunkUnload(ChunkEvent.Unload event) {
    if ((event.getWorld()).field_72995_K)
      return; 
    Chunk chunk = event.getChunk();
    WorldData worldData = WorldData.get(event.getWorld(), false);
    if (worldData == null)
      return; 
    worldData.pendingUnloadChunks.add(chunk);
  }
  
  private static void applyPendingUnloads(WorldData worldData) {
    Collection<Chunk> chunks = worldData.pendingUnloadChunks;
    if (chunks.isEmpty())
      return; 
    for (Chunk chunk : chunks) {
      worldData.worldGenData.remove(chunk);
      if (worldData.chunksToDecorate.remove(chunk));
    } 
    chunks.clear();
  }
  
  public static void onTick(World world, WorldData worldData) {
    applyPendingUnloads(worldData);
    if (worldData.chunksToDecorate.isEmpty())
      return; 
    Config config = MainConfig.get().getSub("worldgen");
    int chunksToCheck = getCheckLimit(config);
    int chunksToDecorate = getUpdateLimit(config);
    long worldSeed = world.func_72905_C();
    Random rnd = new Random(worldSeed);
    long xSeed = rnd.nextLong() >> 3L;
    long zSeed = rnd.nextLong() >> 3L;
    int baseHeight = getBaseHeight(config, world);
    int worldHeight = world.func_72800_K();
    float treeScale = getTreeScale(config);
    float oreScale = getOreScale(config, baseHeight);
    int skip = worldData.chunksToDecorate.size() - chunksToCheck;
    if (skip > 0)
      skip = IC2.random.nextInt(skip + 1); 
    Iterator<Chunk> it = worldData.chunksToDecorate.iterator();
    while (skip > 0) {
      skip--;
      it.next();
    } 
    while (it.hasNext()) {
      Chunk chunk = it.next();
      if (hasNeighborChunks(chunk)) {
        NBTTagCompound nbt = worldData.worldGenData.get(chunk);
        if (nbt == null)
          nbt = new NBTTagCompound(); 
        long chunkSeed = xSeed * chunk.field_76635_g + zSeed * chunk.field_76647_h ^ worldSeed;
        rnd.setSeed(chunkSeed);
        long rubberTreeSeed = rnd.nextLong();
        long copperOreSeed = rnd.nextLong();
        long tinOreSeed = rnd.nextLong();
        long uraniumOreSeed = rnd.nextLong();
        long leadOreSeed = rnd.nextLong();
        float extra;
        if (rubberTreeGenEnabled(config, world) && (extra = treeScale - nbt.func_74760_g("rubberTree")) > 0.0F)
          genRubberTree(rnd, rubberTreeSeed, chunk, extra); 
        if ((extra = oreScale - nbt.func_74760_g("copperOre")) > 0.0F)
          genOre(rnd, copperOreSeed, chunk, BlockName.resource.getBlockState((IIdProvider)ResourceBlock.copper_ore), "copperOre", config.getSub("copper"), baseHeight, worldHeight, extra); 
        if ((extra = oreScale - nbt.func_74760_g("leadOre")) > 0.0F)
          genOre(rnd, leadOreSeed, chunk, BlockName.resource.getBlockState((IIdProvider)ResourceBlock.lead_ore), "leadOre", config.getSub("lead"), baseHeight, worldHeight, extra); 
        if ((extra = oreScale - nbt.func_74760_g("tinOre")) > 0.0F)
          genOre(rnd, tinOreSeed, chunk, BlockName.resource.getBlockState((IIdProvider)ResourceBlock.tin_ore), "tinOre", config.getSub("tin"), baseHeight, worldHeight, extra); 
        if ((extra = oreScale - nbt.func_74760_g("uraniumOre")) > 0.0F)
          genOre(rnd, uraniumOreSeed, chunk, BlockName.resource.getBlockState((IIdProvider)ResourceBlock.uranium_ore), "uraniumOre", config.getSub("uranium"), baseHeight, worldHeight, extra); 
        it.remove();
        if (--chunksToDecorate == 0)
          break; 
      } 
      if (--chunksToCheck == 0)
        break; 
    } 
  }
  
  private static boolean hasNeighborChunks(Chunk chunk) {
    World world = chunk.func_177412_p();
    Ic2BlockPos pos = new Ic2BlockPos();
    for (int dx = 0; dx <= 1; dx++) {
      for (int dz = 0; dz <= 1; dz++) {
        if (dx != 0 || dz != 0) {
          pos.set((chunk.field_76635_g + dx) * 16, 0, (chunk.field_76647_h + dz) * 16);
          if (!world.func_175668_a((BlockPos)pos, false))
            return false; 
        } 
      } 
    } 
    return true;
  }
  
  public void generate(Random rnd, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
    Chunk chunk = chunkProvider.func_186025_d(chunkX, chunkZ);
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
    if (rubberTreeGenEnabled(config, world) && treeScale > 0.0F)
      genRubberTree(rnd, rubberTreeSeed, chunk, treeScale); 
    if (oreScale > 0.0F) {
      int worldHeight = world.func_72800_K();
      genOre(rnd, copperOreSeed, chunk, BlockName.resource.getBlockState((IIdProvider)ResourceBlock.copper_ore), "copperOre", config.getSub("copper"), baseHeight, worldHeight, oreScale);
      genOre(rnd, leadOreSeed, chunk, BlockName.resource.getBlockState((IIdProvider)ResourceBlock.lead_ore), "leadOre", config.getSub("lead"), baseHeight, worldHeight, oreScale);
      genOre(rnd, tinOreSeed, chunk, BlockName.resource.getBlockState((IIdProvider)ResourceBlock.tin_ore), "tinOre", config.getSub("tin"), baseHeight, worldHeight, oreScale);
      genOre(rnd, uraniumOreSeed, chunk, BlockName.resource.getBlockState((IIdProvider)ResourceBlock.uranium_ore), "uraniumOre", config.getSub("uranium"), baseHeight, worldHeight, oreScale);
    } 
  }
  
  private static void genRubberTree(Random rnd, long seed, Chunk chunk, float baseScale) {
    rnd.setSeed(seed);
    Biome[] biomes = new Biome[4];
    for (int i = 0; i < 4; i++) {
      int x = chunk.field_76635_g * 16 + 8 + (i & 0x1) * 15;
      int z = chunk.field_76647_h * 16 + 8 + ((i & 0x2) >>> 1) * 15;
      BlockPos pos = new BlockPos(x, chunk.func_177412_p().func_181545_F(), z);
      biomes[i] = BiomeUtil.getOriginalBiome(chunk.func_177412_p(), pos);
    } 
    int rubberTrees = 0;
    for (Biome biome : biomes) {
      if (biome != null) {
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP))
          rubberTrees += rnd.nextInt(10) + 5; 
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.FOREST) || BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE))
          rubberTrees += rnd.nextInt(5) + 1; 
      } 
    } 
    rubberTrees = Math.round(rubberTrees * baseScale);
    rubberTrees /= 2;
    if (rubberTrees > 0 && rnd.nextInt(100) < rubberTrees) {
      WorldGenRubTree gen = new WorldGenRubTree(false);
      for (int j = 0; j < rubberTrees; j++) {
        if (!gen.func_180709_b(chunk.func_177412_p(), rnd, new BlockPos(
              randomX(chunk, rnd), chunk
              .func_177412_p().func_181545_F(), 
              randomZ(chunk, rnd))))
          rubberTrees -= 3; 
      } 
    } 
    updateScale(chunk, "rubberTree", baseScale);
  }
  
  private static void genOre(Random rnd, long seed, Chunk chunk, IBlockState ore, String oreScaleKey, Config config, int baseHeight, int worldHeight, float baseScale) {
    if (!ConfigUtil.getBool(config, "enabled"))
      return; 
    rnd.setSeed(seed);
    int count = ConfigUtil.getInt(config, "count");
    int size = ConfigUtil.getInt(config, "size");
    int minHeight = ConfigUtil.getInt(config, "minHeight");
    int maxHeight = ConfigUtil.getInt(config, "maxHeight");
    OreDistribution distribution = OreDistribution.of(ConfigUtil.getString(config, "distribution"));
    float baseCount = count * baseScale / 64.0F;
    count = (int)Math.round(rnd.nextGaussian() * Math.sqrt(baseCount) + baseCount);
    minHeight = Util.limit(minHeight * baseHeight / 64, 0, worldHeight - 1);
    maxHeight = Util.limit(maxHeight * baseHeight / 64, minHeight + 1, worldHeight - 1);
    int heightSpan = maxHeight - minHeight;
    if (heightSpan == 0)
      return; 
    WorldGenMinable worldGenMinable = new WorldGenMinable(ore, size);
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    for (int n = 0; n < count; n++) {
      int halfHeightSpan, maxA, maxB, maxC, x = randomX(chunk, rnd);
      int z = randomZ(chunk, rnd);
      int y = minHeight;
      switch (distribution) {
        case UNIFORM:
          y += rnd.nextInt(heightSpan);
          break;
        case TRIANGLE:
          halfHeightSpan = heightSpan >>> 1;
          y += rnd.nextInt(halfHeightSpan + 1) + rnd.nextInt(heightSpan - halfHeightSpan);
          break;
        case RAMP:
          y += heightSpan - 1 - (int)Math.sqrt(rnd.nextInt(heightSpan * heightSpan));
          break;
        case REVRAMP:
          y += (int)Math.sqrt(rnd.nextInt(heightSpan * heightSpan));
          break;
        case SMOOTH:
          maxA = (heightSpan * 4 + 6) / 7;
          y += rnd.nextInt(maxA);
          maxB = ((heightSpan - maxA + 1) * 2 + 2) / 3;
          y += rnd.nextInt(maxB);
          maxC = heightSpan - maxA - maxB + 2;
          y += rnd.nextInt(maxC);
          break;
        default:
          throw new IllegalStateException();
      } 
      pos.func_181079_c(x, y, z);
      worldGenMinable.func_180709_b(chunk.func_177412_p(), rnd, (BlockPos)pos);
    } 
    updateScale(chunk, oreScaleKey, baseScale);
  }
  
  private static int getBaseHeight(Config config, World world) {
    if (ConfigUtil.getBool(config, "normalizeHeight"))
      return world.func_181545_F() + 1; 
    return 64;
  }
  
  private static boolean rubberTreeGenEnabled(Config config, World world) {
    return (ConfigUtil.getBool(config, "rubberTree") && !rubberTreeBlacklist.contains(world.field_73011_w.getDimension()));
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
    WorldData worldData = WorldData.get(chunk.func_177412_p());
    NBTTagCompound nbt = worldData.worldGenData.get(chunk);
    if (nbt == null) {
      nbt = new NBTTagCompound();
      worldData.worldGenData.put(chunk, nbt);
    } 
    nbt.func_74776_a(key, nbt.func_74760_g(key) + scale);
    chunk.func_177427_f(true);
  }
  
  private static int zeroRnd(Random rnd, int limit) {
    if (limit < 0)
      throw new IllegalArgumentException("The limit must not be negative: " + limit); 
    if (limit == 0)
      return 0; 
    return rnd.nextInt(limit);
  }
  
  private static int randomX(Chunk chunk, Random rnd) {
    return chunk.field_76635_g * 16 + rnd.nextInt(16);
  }
  
  private static int randomZ(Chunk chunk, Random rnd) {
    return chunk.field_76647_h * 16 + rnd.nextInt(16);
  }
  
  private enum OreDistribution {
    UNIFORM("uniform"),
    TRIANGLE("triangle"),
    RAMP("ramp"),
    REVRAMP("revramp"),
    SMOOTH("smooth");
    
    private static final OreDistribution[] values = values();
    
    final String name;
    
    OreDistribution(String name) {
      this.name = name;
    }
    
    public static OreDistribution of(String name) {
      for (OreDistribution value : values) {
        if (value.name.equalsIgnoreCase(name))
          return value; 
      } 
      throw new RuntimeException("Invalid/unknown worldgen distribution configured: " + name);
    }
    
    static {
    
    }
  }
  
  private static final TIntSet rubberTreeBlacklist = (TIntSet)new TIntHashSet(ConfigUtil.asIntArray(MainConfig.get(), "worldgen/rubberTreeBlacklist"));
}
