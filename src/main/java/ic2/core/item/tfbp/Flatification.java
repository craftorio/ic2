package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.ref.BlockName;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class Flatification extends TerraformerBase {
  void init() {
    removable.add(Blocks.SNOW);
    removable.add(Blocks.ICE);
    removable.add(Blocks.GRASS);
    removable.add(Blocks.STONE);
    removable.add(Blocks.GRAVEL);
    removable.add(Blocks.SAND);
    removable.add(Blocks.DIRT);
    removable.add(Blocks.LEAVES);
    removable.add(Blocks.LEAVES2);
    removable.add(Blocks.LOG);
    removable.add(Blocks.TALLGRASS);
    removable.add(Blocks.RED_FLOWER);
    removable.add(Blocks.YELLOW_FLOWER);
    removable.add(Blocks.SAPLING);
    removable.add(Blocks.WHEAT);
    removable.add(Blocks.RED_MUSHROOM);
    removable.add(Blocks.BROWN_MUSHROOM);
    removable.add(Blocks.PUMPKIN);
    removable.add(Blocks.MELON_BLOCK);
    removable.add(BlockName.leaves.getInstance());
    removable.add(BlockName.sapling.getInstance());
    removable.add(BlockName.rubber_wood.getInstance());
  }
  
  boolean terraform(World world, BlockPos pos) {
    BlockPos workPos = TileEntityTerra.getFirstBlockFrom(world, pos, 20);
    if (workPos == null)
      return false; 
    if (world.getBlockState(workPos).getBlock() == Blocks.SNOW_LAYER)
      workPos = workPos.down(); 
    if (pos.getY() == workPos.getY())
      return false; 
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
  
  private static boolean canRemove(Block block) {
    return removable.contains(block);
  }
  
  static Set<Block> removable = Collections.newSetFromMap(new IdentityHashMap<>());
}
