package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.IGrowable;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

class Irrigation extends TerraformerBase {
  boolean terraform(World world, BlockPos pos) {
    if (world.field_73012_v.nextInt(48000) == 0) {
      world.func_72912_H().func_76084_b(true);
      return true;
    } 
    pos = TileEntityTerra.getFirstBlockFrom(world, pos, 10);
    if (pos == null)
      return false; 
    if (TileEntityTerra.switchGround(world, pos, (Block)Blocks.field_150354_m, Blocks.field_150346_d.func_176223_P(), true)) {
      TileEntityTerra.switchGround(world, pos, (Block)Blocks.field_150354_m, Blocks.field_150346_d.func_176223_P(), true);
      return true;
    } 
    IBlockState state = world.func_180495_p(pos);
    Block block = state.func_177230_c();
    if (block instanceof IGrowable && ((IGrowable)block).func_176473_a(world, pos, state, false)) {
      ((IGrowable)block).func_176474_b(world, world.field_73012_v, pos, state);
      return true;
    } 
    if (block == Blocks.field_150329_H)
      return (spreadGrass(world, pos.func_177978_c()) || spreadGrass(world, pos.func_177974_f()) || 
        spreadGrass(world, pos.func_177968_d()) || spreadGrass(world, pos.func_177976_e())); 
    if (block == Blocks.field_150364_r || block == Blocks.field_150363_s) {
      BlockPos above = pos.func_177984_a();
      world.func_175656_a(above, state);
      IBlockState leaves = getLeaves(world, pos);
      if (leaves != null)
        createLeaves(world, above, leaves); 
      return true;
    } 
    if (block == Blocks.field_150480_ab) {
      world.func_175698_g(pos);
      return true;
    } 
    return false;
  }
  
  private static IBlockState getLeaves(World world, BlockPos pos) {
    for (EnumFacing facing : EnumFacing.field_176754_o) {
      BlockPos cPos = pos.func_177972_a(facing);
      IBlockState state = world.func_180495_p(cPos);
      if (state.func_177230_c().isLeaves(state, (IBlockAccess)world, cPos))
        return state; 
    } 
    return null;
  }
  
  private static void createLeaves(World world, BlockPos pos, IBlockState state) {
    BlockPos above = pos.func_177984_a();
    if (world.func_175623_d(above))
      world.func_175656_a(above, state); 
    for (EnumFacing facing : EnumFacing.field_176754_o) {
      BlockPos cPos = pos.func_177972_a(facing);
      if (world.func_175623_d(cPos))
        world.func_175656_a(cPos, state); 
    } 
  }
  
  private static boolean spreadGrass(World world, BlockPos pos) {
    if (world.field_73012_v.nextBoolean())
      return false; 
    pos = TileEntityTerra.getFirstBlockFrom(world, pos, 0);
    if (pos == null)
      return false; 
    Block block = world.func_180495_p(pos).func_177230_c();
    if (block == Blocks.field_150346_d) {
      world.func_175656_a(pos, Blocks.field_150349_c.func_176223_P());
      return true;
    } 
    if (block == Blocks.field_150349_c) {
      world.func_175656_a(pos.func_177984_a(), Blocks.field_150329_H.func_176223_P().func_177226_a((IProperty)BlockTallGrass.field_176497_a, (Comparable)BlockTallGrass.EnumType.GRASS));
      return true;
    } 
    return false;
  }
}
