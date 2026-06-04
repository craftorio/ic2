// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tfbp;

import java.util.Map;
import java.util.Collections;
import java.util.IdentityHashMap;
import ic2.core.block.machine.tileentity.TileEntityTerra;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ic2.core.ref.BlockName;
import net.minecraft.init.Blocks;
import net.minecraft.block.Block;
import java.util.Set;

class Flatification extends TerraformerBase
{
    static Set<Block> removable;
    
    @Override
    void init() {
        Flatification.removable.add(Blocks.SNOW);
        Flatification.removable.add(Blocks.ICE);
        Flatification.removable.add((Block)Blocks.GRASS);
        Flatification.removable.add(Blocks.STONE);
        Flatification.removable.add(Blocks.GRAVEL);
        Flatification.removable.add((Block)Blocks.SAND);
        Flatification.removable.add(Blocks.DIRT);
        Flatification.removable.add((Block)Blocks.LEAVES);
        Flatification.removable.add((Block)Blocks.LEAVES2);
        Flatification.removable.add(Blocks.LOG);
        Flatification.removable.add((Block)Blocks.TALLGRASS);
        Flatification.removable.add((Block)Blocks.RED_FLOWER);
        Flatification.removable.add((Block)Blocks.YELLOW_FLOWER);
        Flatification.removable.add(Blocks.SAPLING);
        Flatification.removable.add(Blocks.WHEAT);
        Flatification.removable.add((Block)Blocks.RED_MUSHROOM);
        Flatification.removable.add((Block)Blocks.BROWN_MUSHROOM);
        Flatification.removable.add(Blocks.PUMPKIN);
        Flatification.removable.add(Blocks.MELON_BLOCK);
        Flatification.removable.add(BlockName.leaves.getInstance());
        Flatification.removable.add(BlockName.sapling.getInstance());
        Flatification.removable.add(BlockName.rubber_wood.getInstance());
    }
    
    @Override
    boolean terraform(final World world, final BlockPos pos) {
        BlockPos workPos = TileEntityTerra.getFirstBlockFrom(world, pos, 20);
        if (workPos == null) {
            return false;
        }
        if (world.getBlockState(workPos).getBlock() == Blocks.SNOW_LAYER) {
            workPos = workPos.down();
        }
        if (pos.getY() == workPos.getY()) {
            return false;
        }
        if (workPos.getY() < pos.getY()) {
            world.setBlockState(workPos.up(), Blocks.DIRT.getDefaultState());
            return true;
        }
        if (canRemove(world.getBlockState(workPos).getBlock())) {
            world.setBlockToAir(workPos);
            return true;
        }
        return false;
    }
    
    private static boolean canRemove(final Block block) {
        return Flatification.removable.contains(block);
    }
    
    static {
        Flatification.removable = Collections.newSetFromMap(new IdentityHashMap<Block, Boolean>());
    }
}
