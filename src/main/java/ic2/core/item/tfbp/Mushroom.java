package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.util.BiomeUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

class Mushroom extends TerraformerBase {
  boolean terraform(World world, BlockPos pos) {
    pos = TileEntityTerra.getFirstSolidBlockFrom(world, pos, 20);
    if (pos == null)
      return false; 
    if (growBlockWithDependancy(world, pos, Blocks.field_150420_aW, (Block)Blocks.field_150338_P))
      return true; 
    return false;
  }
  
  private static boolean growBlockWithDependancy(World world, BlockPos pos, Block target, Block dependancy) {
    BlockPos.MutableBlockPos cPos = new BlockPos.MutableBlockPos();
    for (int xm = pos.getX() - 1; dependancy != null && xm < pos.getX() + 1; xm++) {
      for (int zm = pos.getZ() - 1; zm < pos.getZ() + 1; zm++) {
        for (int ym = pos.getY() + 5; ym > pos.getY() - 2; ym--) {
          cPos.func_181079_c(xm, ym, zm);
          IBlockState state = world.getBlockState((BlockPos)cPos);
          Block block = state.getBlock();
          if (dependancy == Blocks.field_150391_bh) {
            if (block == dependancy || block == Blocks.field_150420_aW || block == Blocks.field_150419_aX)
              break; 
            if (!block.isAir(state, (IBlockAccess)world, (BlockPos)cPos))
              if (block == Blocks.field_150346_d || block == Blocks.field_150349_c) {
                BlockPos dstPos = new BlockPos((Vec3i)cPos);
                world.func_175656_a(dstPos, dependancy.getDefaultState());
                BiomeUtil.setBiome(world, dstPos, Biomes.field_76789_p);
                return true;
              }  
          } else if (dependancy == Blocks.field_150338_P) {
            if (block == Blocks.field_150338_P || block == Blocks.field_150337_Q)
              break; 
            if (!block.isAir(state, (IBlockAccess)world, (BlockPos)cPos))
              if (growBlockWithDependancy(world, (BlockPos)cPos, (Block)Blocks.field_150338_P, (Block)Blocks.field_150391_bh))
                return true;  
          } 
        } 
      } 
    } 
    if (target == Blocks.field_150338_P) {
      Block base = world.getBlockState(pos).getBlock();
      if (base != Blocks.field_150391_bh)
        if (base == Blocks.field_150420_aW || base == Blocks.field_150419_aX) {
          world.func_175656_a(pos, Blocks.field_150391_bh.getDefaultState());
        } else {
          return false;
        }  
      BlockPos above = pos.func_177984_a();
      IBlockState state = world.getBlockState(above);
      Block block = state.getBlock();
      if (!block.isAir(state, (IBlockAccess)world, above) && block != Blocks.field_150329_H)
        return false; 
      BlockBush blockBush = world.field_73012_v.nextBoolean() ? Blocks.field_150338_P : Blocks.field_150337_Q;
      world.func_175656_a(above, blockBush.getDefaultState());
      return true;
    } 
    if (target == Blocks.field_150420_aW) {
      BlockPos above = pos.func_177984_a();
      IBlockState state = world.getBlockState(above);
      Block base = state.getBlock();
      if (base != Blocks.field_150338_P && base != Blocks.field_150337_Q)
        return false; 
      if (((BlockMushroom)base).func_176485_d(world, above, state, world.field_73012_v)) {
        for (int i = pos.getX() - 1; i < pos.getX() + 1; i++) {
          for (int zm = pos.getZ() - 1; zm < pos.getZ() + 1; zm++) {
            cPos.func_181079_c(i, above.getY(), zm);
            Block block = world.getBlockState((BlockPos)cPos).getBlock();
            if (block == Blocks.field_150338_P || block == Blocks.field_150337_Q)
              world.func_175698_g(new BlockPos((Vec3i)cPos)); 
          } 
        } 
        return true;
      } 
    } 
    return false;
  }
}
