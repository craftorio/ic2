// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import ic2.core.ref.BlockName;
import ic2.core.block.type.ResourceBlock;

public class BlockOre extends BlockMultiID<ResourceBlock>
{
    public static BlockOre create() {
        return BlockMultiID.create(BlockOre.class, ResourceBlock.class, new Object[0]);
    }
    
    public BlockOre() {
        super(BlockName.resource, Material.ROCK);
    }
    
    public int damageDropped(final IBlockState state) {
        return this.getMetaFromState(state);
    }
}
