package ic2.core.util;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public final class BiomeUtil {
   public static Biome getOriginalBiome(World world, BlockPos pos) {
      return world.getBiomeProvider().getBiome(pos, Biomes.PLAINS);
   }

   public static Biome getBiome(World world, BlockPos pos) {
      return world.getBiome(pos);
   }

   public static void setBiome(World world, BlockPos pos, Biome biome) {
      byte[] biomeArray = world.getChunkFromBlockCoords(pos).getBiomeArray();
      int index = (pos.getZ() & 15) << 4 | pos.getX() & 15;
      biomeArray[index] = (byte)Biome.getIdForBiome(biome);
   }

   public static int getBiomeTemperature(World world, BlockPos pos) {
      Biome biome = getBiome(world, pos);
      if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.HOT)) {
         return 45;
      } else {
         return BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD) ? 0 : 25;
      }
   }
}
