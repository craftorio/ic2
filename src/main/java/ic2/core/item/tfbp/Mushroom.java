// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tfbp;

import net.minecraft.block.state.IBlockState;
import net.minecraft.block.BlockMushroom;
import ic2.core.util.BiomeUtil;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import ic2.core.block.machine.tileentity.TileEntityTerra;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class Mushroom extends TerraformerBase
{
    @Override
    boolean terraform(final World world, BlockPos pos) {
        pos = TileEntityTerra.getFirstSolidBlockFrom(world, pos, 20);
        return pos != null && growBlockWithDependancy(world, pos, Blocks.BROWN_MUSHROOM_BLOCK, (Block)Blocks.BROWN_MUSHROOM);
    }
    
    private static boolean growBlockWithDependancy(final World world, final BlockPos pos, final Block target, final Block dependancy) {
        final BlockPos.MutableBlockPos cPos = new BlockPos.MutableBlockPos();
        for (int xm = pos.getX() - 1; dependancy != null && xm < pos.getX() + 1; ++xm) {
            for (int zm = pos.getZ() - 1; zm < pos.getZ() + 1; ++zm) {
                for (int ym = pos.getY() + 5; ym > pos.getY() - 2; --ym) {
                    cPos.setPos(xm, ym, zm);
                    final IBlockState state = world.getBlockState((BlockPos)cPos);
                    final Block block = state.getBlock();
                    if (dependancy == Blocks.MYCELIUM) {
                        if (block == dependancy || block == Blocks.BROWN_MUSHROOM_BLOCK) {
                            break;
                        }
                        if (block == Blocks.RED_MUSHROOM_BLOCK) {
                            break;
                        }
                        if (!block.isAir(state, (IBlockAccess)world, (BlockPos)cPos)) {
                            if (block == Blocks.DIRT || block == Blocks.GRASS) {
                                final BlockPos dstPos = new BlockPos((Vec3i)cPos);
                                world.setBlockState(dstPos, dependancy.getDefaultState());
                                BiomeUtil.setBiome(world, dstPos, Biomes.MUSHROOM_ISLAND);
                                return true;
                            }
                        }
                    }
                    else if (dependancy == Blocks.BROWN_MUSHROOM) {
                        if (block == Blocks.BROWN_MUSHROOM) {
                            break;
                        }
                        if (block == Blocks.RED_MUSHROOM) {
                            break;
                        }
                        if (!block.isAir(state, (IBlockAccess)world, (BlockPos)cPos)) {
                            if (growBlockWithDependancy(world, (BlockPos)cPos, (Block)Blocks.BROWN_MUSHROOM, (Block)Blocks.MYCELIUM)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        if (target != Blocks.BROWN_MUSHROOM) {
            if (target == Blocks.BROWN_MUSHROOM_BLOCK) {
                final BlockPos above = pos.up();
                final IBlockState state2 = world.getBlockState(above);
                final Block base = state2.getBlock();
                if (base != Blocks.BROWN_MUSHROOM && base != Blocks.RED_MUSHROOM) {
                    return false;
                }
                if (((BlockMushroom)base).generateBigMushroom(world, above, state2, world.rand)) {
                    for (int xm2 = pos.getX() - 1; xm2 < pos.getX() + 1; ++xm2) {
                        for (int zm2 = pos.getZ() - 1; zm2 < pos.getZ() + 1; ++zm2) {
                            cPos.setPos(xm2, above.getY(), zm2);
                            final Block block2 = world.getBlockState((BlockPos)cPos).getBlock();
                            if (block2 == Blocks.BROWN_MUSHROOM || block2 == Blocks.RED_MUSHROOM) {
                                world.setBlockToAir(new BlockPos((Vec3i)cPos));
                            }
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        final Block base2 = world.getBlockState(pos).getBlock();
        if (base2 != Blocks.MYCELIUM) {
            if (base2 != Blocks.BROWN_MUSHROOM_BLOCK && base2 != Blocks.RED_MUSHROOM_BLOCK) {
                return false;
            }
            world.setBlockState(pos, Blocks.MYCELIUM.getDefaultState());
        }
        final BlockPos above2 = pos.up();
        final IBlockState state3 = world.getBlockState(above2);
        final Block block3 = state3.getBlock();
        if (!block3.isAir(state3, (IBlockAccess)world, above2) && block3 != Blocks.TALLGRASS) {
            return false;
        }
        final Block shroom = (Block)(world.rand.nextBoolean() ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM);
        world.setBlockState(above2, shroom.getDefaultState());
        return true;
    }
}
