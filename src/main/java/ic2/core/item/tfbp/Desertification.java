// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tfbp;

import java.util.Iterator;
import net.minecraft.block.state.IBlockState;
import ic2.core.ref.BlockName;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import ic2.core.block.machine.tileentity.TileEntityTerra;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class Desertification extends TerraformerBase
{
    @Override
    boolean terraform(final World world, BlockPos pos) {
        pos = TileEntityTerra.getFirstBlockFrom(world, pos, 10);
        if (pos == null) {
            return false;
        }
        final IBlockState sand = Blocks.SAND.getDefaultState();
        if (TileEntityTerra.switchGround(world, pos, Blocks.DIRT, sand, false) || TileEntityTerra.switchGround(world, pos, (Block)Blocks.GRASS, sand, false) || TileEntityTerra.switchGround(world, pos, Blocks.FARMLAND, sand, false)) {
            TileEntityTerra.switchGround(world, pos, Blocks.DIRT, sand, false);
            return true;
        }
        final Block block = world.getBlockState(pos).getBlock();
        if (block == Blocks.WATER || block == Blocks.FLOWING_WATER || block == Blocks.SNOW_LAYER || block == Blocks.LEAVES || block == Blocks.LEAVES2 || block == BlockName.leaves.getInstance() || isPlant(block)) {
            world.setBlockToAir(pos);
            if (isPlant(world.getBlockState(pos.up()).getBlock())) {
                world.setBlockToAir(pos.up());
            }
            return true;
        }
        if (block == Blocks.ICE || block == Blocks.SNOW) {
            world.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState());
            return true;
        }
        if ((block == Blocks.PLANKS || block == Blocks.LOG || block == BlockName.rubber_wood.getInstance()) && world.rand.nextInt(15) == 0) {
            world.setBlockState(pos, Blocks.FIRE.getDefaultState());
            return true;
        }
        return false;
    }
    
    private static boolean isPlant(final Block block) {
        for (final IBlockState state : Cultivation.plants) {
            if (state.getBlock() == block) {
                return true;
            }
        }
        return false;
    }
}
