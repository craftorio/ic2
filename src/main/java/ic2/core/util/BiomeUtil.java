// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import net.minecraftforge.common.BiomeDictionary;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BiomeUtil
{
    public static Biome getOriginalBiome(final World world, final BlockPos pos) {
        return world.getBiomeProvider().getBiome(pos, Biomes.PLAINS);
    }
    
    public static Biome getBiome(final World world, final BlockPos pos) {
        return world.getBiome(pos);
    }
    
    public static void setBiome(final World world, final BlockPos pos, final Biome biome) {
        final byte[] biomeArray = world.getChunkFromBlockCoords(pos).getBiomeArray();
        final int index = (pos.getZ() & 0xF) << 4 | (pos.getX() & 0xF);
        biomeArray[index] = (byte)Biome.getIdForBiome(biome);
    }
    
    public static int getBiomeTemperature(final World world, final BlockPos pos) {
        final Biome biome = getBiome(world, pos);
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.HOT)) {
            return 45;
        }
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD)) {
            return 0;
        }
        return 25;
    }
}
