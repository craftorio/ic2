package ic2.core.util;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public final class BiomeUtil {
  public static Biome getOriginalBiome(World world, BlockPos pos) {
    return world.func_72959_q().func_180300_a(pos, Biomes.field_76772_c);
  }
  
  public static Biome getBiome(World world, BlockPos pos) {
    return world.func_180494_b(pos);
  }
  
  public static void setBiome(World world, BlockPos pos, Biome biome) {
    byte[] biomeArray = world.func_175726_f(pos).func_76605_m();
    int index = (pos.func_177952_p() & 0xF) << 4 | pos.func_177958_n() & 0xF;
    biomeArray[index] = (byte)Biome.func_185362_a(biome);
  }
  
  public static int getBiomeTemperature(World world, BlockPos pos) {
    Biome biome = getBiome(world, pos);
    if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.HOT))
      return 45; 
    if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD))
      return 0; 
    return 25;
  }
}
