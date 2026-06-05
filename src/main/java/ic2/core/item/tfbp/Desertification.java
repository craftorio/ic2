package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.ref.BlockName;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class Desertification extends TerraformerBase {
   @Override
   boolean terraform(World world, BlockPos pos) {
      pos = TileEntityTerra.getFirstBlockFrom(world, pos, 10);
      if (pos == null) {
         return false;
      }

      IBlockState sand = Blocks.SAND.getDefaultState();
      if (!TileEntityTerra.switchGround(world, pos, Blocks.DIRT, sand, false)
         && !TileEntityTerra.switchGround(world, pos, Blocks.GRASS, sand, false)
         && !TileEntityTerra.switchGround(world, pos, Blocks.FARMLAND, sand, false)) {
         Block block = world.getBlockState(pos).getBlock();
         if (block == Blocks.WATER
            || block == Blocks.FLOWING_WATER
            || block == Blocks.SNOW_LAYER
            || block == Blocks.LEAVES
            || block == Blocks.LEAVES2
            || block == BlockName.leaves.getInstance()
            || isPlant(block)) {
            world.setBlockToAir(pos);
            if (isPlant(world.getBlockState(pos.up()).getBlock())) {
               world.setBlockToAir(pos.up());
            }

            return true;
         } else if (block != Blocks.ICE && block != Blocks.SNOW) {
            if ((block == Blocks.PLANKS || block == Blocks.LOG || block == BlockName.rubber_wood.getInstance())
               && world.rand.nextInt(15) == 0) {
               world.setBlockState(pos, Blocks.FIRE.getDefaultState());
               return true;
            } else {
               return false;
            }
         } else {
            world.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState());
            return true;
         }
      } else {
         TileEntityTerra.switchGround(world, pos, Blocks.DIRT, sand, false);
         return true;
      }
   }

   private static boolean isPlant(Block block) {
      for (IBlockState state : Cultivation.plants) {
         if (state.getBlock() == block) {
            return true;
         }
      }

      return false;
   }
}
