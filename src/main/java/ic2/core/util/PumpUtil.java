// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import net.minecraft.block.Block;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraft.block.state.IBlockState;
import java.util.Set;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.math.Vec3i;
import java.util.HashSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PumpUtil
{
    private static int moveUp(final World world, final BlockPos.MutableBlockPos pos) {
        pos.setPos(pos.getX(), pos.getY() + 1, pos.getZ());
        int newDecay = getFlowDecay(world, (BlockPos)pos);
        if (newDecay >= 0) {
            return newDecay;
        }
        pos.setPos(pos.getX() + 1, pos.getY(), pos.getZ());
        newDecay = getFlowDecay(world, (BlockPos)pos);
        if (newDecay >= 0) {
            return newDecay;
        }
        pos.setPos(pos.getX() - 2, pos.getY(), pos.getZ());
        newDecay = getFlowDecay(world, (BlockPos)pos);
        if (newDecay >= 0) {
            return newDecay;
        }
        pos.setPos(pos.getX() + 1, pos.getY(), pos.getZ() + 1);
        newDecay = getFlowDecay(world, (BlockPos)pos);
        if (newDecay >= 0) {
            return newDecay;
        }
        pos.setPos(pos.getX(), pos.getY(), pos.getZ() - 2);
        newDecay = getFlowDecay(world, (BlockPos)pos);
        if (newDecay >= 0) {
            return newDecay;
        }
        pos.setPos(pos.getX(), pos.getY() - 1, pos.getZ() + 1);
        return -1;
    }
    
    private static int moveSideways(final World world, final BlockPos.MutableBlockPos pos, final int decay) {
        pos.setPos(pos.getX() - 1, pos.getY(), pos.getZ());
        int newDecay = getFlowDecay(world, (BlockPos)pos);
        if (newDecay >= 0 && newDecay < decay) {
            return newDecay;
        }
        pos.setPos(pos.getX() + 1, pos.getY(), pos.getZ() + 1);
        newDecay = getFlowDecay(world, (BlockPos)pos);
        if (newDecay >= 0 && newDecay < decay) {
            return newDecay;
        }
        pos.setPos(pos.getX(), pos.getY(), pos.getZ() - 2);
        newDecay = getFlowDecay(world, (BlockPos)pos);
        if (newDecay >= 0 && newDecay < decay) {
            return newDecay;
        }
        pos.setPos(pos.getX() + 1, pos.getY(), pos.getZ() + 1);
        newDecay = getFlowDecay(world, (BlockPos)pos);
        if (newDecay >= 0 && newDecay < decay) {
            return newDecay;
        }
        pos.setPos(pos.getX() - 1, pos.getY(), pos.getZ());
        return -1;
    }
    
    public static BlockPos searchFluidSource(final World world, final BlockPos startPos) {
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        pos.setPos(startPos.getX(), startPos.getY(), startPos.getZ());
        int decay = getFlowDecay(world, (BlockPos)pos);
        for (int i = 0; i < 64; ++i) {
            int newDecay = moveUp(world, pos);
            if (newDecay < 0) {
                newDecay = moveSideways(world, pos, decay);
                if (newDecay < 0) {
                    break;
                }
            }
            decay = newDecay;
        }
        final Set<BlockPos> visited = new HashSet<BlockPos>(64);
        for (int j = 0; j < 64; ++j) {
            visited.add(new BlockPos((Vec3i)pos));
            pos.setPos(pos.getX() - 1, pos.getY(), pos.getZ());
            if (!visited.contains(pos)) {
                final int newDecay2 = getFlowDecay(world, (BlockPos)pos);
                if (newDecay2 >= 0) {
                    if (newDecay2 == 0) {
                        return (BlockPos)pos;
                    }
                    continue;
                }
            }
            pos.setPos(pos.getX() + 1, pos.getY(), pos.getZ() + 1);
            if (!visited.contains(pos)) {
                final int newDecay2 = getFlowDecay(world, (BlockPos)pos);
                if (newDecay2 >= 0) {
                    if (newDecay2 == 0) {
                        return (BlockPos)pos;
                    }
                    continue;
                }
            }
            pos.setPos(pos.getX(), pos.getY(), pos.getZ() - 2);
            if (!visited.contains(pos)) {
                final int newDecay2 = getFlowDecay(world, (BlockPos)pos);
                if (newDecay2 >= 0) {
                    if (newDecay2 == 0) {
                        return (BlockPos)pos;
                    }
                    continue;
                }
            }
            pos.setPos(pos.getX() + 1, pos.getY(), pos.getZ() + 1);
            if (!visited.contains(pos)) {
                final int newDecay2 = getFlowDecay(world, (BlockPos)pos);
                if (newDecay2 >= 0) {
                    if (newDecay2 == 0) {
                        return (BlockPos)pos;
                    }
                    continue;
                }
            }
            pos.setPos(pos.getX() - 1, pos.getY(), pos.getZ());
            break;
        }
        final BlockPos.MutableBlockPos cPos = new BlockPos.MutableBlockPos();
        for (int ix = -2; ix <= 2; ++ix) {
            for (int iz = -2; iz <= 2; ++iz) {
                cPos.setPos(pos.getX() + ix, pos.getY(), pos.getZ() + iz);
                final IBlockState state = world.getBlockState((BlockPos)cPos);
                decay = getFlowDecay(state, world, (BlockPos)cPos);
                if (decay >= 0) {
                    if (decay == 0) {
                        return (BlockPos)cPos;
                    }
                    if (decay >= 1 && decay < 7 && state.getBlock() instanceof BlockLiquid) {
                        world.setBlockState((BlockPos)cPos, state.withProperty((IProperty)BlockLiquid.LEVEL, (Comparable)(decay + 1)));
                    }
                    else {
                        world.setBlockToAir((BlockPos)cPos);
                    }
                }
            }
        }
        return null;
    }
    
    protected static int getFlowDecay(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        return getFlowDecay(state, world, pos);
    }
    
    protected static int getFlowDecay(final IBlockState state, final World world, final BlockPos pos) {
        final Block block = state.getBlock();
        if (block instanceof IFluidBlock) {
            final IFluidBlock fb = (IFluidBlock)block;
            if (fb.canDrain(world, pos)) {
                return 0;
            }
            final float level = Math.abs(fb.getFilledPercentage(world, pos));
            return 7 - Util.limit(Math.round(6.0f * level), 0, 6);
        }
        else {
            if (block instanceof BlockLiquid) {
                return (int)state.getValue((IProperty)BlockLiquid.LEVEL);
            }
            return -1;
        }
    }
    
    protected static boolean isExistInArray(final int x, final int y, final int z, final int[][] xyz, final int end_i) {
        for (int i = 0; i <= end_i; ++i) {
            if (xyz[i][0] == x && xyz[i][1] == y && xyz[i][2] == z) {
                return true;
            }
        }
        return false;
    }
}
