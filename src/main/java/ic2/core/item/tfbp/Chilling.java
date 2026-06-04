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
    if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
      world.setBlockState(pos, Blocks.ICE.getDefaultState());
      return true;
    } 
    if (block == Blocks.ICE) {
      BlockPos below = pos.down();
      Block blockBelow = world.getBlockState(below).getBlock();
      if (blockBelow == Blocks.WATER || blockBelow == Blocks.FLOWING_WATER) {
        world.setBlockState(below, Blocks.ICE.getDefaultState());
        return true;
      } 
    } else if (block == Blocks.SNOW_LAYER) {
      if (isSurroundedBySnow(world, pos)) {
        world.setBlockState(pos, Blocks.SNOW.getDefaultState());
        return true;
      } 
      int size = ((Integer)state.getValue((IProperty)BlockSnow.LAYERS)).intValue();
      if (BlockSnow.LAYERS.getAllowedValues().contains(Integer.valueOf(size + 1))) {
        world.setBlockState(pos, state.withProperty((IProperty)BlockSnow.LAYERS, Integer.valueOf(size + 1)));
        return true;
      } 
    } 
    pos = pos.up();
    if (Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos) || block == Blocks.ICE) {
      world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState());
      return true;
    } 
    return false;
  }
  
  private static boolean isSurroundedBySnow(World world, BlockPos pos) {
    for (EnumFacing dir : EnumFacing.HORIZONTALS) {
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
    if (block == Blocks.SNOW || block == Blocks.SNOW_LAYER)
      return true; 
    pos = pos.up();
    if (Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos) || block == Blocks.ICE)
      world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState()); 
    return false;
  }
}
