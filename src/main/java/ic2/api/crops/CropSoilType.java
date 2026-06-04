// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.crops;

import net.minecraft.init.Blocks;
import javax.annotation.Nonnull;
import net.minecraft.block.Block;

public enum CropSoilType
{
    FARMLAND(Blocks.FARMLAND), 
    MYCELIUM((Block)Blocks.MYCELIUM), 
    SAND((Block)Blocks.SAND), 
    SOULSAND(Blocks.SOUL_SAND);
    
    private final Block block;
    
    private CropSoilType(final Block block) {
        this.block = block;
    }
    
    public Block getBlock() {
        return this.block;
    }
    
    public static boolean contais(final Block block) {
        for (final CropSoilType aux : values()) {
            if (aux.getBlock() == block) {
                return true;
            }
        }
        return false;
    }
}
