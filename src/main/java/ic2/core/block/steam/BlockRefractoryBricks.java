// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.steam;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.material.Material;
import ic2.core.ref.BlockName;
import ic2.core.block.BlockBase;

public class BlockRefractoryBricks extends BlockBase
{
    public BlockRefractoryBricks() {
        super(BlockName.refractory_bricks, Material.ROCK);
        this.setHardness(2.0f);
        this.setResistance(10.0f);
        this.setHarvestLevel("pickaxe", 0);
    }
    
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer((Block)this, new IProperty[0]);
    }
    
    public boolean isFlammable(final IBlockAccess world, final BlockPos pos, final EnumFacing face) {
        return false;
    }
}
