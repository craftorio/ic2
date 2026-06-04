// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import ic2.core.util.Util;
import net.minecraft.block.state.IBlockState;
import ic2.core.block.WorldGenRubTree;
import net.minecraftforge.common.BiomeDictionary;
import ic2.core.util.BiomeUtil;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.util.math.BlockPos;
import ic2.core.util.Ic2BlockPos;
import ic2.core.block.type.ResourceBlock;
import ic2.core.ref.BlockName;
import java.util.Random;
import net.minecraft.world.World;
import java.util.Iterator;
import java.util.Collection;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraft.nbt.NBTBase;
import ic2.core.util.Config;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.common.MinecraftForge;
import gnu.trove.set.TIntSet;
import net.minecraftforge.fml.common.IWorldGenerator;

public class Ic2WorldDecorator implements IWorldGenerator
{
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
    private static final TIntSet rubberTreeBlacklist;
    
    public Ic2WorldDecorator() {
        MinecraftForge.EVENT_BUS.register((Object)this);
    }
    
    @SubscribeEvent
    public void onChunkLoad(final ChunkDataEvent.Load event) {
        assert !event.getWorld().isRemote;
        final Chunk chunk = event.getChunk();
        final WorldData worldData = WorldData.get(event.getWorld());
        if (!worldData.pendingUnloadChunks.remove(chunk)) {
            final NBTTagCompound nbt = event.getData().getCompoundTag("ic2WorldGen");
            worldData.worldGenData.put(chunk, nbt);
            checkRetroGen(chunk, nbt);
        }
    }
    
    private static void checkRetroGen(final Chunk chunk, final NBTTagCompound nbt) {
        if (!chunk.isTerrainPopulated()) {
            return;
        }
        final Config config = MainConfig.get().getSub("worldgen");
        if (getCheckLimit(config) <= 0 || getUpdateLimit(config) <= 0) {
            return;
        }
        final float epsilon = 1.0E-5f;
        final float treeScale = getTreeScale(config) - epsilon;
        final float oreScale = getOreScale(config, getBaseHeight(config, chunk.getWorld())) - epsilon;
        if (treeScale <= 0.0f && oreScale <= 0.0f) {
            return;
        }
        if ((rubberTreeGenEnabled(config, chunk.getWorld()) && nbt.getFloat("rubberTree") < treeScale) || (ConfigUtil.getBool(config, "copper/enabled") && nbt.getFloat("copperOre") < oreScale) || (ConfigUtil.getBool(config, "lead/enabled") && nbt.getFloat("leadOre") < oreScale) || (ConfigUtil.getBool(config, "tin/enabled") && nbt.getFloat("tinOre") < oreScale) || (ConfigUtil.getBool(config, "uranium/enabled") && nbt.getFloat("uraniumOre") < oreScale)) {
            WorldData.get(chunk.getWorld()).chunksToDecorate.add(chunk);
        }
    }
    
    @SubscribeEvent
    public void onChunkSave(final ChunkDataEvent.Save event) {
        assert !event.getWorld().isRemote;
        final Chunk chunk = event.getChunk();
        NBTTagCompound nbt = WorldData.get(event.getWorld()).worldGenData.get(chunk);
        if (nbt != null && !nbt.hasNoTags()) {
            nbt = nbt.copy();
            event.getData().setTag("ic2WorldGen", (NBTBase)nbt);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChunkUnload(final ChunkEvent.Unload event) {
        if (event.getWorld().isRemote) {
            return;
        }
        final Chunk chunk = event.getChunk();
        final WorldData worldData = WorldData.get(event.getWorld(), false);
        if (worldData == null) {
            return;
        }
        worldData.pendingUnloadChunks.add(chunk);
    }
    
    private static void applyPendingUnloads(final WorldData worldData) {
        final Collection<Chunk> chunks = worldData.pendingUnloadChunks;
        if (chunks.isEmpty()) {
            return;
        }
        for (final Chunk chunk : chunks) {
            worldData.worldGenData.remove(chunk);
            if (worldData.chunksToDecorate.remove(chunk)) {}
        }
        chunks.clear();
    }
    
    public static void onTick(final World world, final WorldData worldData) {
        applyPendingUnloads(worldData);
        if (worldData.chunksToDecorate.isEmpty()) {
            return;
        }
        final Config config = MainConfig.get().getSub("worldgen");
        int chunksToCheck = getCheckLimit(config);
        int chunksToDecorate = getUpdateLimit(config);
        final long worldSeed = world.getSeed();
        final Random rnd = new Random(worldSeed);
        final long xSeed = rnd.nextLong() >> 3;
        final long zSeed = rnd.nextLong() >> 3;
        final int baseHeight = getBaseHeight(config, world);
        final int worldHeight = world.getHeight();
        final float treeScale = getTreeScale(config);
        final float oreScale = getOreScale(config, baseHeight);
        int skip = worldData.chunksToDecorate.size() - chunksToCheck;
        if (skip > 0) {
            skip = IC2.random.nextInt(skip + 1);
        }
        final Iterator<Chunk> it = worldData.chunksToDecorate.iterator();
        while (skip > 0) {
            --skip;
            it.next();
        }
        while (it.hasNext()) {
            final Chunk chunk = it.next();
            if (hasNeighborChunks(chunk)) {
                NBTTagCompound nbt = worldData.worldGenData.get(chunk);
                if (nbt == null) {
                    nbt = new NBTTagCompound();
                }
                final long chunkSeed = xSeed * chunk.x + zSeed * chunk.z ^ worldSeed;
                rnd.setSeed(chunkSeed);
                final long rubberTreeSeed = rnd.nextLong();
                final long copperOreSeed = rnd.nextLong();
                final long tinOreSeed = rnd.nextLong();
                final long uraniumOreSeed = rnd.nextLong();
                final long leadOreSeed = rnd.nextLong();
                float extra;
                if (rubberTreeGenEnabled(config, world) && (extra = treeScale - nbt.getFloat("rubberTree")) > 0.0f) {
                    genRubberTree(rnd, rubberTreeSeed, chunk, extra);
                }
                if ((extra = oreScale - nbt.getFloat("copperOre")) > 0.0f) {
                    genOre(rnd, copperOreSeed, chunk, BlockName.resource.getBlockState(ResourceBlock.copper_ore), "copperOre", config.getSub("copper"), baseHeight, worldHeight, extra);
                }
                if ((extra = oreScale - nbt.getFloat("leadOre")) > 0.0f) {
                    genOre(rnd, leadOreSeed, chunk, BlockName.resource.getBlockState(ResourceBlock.lead_ore), "leadOre", config.getSub("lead"), baseHeight, worldHeight, extra);
                }
                if ((extra = oreScale - nbt.getFloat("tinOre")) > 0.0f) {
                    genOre(rnd, tinOreSeed, chunk, BlockName.resource.getBlockState(ResourceBlock.tin_ore), "tinOre", config.getSub("tin"), baseHeight, worldHeight, extra);
                }
                if ((extra = oreScale - nbt.getFloat("uraniumOre")) > 0.0f) {
                    genOre(rnd, uraniumOreSeed, chunk, BlockName.resource.getBlockState(ResourceBlock.uranium_ore), "uraniumOre", config.getSub("uranium"), baseHeight, worldHeight, extra);
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
    
    private static boolean hasNeighborChunks(final Chunk chunk) {
        final World world = chunk.getWorld();
        final Ic2BlockPos pos = new Ic2BlockPos();
        for (int dx = 0; dx <= 1; ++dx) {
            for (int dz = 0; dz <= 1; ++dz) {
                if (dx != 0 || dz != 0) {
                    pos.set((chunk.x + dx) * 16, 0, (chunk.z + dz) * 16);
                    if (!world.isBlockLoaded((BlockPos)pos, false)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public void generate(final Random rnd, final int chunkX, final int chunkZ, final World world, final IChunkGenerator chunkGenerator, final IChunkProvider chunkProvider) {
        final Chunk chunk = chunkProvider.provideChunk(chunkX, chunkZ);
        assert hasNeighborChunks(chunk);
        final long rubberTreeSeed = rnd.nextLong();
        final long copperOreSeed = rnd.nextLong();
        final long tinOreSeed = rnd.nextLong();
        final long uraniumOreSeed = rnd.nextLong();
        final long leadOreSeed = rnd.nextLong();
        final Config config = MainConfig.get().getSub("worldgen");
        final int baseHeight = getBaseHeight(config, world);
        final float treeScale = getTreeScale(config);
        final float oreScale = getOreScale(config, baseHeight);
        if (rubberTreeGenEnabled(config, world) && treeScale > 0.0f) {
            genRubberTree(rnd, rubberTreeSeed, chunk, treeScale);
        }
        if (oreScale > 0.0f) {
            final int worldHeight = world.getHeight();
            genOre(rnd, copperOreSeed, chunk, BlockName.resource.getBlockState(ResourceBlock.copper_ore), "copperOre", config.getSub("copper"), baseHeight, worldHeight, oreScale);
            genOre(rnd, leadOreSeed, chunk, BlockName.resource.getBlockState(ResourceBlock.lead_ore), "leadOre", config.getSub("lead"), baseHeight, worldHeight, oreScale);
            genOre(rnd, tinOreSeed, chunk, BlockName.resource.getBlockState(ResourceBlock.tin_ore), "tinOre", config.getSub("tin"), baseHeight, worldHeight, oreScale);
            genOre(rnd, uraniumOreSeed, chunk, BlockName.resource.getBlockState(ResourceBlock.uranium_ore), "uraniumOre", config.getSub("uranium"), baseHeight, worldHeight, oreScale);
        }
    }
    
    private static void genRubberTree(final Random rnd, final long seed, final Chunk chunk, final float baseScale) {
        rnd.setSeed(seed);
        final Biome[] biomes = new Biome[4];
        for (int i = 0; i < 4; ++i) {
            final int x = chunk.x * 16 + 8 + (i & 0x1) * 15;
            final int z = chunk.z * 16 + 8 + ((i & 0x2) >>> 1) * 15;
            final BlockPos pos = new BlockPos(x, chunk.getWorld().getSeaLevel(), z);
            biomes[i] = BiomeUtil.getOriginalBiome(chunk.getWorld(), pos);
        }
        int rubberTrees = 0;
        for (final Biome biome : biomes) {
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
            final WorldGenRubTree gen = new WorldGenRubTree(false);
            for (int j = 0; j < rubberTrees; ++j) {
                if (!gen.generate(chunk.getWorld(), rnd, new BlockPos(randomX(chunk, rnd), chunk.getWorld().getSeaLevel(), randomZ(chunk, rnd)))) {
                    rubberTrees -= 3;
                }
            }
        }
        updateScale(chunk, "rubberTree", baseScale);
    }
    
    private static void genOre(final Random rnd, final long seed, final Chunk chunk, final IBlockState ore, final String oreScaleKey, final Config config, final int baseHeight, final int worldHeight, final float baseScale) {
        if (!ConfigUtil.getBool(config, "enabled")) {
            return;
        }
        rnd.setSeed(seed);
        int count = ConfigUtil.getInt(config, "count");
        final int size = ConfigUtil.getInt(config, "size");
        int minHeight = ConfigUtil.getInt(config, "minHeight");
        int maxHeight = ConfigUtil.getInt(config, "maxHeight");
        final OreDistribution distribution = OreDistribution.of(ConfigUtil.getString(config, "distribution"));
        final float baseCount = count * baseScale / 64.0f;
        count = (int)Math.round(rnd.nextGaussian() * Math.sqrt(baseCount) + baseCount);
        minHeight = Util.limit(minHeight * baseHeight / 64, 0, worldHeight - 1);
        maxHeight = Util.limit(maxHeight * baseHeight / 64, minHeight + 1, worldHeight - 1);
        final int heightSpan = maxHeight - minHeight;
        if (heightSpan == 0) {
            return;
        }
        final WorldGenerator gen = (WorldGenerator)new WorldGenMinable(ore, size);
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int n = 0; n < count; ++n) {
            final int x = randomX(chunk, rnd);
            final int z = randomZ(chunk, rnd);
            int y = minHeight;
            switch (distribution) {
                case UNIFORM: {
                    y += rnd.nextInt(heightSpan);
                    break;
                }
                case TRIANGLE: {
                    final int halfHeightSpan = heightSpan >>> 1;
                    y += rnd.nextInt(halfHeightSpan + 1) + rnd.nextInt(heightSpan - halfHeightSpan);
                    break;
                }
                case RAMP: {
                    y += heightSpan - 1 - (int)Math.sqrt(rnd.nextInt(heightSpan * heightSpan));
                    break;
                }
                case REVRAMP: {
                    y += (int)Math.sqrt(rnd.nextInt(heightSpan * heightSpan));
                    break;
                }
                case SMOOTH: {
                    final int maxA = (heightSpan * 4 + 6) / 7;
                    y += rnd.nextInt(maxA);
                    final int maxB = ((heightSpan - maxA + 1) * 2 + 2) / 3;
                    y += rnd.nextInt(maxB);
                    final int maxC = heightSpan - maxA - maxB + 2;
                    y += rnd.nextInt(maxC);
                    break;
                }
                default: {
                    throw new IllegalStateException();
                }
            }
            pos.setPos(x, y, z);
            gen.generate(chunk.getWorld(), rnd, (BlockPos)pos);
        }
        updateScale(chunk, oreScaleKey, baseScale);
    }
    
    private static int getBaseHeight(final Config config, final World world) {
        if (ConfigUtil.getBool(config, "normalizeHeight")) {
            return world.getSeaLevel() + 1;
        }
        return 64;
    }
    
    private static boolean rubberTreeGenEnabled(final Config config, final World world) {
        return ConfigUtil.getBool(config, "rubberTree") && !Ic2WorldDecorator.rubberTreeBlacklist.contains(world.provider.getDimension());
    }
    
    private static float getTreeScale(final Config config) {
        return ConfigUtil.getFloat(config, "treeDensityFactor");
    }
    
    private static float getOreScale(final Config config, final int baseHeight) {
        return ConfigUtil.getFloat(config, "oreDensityFactor") * baseHeight;
    }
    
    private static int getCheckLimit(final Config config) {
        return ConfigUtil.getInt(config, "retrogenCheckLimit");
    }
    
    private static int getUpdateLimit(final Config config) {
        return ConfigUtil.getInt(config, "retrogenUpdateLimit");
    }
    
    private static void updateScale(final Chunk chunk, final String key, final float scale) {
        final WorldData worldData = WorldData.get(chunk.getWorld());
        NBTTagCompound nbt = worldData.worldGenData.get(chunk);
        if (nbt == null) {
            nbt = new NBTTagCompound();
            worldData.worldGenData.put(chunk, nbt);
        }
        nbt.setFloat(key, nbt.getFloat(key) + scale);
        chunk.setModified(true);
    }
    
    private static int zeroRnd(final Random rnd, final int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("The limit must not be negative: " + limit);
        }
        if (limit == 0) {
            return 0;
        }
        return rnd.nextInt(limit);
    }
    
    private static int randomX(final Chunk chunk, final Random rnd) {
        return chunk.x * 16 + rnd.nextInt(16);
    }
    
    private static int randomZ(final Chunk chunk, final Random rnd) {
        return chunk.z * 16 + rnd.nextInt(16);
    }
    
    static {
        rubberTreeBlacklist = (TIntSet)new TIntHashSet(ConfigUtil.asIntArray(MainConfig.get(), "worldgen/rubberTreeBlacklist"));
    }
    
    private enum OreDistribution
    {
        UNIFORM("uniform"), 
        TRIANGLE("triangle"), 
        RAMP("ramp"), 
        REVRAMP("revramp"), 
        SMOOTH("smooth");
        
        private static final OreDistribution[] values;
        final String name;
        
        private OreDistribution(final String name) {
            this.name = name;
        }
        
        public static OreDistribution of(final String name) {
            for (final OreDistribution value : OreDistribution.values) {
                if (value.name.equalsIgnoreCase(name)) {
                    return value;
                }
            }
            throw new RuntimeException("Invalid/unknown worldgen distribution configured: " + name);
        }
        
        static {
            values = values();
        }
    }
}
