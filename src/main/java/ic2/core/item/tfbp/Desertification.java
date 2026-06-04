package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.ref.BlockName;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class Desertification extends TerraformerBase {
  boolean terraform(World world, BlockPos pos) {
    pos = TileEntityTerra.getFirstBlockFrom(world, pos, 10);
    if (pos == null)
      return false; 
    IBlockState sand = Blocks.SAND.getDefaultState();
    if (TileEntityTerra.switchGround(world, pos, Blocks.field_150346_d, sand, false) || 
      TileEntityTerra.switchGround(world, pos, (Block)Blocks.field_150349_c, sand, false) || 
      TileEntityTerra.switchGround(world, pos, Blocks.FARMLAND, sand, false)) {
      TileEntityTerra.switchGround(world, pos, Blocks.field_150346_d, sand, false);
      return true;
    } 
    Block block = world.getBlockState(pos).getBlock();
    if (block == Blocks.field_150355_j || block == Blocks.field_150358_i || block == Blocks.field_150431_aC || block == Blocks.field_150362_t || block == Blocks.field_150361_u || block == BlockName.leaves
      .getInstance() || isPlant(block)) {
      world.func_175698_g(pos);
      if (isPlant(world.getBlockState(pos.func_177984_a()).getBlock()))
        world.func_175698_g(pos.func_177984_a()); 
      return true;
    } 
    if (block == Blocks.field_150432_aD || block == Blocks.field_150433_aE) {
      world.func_175656_a(pos, Blocks.field_150358_i.getDefaultState());
      return true;
    } 
    if ((block == Blocks.field_150344_f || block == Blocks.field_150364_r || block == BlockName.rubber_wood.getInstance()) && world.field_73012_v
      .nextInt(15) == 0) {
      world.func_175656_a(pos, Blocks.field_150480_ab.getDefaultState());
      return true;
    } 
    return false;
  }
  
  private static boolean isPlant(Block block) {
    for (IBlockState state : Cultivation.plants) {
      if (state.getBlock() == block)
        return true; 
    } 
    return false;
  }
}
