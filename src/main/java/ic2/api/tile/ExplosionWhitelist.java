// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.tile;

import java.util.Map;
import java.util.Collections;
import java.util.IdentityHashMap;
import net.minecraft.block.Block;
import java.util.Set;

public final class ExplosionWhitelist
{
    private static Set<Block> whitelist;
    
    public static void addWhitelistedBlock(final Block block) {
        ExplosionWhitelist.whitelist.add(block);
    }
    
    public static void removeWhitelistedBlock(final Block block) {
        ExplosionWhitelist.whitelist.remove(block);
    }
    
    public static boolean isBlockWhitelisted(final Block block) {
        return ExplosionWhitelist.whitelist.contains(block);
    }
    
    static {
        ExplosionWhitelist.whitelist = Collections.newSetFromMap(new IdentityHashMap<Block, Boolean>());
    }
}
