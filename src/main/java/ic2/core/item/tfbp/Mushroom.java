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
    if (growBlockWithDependancy(world, pos, Blocks.BROWN_MUSHROOM_BLOCK, (Block)Blocks.BROWN_MUSHROOM))
      return true; 
    return false;
  }
  
  private static boolean growBlockWithDependancy(World world, BlockPos pos, Block target, Block dependancy) {
    BlockPos.MutableBlockPos cPos = new BlockPos.MutableBlockPos();
    for (int xm = pos.getX() - 1; dependancy != null && xm < pos.getX() + 1; xm++) {
      for (int zm = pos.getZ() - 1; zm < pos.getZ() + 1; zm++) {
        for (int ym = pos.getY() + 5; ym > pos.getY() - 2; ym--) {
          cPos.setPos(xm, ym, zm);
          IBlockState state = world.getBlockState((BlockPos)cPos);
          Block block = state.getBlock();
          if (dependancy == Blocks.MYCELIUM) {
            if (block == dependancy || block == Blocks.BROWN_MUSHROOM_BLOCK || block == Blocks.RED_MUSHROOM_BLOCK)
              break; 
            if (!block.isAir(state, (IBlockAccess)world, (BlockPos)cPos))
              if (block == Blocks.DIRT || block == Blocks.GRASS) {
                BlockPos dstPos = new BlockPos((Vec3i)cPos);
                world.setBlockState(dstPos, dependancy.getDefaultState());
                BiomeUtil.setBiome(world, dstPos, Biomes.MUSHROOM_ISLAND);
                return true;
              }  
          } else if (dependancy == Blocks.BROWN_MUSHROOM) {
            if (block == Blocks.BROWN_MUSHROOM || block == Blocks.RED_MUSHROOM)
              break; 
            if (!block.isAir(state, (IBlockAccess)world, (BlockPos)cPos))
              if (growBlockWithDependancy(world, (BlockPos)cPos, (Block)Blocks.BROWN_MUSHROOM, (Block)Blocks.MYCELIUM))
                return true;  
          } 
        } 
      } 
    } 
    if (target == Blocks.BROWN_MUSHROOM) {
      Block base = world.getBlockState(pos).getBlock();
      if (base != Blocks.MYCELIUM)
        if (base == Blocks.BROWN_MUSHROOM_BLOCK || base == Blocks.RED_MUSHROOM_BLOCK) {
          world.setBlockState(pos, Blocks.MYCELIUM.getDefaultState());
        } else {
          return false;
        }  
      BlockPos above = pos.up();
      IBlockState state = world.getBlockState(above);
      Block block = state.getBlock();
      if (!block.isAir(state, (IBlockAccess)world, above) && block != Blocks.TALLGRASS)
        return false; 
      BlockBush blockBush = world.rand.nextBoolean() ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM;
      world.setBlockState(above, blockBush.getDefaultState());
      return true;
    } 
    if (target == Blocks.BROWN_MUSHROOM_BLOCK) {
      BlockPos above = pos.up();
      IBlockState state = world.getBlockState(above);
      Block base = state.getBlock();
      if (base != Blocks.BROWN_MUSHROOM && base != Blocks.RED_MUSHROOM)
        return false; 
      if (((BlockMushroom)base).generateBigMushroom(world, above, state, world.rand)) {
        for (int i = pos.getX() - 1; i < pos.getX() + 1; i++) {
          for (int zm = pos.getZ() - 1; zm < pos.getZ() + 1; zm++) {
            cPos.setPos(i, above.getY(), zm);
            Block block = world.getBlockState((BlockPos)cPos).getBlock();
            if (block == Blocks.BROWN_MUSHROOM || block == Blocks.RED_MUSHROOM)
              world.setBlockToAir(new BlockPos((Vec3i)cPos)); 
          } 
        } 
        return true;
      } 
    } 
    return false;
  }
}
