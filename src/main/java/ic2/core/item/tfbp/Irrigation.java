// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tfbp;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import ic2.core.block.machine.tileentity.TileEntityTerra;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class Irrigation extends TerraformerBase
{
    @Override
    boolean terraform(final World world, BlockPos pos) {
        if (world.rand.nextInt(48000) == 0) {
            world.getWorldInfo().setRaining(true);
            return true;
        }
        pos = TileEntityTerra.getFirstBlockFrom(world, pos, 10);
        if (pos == null) {
            return false;
        }
        if (TileEntityTerra.switchGround(world, pos, (Block)Blocks.SAND, Blocks.DIRT.getDefaultState(), true)) {
            TileEntityTerra.switchGround(world, pos, (Block)Blocks.SAND, Blocks.DIRT.getDefaultState(), true);
            return true;
        }
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        if (block instanceof IGrowable && ((IGrowable)block).canGrow(world, pos, state, false)) {
            ((IGrowable)block).grow(world, world.rand, pos, state);
            return true;
        }
        if (block == Blocks.TALLGRASS) {
            return spreadGrass(world, pos.north()) || spreadGrass(world, pos.east()) || spreadGrass(world, pos.south()) || spreadGrass(world, pos.west());
        }
        if (block == Blocks.LOG || block == Blocks.LOG2) {
            final BlockPos above = pos.up();
            world.setBlockState(above, state);
            final IBlockState leaves = getLeaves(world, pos);
            if (leaves != null) {
                createLeaves(world, above, leaves);
            }
            return true;
        }
        if (block == Blocks.FIRE) {
            world.setBlockToAir(pos);
            return true;
        }
        return false;
    }
    
    private static IBlockState getLeaves(final World world, final BlockPos pos) {
        for (final EnumFacing facing : EnumFacing.HORIZONTALS) {
            final BlockPos cPos = pos.offset(facing);
            final IBlockState state = world.getBlockState(cPos);
            if (state.getBlock().isLeaves(state, (IBlockAccess)world, cPos)) {
                return state;
            }
        }
        return null;
    }
    
    private static void createLeaves(final World world, final BlockPos pos, final IBlockState state) {
        final BlockPos above = pos.up();
        if (world.isAirBlock(above)) {
            world.setBlockState(above, state);
        }
        for (final EnumFacing facing : EnumFacing.HORIZONTALS) {
            final BlockPos cPos = pos.offset(facing);
            if (world.isAirBlock(cPos)) {
                world.setBlockState(cPos, state);
            }
        }
    }
    
    private static boolean spreadGrass(final World world, BlockPos pos) {
        if (world.rand.nextBoolean()) {
            return false;
        }
        pos = TileEntityTerra.getFirstBlockFrom(world, pos, 0);
        if (pos == null) {
            return false;
        }
        final Block block = world.getBlockState(pos).getBlock();
        if (block == Blocks.DIRT) {
            world.setBlockState(pos, Blocks.GRASS.getDefaultState());
            return true;
        }
        if (block == Blocks.GRASS) {
            world.setBlockState(pos.up(), Blocks.TALLGRASS.getDefaultState().withProperty((IProperty)BlockTallGrass.TYPE, (Comparable)BlockTallGrass.EnumType.GRASS));
            return true;
        }
        return false;
    }
}
