// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.comp;

import net.minecraft.block.Block;
import ic2.core.block.TileEntityBlock;

public class ComparatorEmitter extends BasicRedstoneComponent
{
    public ComparatorEmitter(final TileEntityBlock parent) {
        super(parent);
    }
    
    @Override
    public void onChange() {
        this.parent.getWorld().updateComparatorOutputLevel(this.parent.getPos(), (Block)this.parent.getBlockType());
    }
}
