package ic2.api.tile;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.block.Block;

public final class ExplosionWhitelist {
   private static Set<Block> whitelist = Collections.newSetFromMap(new IdentityHashMap<>());

   public static void addWhitelistedBlock(Block block) {
      whitelist.add(block);
   }

   public static void removeWhitelistedBlock(Block block) {
      whitelist.remove(block);
   }

   public static boolean isBlockWhitelisted(Block block) {
      return whitelist.contains(block);
   }
}
