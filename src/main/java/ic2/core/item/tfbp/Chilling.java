package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class Chilling extends TerraformerBase {
  boolean terraform(World world, BlockPos pos) {
    pos = TileEntityTerra.getFirstBlockFrom(world, pos, 10);
    if (pos == null)
      return false; 
    IBlockState state = world.getBlockState(pos);
    Block block = state.getBlock();
    if (block == Blocks.WATER || block == Blocks.field_150358_i) {
      world.func_175656_a(pos, Blocks.field_150432_aD.getDefaultState());
      return true;
    } 
    if (block == Blocks.field_150432_aD) {
      BlockPos below = pos.func_177977_b();
      Block blockBelow = world.getBlockState(below).getBlock();
      if (blockBelow == Blocks.WATER || blockBelow == Blocks.field_150358_i) {
        world.func_175656_a(below, Blocks.field_150432_aD.getDefaultState());
        return true;
      } 
    } else if (block == Blocks.field_150431_aC) {
      if (isSurroundedBySnow(world, pos)) {
        world.func_175656_a(pos, Blocks.field_150433_aE.getDefaultState());
        return true;
      } 
      int size = ((Integer)state.func_177229_b((IProperty)BlockSnow.field_176315_a)).intValue();
      if (BlockSnow.field_176315_a.func_177700_c().contains(Integer.valueOf(size + 1))) {
        world.func_175656_a(pos, state.func_177226_a((IProperty)BlockSnow.field_176315_a, Integer.valueOf(size + 1)));
        return true;
      } 
    } 
    pos = pos.up();
    if (Blocks.field_150431_aC.func_176196_c(world, pos) || block == Blocks.field_150432_aD) {
      world.func_175656_a(pos, Blocks.field_150431_aC.getDefaultState());
      return true;
    } 
    return false;
  }
  
  private static boolean isSurroundedBySnow(World world, BlockPos pos) {
    for (EnumFacing dir : EnumFacing.field_176754_o) {
      if (!isSnowHere(world, pos.offset(dir)))
        return false; 
    } 
    return true;
  }
  
  private static boolean isSnowHere(World world, BlockPos pos) {
    int prevY = pos.getY();
    pos = TileEntityTerra.getFirstBlockFrom(world, pos, 16);
    if (pos == null || prevY > pos.getY())
      return false; 
    Block block = world.getBlockState(pos).getBlock();
    if (block == Blocks.field_150433_aE || block == Blocks.field_150431_aC)
      return true; 
    pos = pos.up();
    if (Blocks.field_150431_aC.func_176196_c(world, pos) || block == Blocks.field_150432_aD)
      world.func_175656_a(pos, Blocks.field_150431_aC.getDefaultState()); 
    return false;
  }
}
