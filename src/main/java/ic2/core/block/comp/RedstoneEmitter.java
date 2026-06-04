// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.comp;

import net.minecraft.block.Block;
import ic2.core.block.TileEntityBlock;

public class RedstoneEmitter extends BasicRedstoneComponent
{
    public RedstoneEmitter(final TileEntityBlock parent) {
        super(parent);
    }
    
    @Override
    public void onChange() {
        this.parent.getWorld().notifyNeighborsOfStateChange(this.parent.getPos(), (Block)this.parent.getBlockType(), false);
    }
}
