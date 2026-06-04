package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.ref.BlockName;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

class Cultivation extends TerraformerBase {
  void init() {
    plants.add(Blocks.TALLGRASS.getDefaultState().withProperty((IProperty)BlockTallGrass.TYPE, (Comparable)BlockTallGrass.EnumType.GRASS));
    plants.add(Blocks.TALLGRASS.getDefaultState().withProperty((IProperty)BlockTallGrass.TYPE, (Comparable)BlockTallGrass.EnumType.GRASS));
    plants.add(Blocks.TALLGRASS.getDefaultState().withProperty((IProperty)BlockTallGrass.TYPE, (Comparable)BlockTallGrass.EnumType.FERN));
    plants.add(Blocks.RED_FLOWER.getDefaultState());
    plants.add(Blocks.YELLOW_FLOWER.getDefaultState());
    plants.add(Blocks.DOUBLE_PLANT.getDefaultState().withProperty((IProperty)BlockDoublePlant.VARIANT, (Comparable)BlockDoublePlant.EnumPlantType.GRASS));
    plants.add(Blocks.DOUBLE_PLANT.getDefaultState().withProperty((IProperty)BlockDoublePlant.VARIANT, (Comparable)BlockDoublePlant.EnumPlantType.ROSE));
    plants.add(Blocks.DOUBLE_PLANT.getDefaultState().withProperty((IProperty)BlockDoublePlant.VARIANT, (Comparable)BlockDoublePlant.EnumPlantType.SUNFLOWER));
    for (BlockPlanks.EnumType type : BlockSapling.TYPE.getAllowedValues())
      plants.add(Blocks.SAPLING.getDefaultState().withProperty((IProperty)BlockSapling.TYPE, (Comparable)type)); 
    plants.add(Blocks.WHEAT.getDefaultState());
    plants.add(Blocks.RED_MUSHROOM.getDefaultState());
    plants.add(Blocks.BROWN_MUSHROOM.getDefaultState());
    plants.add(Blocks.PUMPKIN.getDefaultState());
    plants.add(Blocks.MELON_BLOCK.getDefaultState());
    plants.add(BlockName.sapling.getInstance().getDefaultState());
  }
  
  boolean terraform(World world, BlockPos pos) {
    pos = TileEntityTerra.getFirstSolidBlockFrom(world, pos, 10);
    if (pos == null)
      return false; 
    if (TileEntityTerra.switchGround(world, pos, (Block)Blocks.SAND, Blocks.DIRT.getDefaultState(), true))
      return true; 
    if (TileEntityTerra.switchGround(world, pos, Blocks.END_STONE, Blocks.DIRT.getDefaultState(), true)) {
      int i = 4;
      while (--i > 0 && TileEntityTerra.switchGround(world, pos, Blocks.END_STONE, Blocks.DIRT.getDefaultState(), true));
    } 
    Block block = world.getBlockState(pos).getBlock();
    if (block == Blocks.DIRT) {
      world.setBlockState(pos, Blocks.GRASS.getDefaultState());
      return true;
    } 
    if (block == Blocks.GRASS)
      return growPlantsOn(world, pos); 
    return false;
  }
  
  private static boolean growPlantsOn(World world, BlockPos pos) {
    BlockPos above = pos.up();
    IBlockState state = world.getBlockState(above);
    Block block = state.getBlock();
    if (block.isAir(state, (IBlockAccess)world, above) || (block == Blocks.TALLGRASS && world.rand.nextInt(4) == 0)) {
      IBlockState plant = pickRandomPlant(world.rand);
      if (plant.getProperties().containsKey(BlockDirectional.FACING))
        plant = plant.withProperty((IProperty)BlockDirectional.FACING, (Comparable)EnumFacing.HORIZONTALS[world.rand.nextInt(EnumFacing.HORIZONTALS.length)]); 
      if (plant.getBlock() instanceof net.minecraft.block.BlockCrops) {
        world.setBlockState(pos, Blocks.FARMLAND.getDefaultState());
      } else if (plant.getBlock() == Blocks.DOUBLE_PLANT) {
        plant = plant.withProperty((IProperty)BlockDoublePlant.HALF, (Comparable)BlockDoublePlant.EnumBlockHalf.LOWER);
        world.setBlockState(above, plant.withProperty((IProperty)BlockDoublePlant.HALF, (Comparable)BlockDoublePlant.EnumBlockHalf.LOWER));
        world.setBlockState(above.up(), plant.withProperty((IProperty)BlockDoublePlant.HALF, (Comparable)BlockDoublePlant.EnumBlockHalf.UPPER));
        return true;
      } 
      world.setBlockState(above, plant);
      return true;
    } 
    return false;
  }
  
  private static IBlockState pickRandomPlant(Random random) {
    return plants.get(random.nextInt(plants.size()));
  }
  
  static ArrayList<IBlockState> plants = new ArrayList<>();
}
