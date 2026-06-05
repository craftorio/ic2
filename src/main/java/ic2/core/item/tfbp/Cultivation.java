package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.ref.BlockName;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockDoublePlant.EnumBlockHalf;
import net.minecraft.block.BlockDoublePlant.EnumPlantType;
import net.minecraft.block.BlockTallGrass.EnumType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class Cultivation extends TerraformerBase {
   static ArrayList<IBlockState> plants = new ArrayList<>();

   @Override
   void init() {
      plants.add(Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, EnumType.GRASS));
      plants.add(Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, EnumType.GRASS));
      plants.add(Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, EnumType.FERN));
      plants.add(Blocks.RED_FLOWER.getDefaultState());
      plants.add(Blocks.YELLOW_FLOWER.getDefaultState());
      plants.add(Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, EnumPlantType.GRASS));
      plants.add(Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, EnumPlantType.ROSE));
      plants.add(Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, EnumPlantType.SUNFLOWER));

      for (net.minecraft.block.BlockPlanks.EnumType type : BlockSapling.TYPE.getAllowedValues()) {
         plants.add(Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, type));
      }

      plants.add(Blocks.WHEAT.getDefaultState());
      plants.add(Blocks.RED_MUSHROOM.getDefaultState());
      plants.add(Blocks.BROWN_MUSHROOM.getDefaultState());
      plants.add(Blocks.PUMPKIN.getDefaultState());
      plants.add(Blocks.MELON_BLOCK.getDefaultState());
      plants.add(BlockName.sapling.getInstance().getDefaultState());
   }

   @Override
   boolean terraform(World world, BlockPos pos) {
      pos = TileEntityTerra.getFirstSolidBlockFrom(world, pos, 10);
      if (pos == null) {
         return false;
      }

      if (TileEntityTerra.switchGround(world, pos, Blocks.SAND, Blocks.DIRT.getDefaultState(), true)) {
         return true;
      }

      if (TileEntityTerra.switchGround(world, pos, Blocks.END_STONE, Blocks.DIRT.getDefaultState(), true)) {
         int i = 4;

         while (--i > 0 && TileEntityTerra.switchGround(world, pos, Blocks.END_STONE, Blocks.DIRT.getDefaultState(), true)) {
         }
      }

      Block block = world.getBlockState(pos).getBlock();
      if (block == Blocks.DIRT) {
         world.setBlockState(pos, Blocks.GRASS.getDefaultState());
         return true;
      } else {
         return block == Blocks.GRASS ? growPlantsOn(world, pos) : false;
      }
   }

   private static boolean growPlantsOn(World world, BlockPos pos) {
      BlockPos above = pos.up();
      IBlockState state = world.getBlockState(above);
      Block block = state.getBlock();
      if (block.isAir(state, world, above) || block == Blocks.TALLGRASS && world.rand.nextInt(4) == 0) {
         IBlockState plant = pickRandomPlant(world.rand);
         if (plant.getProperties().containsKey(BlockDirectional.FACING)) {
            plant = plant.withProperty(
               BlockDirectional.FACING, EnumFacing.HORIZONTALS[world.rand.nextInt(EnumFacing.HORIZONTALS.length)]
            );
         }

         if (plant.getBlock() instanceof BlockCrops) {
            world.setBlockState(pos, Blocks.FARMLAND.getDefaultState());
         } else if (plant.getBlock() == Blocks.DOUBLE_PLANT) {
            plant = plant.withProperty(BlockDoublePlant.HALF, EnumBlockHalf.LOWER);
            world.setBlockState(above, plant.withProperty(BlockDoublePlant.HALF, EnumBlockHalf.LOWER));
            world.setBlockState(above.up(), plant.withProperty(BlockDoublePlant.HALF, EnumBlockHalf.UPPER));
            return true;
         }

         world.setBlockState(above, plant);
         return true;
      } else {
         return false;
      }
   }

   private static IBlockState pickRandomPlant(Random random) {
      return plants.get(random.nextInt(plants.size()));
   }
}
