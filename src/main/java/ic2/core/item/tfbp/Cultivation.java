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
    plants.add(Blocks.field_150329_H.getDefaultState().func_177226_a((IProperty)BlockTallGrass.field_176497_a, (Comparable)BlockTallGrass.EnumType.GRASS));
    plants.add(Blocks.field_150329_H.getDefaultState().func_177226_a((IProperty)BlockTallGrass.field_176497_a, (Comparable)BlockTallGrass.EnumType.GRASS));
    plants.add(Blocks.field_150329_H.getDefaultState().func_177226_a((IProperty)BlockTallGrass.field_176497_a, (Comparable)BlockTallGrass.EnumType.FERN));
    plants.add(Blocks.field_150328_O.getDefaultState());
    plants.add(Blocks.field_150327_N.getDefaultState());
    plants.add(Blocks.field_150398_cm.getDefaultState().func_177226_a((IProperty)BlockDoublePlant.field_176493_a, (Comparable)BlockDoublePlant.EnumPlantType.GRASS));
    plants.add(Blocks.field_150398_cm.getDefaultState().func_177226_a((IProperty)BlockDoublePlant.field_176493_a, (Comparable)BlockDoublePlant.EnumPlantType.ROSE));
    plants.add(Blocks.field_150398_cm.getDefaultState().func_177226_a((IProperty)BlockDoublePlant.field_176493_a, (Comparable)BlockDoublePlant.EnumPlantType.SUNFLOWER));
    for (BlockPlanks.EnumType type : BlockSapling.field_176480_a.func_177700_c())
      plants.add(Blocks.field_150345_g.getDefaultState().func_177226_a((IProperty)BlockSapling.field_176480_a, (Comparable)type)); 
    plants.add(Blocks.field_150464_aj.getDefaultState());
    plants.add(Blocks.field_150337_Q.getDefaultState());
    plants.add(Blocks.field_150338_P.getDefaultState());
    plants.add(Blocks.field_150423_aK.getDefaultState());
    plants.add(Blocks.field_150440_ba.getDefaultState());
    plants.add(BlockName.sapling.getInstance().getDefaultState());
  }
  
  boolean terraform(World world, BlockPos pos) {
    pos = TileEntityTerra.getFirstSolidBlockFrom(world, pos, 10);
    if (pos == null)
      return false; 
    if (TileEntityTerra.switchGround(world, pos, (Block)Blocks.SAND, Blocks.field_150346_d.getDefaultState(), true))
      return true; 
    if (TileEntityTerra.switchGround(world, pos, Blocks.field_150377_bs, Blocks.field_150346_d.getDefaultState(), true)) {
      int i = 4;
      while (--i > 0 && TileEntityTerra.switchGround(world, pos, Blocks.field_150377_bs, Blocks.field_150346_d.getDefaultState(), true));
    } 
    Block block = world.getBlockState(pos).getBlock();
    if (block == Blocks.field_150346_d) {
      world.func_175656_a(pos, Blocks.field_150349_c.getDefaultState());
      return true;
    } 
    if (block == Blocks.field_150349_c)
      return growPlantsOn(world, pos); 
    return false;
  }
  
  private static boolean growPlantsOn(World world, BlockPos pos) {
    BlockPos above = pos.up();
    IBlockState state = world.getBlockState(above);
    Block block = state.getBlock();
    if (block.isAir(state, (IBlockAccess)world, above) || (block == Blocks.field_150329_H && world.rand.nextInt(4) == 0)) {
      IBlockState plant = pickRandomPlant(world.rand);
      if (plant.func_177228_b().containsKey(BlockDirectional.field_176387_N))
        plant = plant.func_177226_a((IProperty)BlockDirectional.field_176387_N, (Comparable)EnumFacing.field_176754_o[world.rand.nextInt(EnumFacing.field_176754_o.length)]); 
      if (plant.getBlock() instanceof net.minecraft.block.BlockCrops) {
        world.func_175656_a(pos, Blocks.FARMLAND.getDefaultState());
      } else if (plant.getBlock() == Blocks.field_150398_cm) {
        plant = plant.func_177226_a((IProperty)BlockDoublePlant.field_176492_b, (Comparable)BlockDoublePlant.EnumBlockHalf.LOWER);
        world.func_175656_a(above, plant.func_177226_a((IProperty)BlockDoublePlant.field_176492_b, (Comparable)BlockDoublePlant.EnumBlockHalf.LOWER));
        world.func_175656_a(above.up(), plant.func_177226_a((IProperty)BlockDoublePlant.field_176492_b, (Comparable)BlockDoublePlant.EnumBlockHalf.UPPER));
        return true;
      } 
      world.func_175656_a(above, plant);
      return true;
    } 
    return false;
  }
  
  private static IBlockState pickRandomPlant(Random random) {
    return plants.get(random.nextInt(plants.size()));
  }
  
  static ArrayList<IBlockState> plants = new ArrayList<>();
}
