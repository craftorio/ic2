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
    removable.add(Blocks.field_150433_aE);
    removable.add(Blocks.field_150432_aD);
    removable.add(Blocks.field_150349_c);
    removable.add(Blocks.field_150348_b);
    removable.add(Blocks.field_150351_n);
    removable.add(Blocks.SAND);
    removable.add(Blocks.field_150346_d);
    removable.add(Blocks.field_150362_t);
    removable.add(Blocks.field_150361_u);
    removable.add(Blocks.field_150364_r);
    removable.add(Blocks.field_150329_H);
    removable.add(Blocks.field_150328_O);
    removable.add(Blocks.field_150327_N);
    removable.add(Blocks.field_150345_g);
    removable.add(Blocks.field_150464_aj);
    removable.add(Blocks.field_150337_Q);
    removable.add(Blocks.field_150338_P);
    removable.add(Blocks.field_150423_aK);
    removable.add(Blocks.field_150440_ba);
    removable.add(BlockName.leaves.getInstance());
    removable.add(BlockName.sapling.getInstance());
    removable.add(BlockName.rubber_wood.getInstance());
  }
  
  boolean terraform(World world, BlockPos pos) {
    BlockPos workPos = TileEntityTerra.getFirstBlockFrom(world, pos, 20);
    if (workPos == null)
      return false; 
    if (world.func_180495_p(workPos).func_177230_c() == Blocks.field_150431_aC)
      workPos = workPos.func_177977_b(); 
    if (pos.func_177956_o() == workPos.func_177956_o())
      return false; 
    if (workPos.func_177956_o() < pos.func_177956_o()) {
      world.func_175656_a(workPos.func_177984_a(), Blocks.field_150346_d.getDefaultState());
      return true;
    } 
    if (canRemove(world.func_180495_p(workPos).func_177230_c())) {
      world.func_175698_g(workPos);
      return true;
    } 
    return false;
  }
  
  private static boolean canRemove(Block block) {
    return removable.contains(block);
  }
  
  static Set<Block> removable = Collections.newSetFromMap(new IdentityHashMap<>());
}
