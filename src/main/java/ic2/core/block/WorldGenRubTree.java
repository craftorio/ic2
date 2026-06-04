// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraftforge.common.IPlantable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.properties.IProperty;
import ic2.core.ref.BlockName;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import net.minecraft.util.math.BlockPos;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenRubTree extends WorldGenerator
{
    public static final int maxHeight = 8;
    
    public WorldGenRubTree(final boolean notify) {
        super(notify);
    }
    
    public boolean generate(final World world, final Random random, final BlockPos pos) {
        final BlockPos.MutableBlockPos cPos = new BlockPos.MutableBlockPos();
        cPos.setPos(pos.getX() + 8, IC2.getWorldHeight(world) - 1, pos.getZ() + 8);
        while (world.isAirBlock((BlockPos)cPos) && cPos.getY() > 0) {
            cPos.setPos(cPos.getX(), cPos.getY() - 1, cPos.getZ());
        }
        cPos.setPos(cPos.getX(), cPos.getY() + 1, cPos.getZ());
        return this.grow(world, (BlockPos)cPos, random);
    }
    
    public boolean grow(final World world, final BlockPos pos, final Random random) {
        if (world == null) {
            IC2.log.warn(LogCategory.General, "RubberTree did not spawn! w=%s.", world);
            return false;
        }
        final SaplingGrowTreeEvent event = new SaplingGrowTreeEvent(world, random, pos) {
            public void setResult(final Event.Result value) {
                super.setResult(value);
                if (value == Event.Result.DENY) {
                    TreeManager.INSTANCE.logCaller();
                }
            }
        };
        MinecraftForge.TERRAIN_GEN_BUS.post((Event)event);
        if (event.getResult() == Event.Result.DENY) {
            IC2.log.debug(LogCategory.General, "Rubber tree growth cancelled by " + TreeManager.INSTANCE.getCallerClass());
            return false;
        }
        final Block woodBlock = BlockName.rubber_wood.getInstance();
        final IBlockState leaves = BlockName.leaves.getInstance().getDefaultState().withProperty((IProperty)Ic2Leaves.typeProperty, (Comparable)Ic2Leaves.LeavesType.rubber);
        int treeholechance = 25;
        int height = this.getGrowHeight(world, pos);
        if (height < 2) {
            return false;
        }
        height -= random.nextInt(height / 2 + 1);
        final BlockPos.MutableBlockPos tmpPos = new BlockPos.MutableBlockPos();
        for (int cHeight = 0; cHeight < height; ++cHeight) {
            final BlockPos cPos = pos.up(cHeight);
            if (random.nextInt(100) <= treeholechance) {
                treeholechance -= 10;
                this.setBlockAndNotifyAdequately(world, cPos, woodBlock.getDefaultState().withProperty((IProperty)BlockRubWood.stateProperty, (Comparable)BlockRubWood.RubberWoodState.getWet(EnumFacing.HORIZONTALS[random.nextInt(4)])));
            }
            else {
                this.setBlockAndNotifyAdequately(world, cPos, woodBlock.getDefaultState().withProperty((IProperty)BlockRubWood.stateProperty, (Comparable)BlockRubWood.RubberWoodState.plain_y));
            }
            if (height < 4 || (height < 7 && cHeight > 1) || cHeight > 2) {
                for (int cx = pos.getX() - 2; cx <= pos.getX() + 2; ++cx) {
                    for (int cz = pos.getZ() - 2; cz <= pos.getZ() + 2; ++cz) {
                        final int chance = Math.max(1, cHeight + 4 - height);
                        final int dx = Math.abs(cx - pos.getX());
                        final int dz = Math.abs(cz - pos.getZ());
                        if ((dx <= 1 && dz <= 1) || (dx <= 1 && random.nextInt(chance) == 0) || (dz <= 1 && random.nextInt(chance) == 0)) {
                            tmpPos.setPos(cx, pos.getY() + cHeight, cz);
                            if (world.isAirBlock((BlockPos)tmpPos)) {
                                this.setBlockAndNotifyAdequately(world, new BlockPos((Vec3i)tmpPos), leaves);
                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i <= height / 4 + random.nextInt(2); ++i) {
            tmpPos.setPos(pos.getX(), pos.getY() + height + i, pos.getZ());
            if (world.isAirBlock((BlockPos)tmpPos)) {
                this.setBlockAndNotifyAdequately(world, new BlockPos((Vec3i)tmpPos), leaves);
            }
        }
        return true;
    }
    
    public int getGrowHeight(final World world, BlockPos pos) {
        final BlockPos below = pos.down();
        final IBlockState baseState = world.getBlockState(below);
        final Block baseBlock = baseState.getBlock();
        if (baseBlock.isAir(baseState, (IBlockAccess)world, below) || !baseBlock.canSustainPlant(baseState, (IBlockAccess)world, below, EnumFacing.UP, (IPlantable)BlockName.sapling.getInstance()) || (!world.isAirBlock(pos.up()) && world.getBlockState(pos.up()).getBlock() != BlockName.sapling.getInstance())) {
            return 0;
        }
        int height;
        for (height = 1, pos = pos.up(); world.isAirBlock(pos) && height < 8; pos = pos.up(), ++height) {}
        return height;
    }
    
    private static class TreeManager extends SecurityManager
    {
        static final TreeManager INSTANCE;
        private String caller;
        
        void logCaller() {
            this.caller = this.getClassContext()[2].getName();
        }
        
        String getCallerClass() {
            final String ret = this.caller;
            this.caller = null;
            return ret;
        }
        
        static {
            INSTANCE = new TreeManager();
        }
    }
}
