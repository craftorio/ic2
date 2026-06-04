// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tfbp;

import java.util.Random;
import net.minecraft.block.BlockCrops;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.BlockDirectional;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.Block;
import ic2.core.block.machine.tileentity.TileEntityTerra;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.Iterator;
import ic2.core.ref.BlockName;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.init.Blocks;
import net.minecraft.block.state.IBlockState;
import java.util.ArrayList;

class Cultivation extends TerraformerBase
{
    static ArrayList<IBlockState> plants;
    
    @Override
    void init() {
        Cultivation.plants.add(Blocks.TALLGRASS.getDefaultState().withProperty((IProperty)BlockTallGrass.TYPE, (Comparable)BlockTallGrass.EnumType.GRASS));
        Cultivation.plants.add(Blocks.TALLGRASS.getDefaultState().withProperty((IProperty)BlockTallGrass.TYPE, (Comparable)BlockTallGrass.EnumType.GRASS));
        Cultivation.plants.add(Blocks.TALLGRASS.getDefaultState().withProperty((IProperty)BlockTallGrass.TYPE, (Comparable)BlockTallGrass.EnumType.FERN));
        Cultivation.plants.add(Blocks.RED_FLOWER.getDefaultState());
        Cultivation.plants.add(Blocks.YELLOW_FLOWER.getDefaultState());
        Cultivation.plants.add(Blocks.DOUBLE_PLANT.getDefaultState().withProperty((IProperty)BlockDoublePlant.VARIANT, (Comparable)BlockDoublePlant.EnumPlantType.GRASS));
        Cultivation.plants.add(Blocks.DOUBLE_PLANT.getDefaultState().withProperty((IProperty)BlockDoublePlant.VARIANT, (Comparable)BlockDoublePlant.EnumPlantType.ROSE));
        Cultivation.plants.add(Blocks.DOUBLE_PLANT.getDefaultState().withProperty((IProperty)BlockDoublePlant.VARIANT, (Comparable)BlockDoublePlant.EnumPlantType.SUNFLOWER));
        for (final BlockPlanks.EnumType type : BlockSapling.TYPE.getAllowedValues()) {
            Cultivation.plants.add(Blocks.SAPLING.getDefaultState().withProperty((IProperty)BlockSapling.TYPE, (Comparable)type));
        }
        Cultivation.plants.add(Blocks.WHEAT.getDefaultState());
        Cultivation.plants.add(Blocks.RED_MUSHROOM.getDefaultState());
        Cultivation.plants.add(Blocks.BROWN_MUSHROOM.getDefaultState());
        Cultivation.plants.add(Blocks.PUMPKIN.getDefaultState());
        Cultivation.plants.add(Blocks.MELON_BLOCK.getDefaultState());
        Cultivation.plants.add(BlockName.sapling.getInstance().getDefaultState());
    }
    
    @Override
    boolean terraform(final World world, BlockPos pos) {
        pos = TileEntityTerra.getFirstSolidBlockFrom(world, pos, 10);
        if (pos == null) {
            return false;
        }
        if (TileEntityTerra.switchGround(world, pos, (Block)Blocks.SAND, Blocks.DIRT.getDefaultState(), true)) {
            return true;
        }
        if (TileEntityTerra.switchGround(world, pos, Blocks.END_STONE, Blocks.DIRT.getDefaultState(), true)) {
            int i = 4;
            while (--i > 0 && TileEntityTerra.switchGround(world, pos, Blocks.END_STONE, Blocks.DIRT.getDefaultState(), true)) {}
        }
        final Block block = world.getBlockState(pos).getBlock();
        if (block == Blocks.DIRT) {
            world.setBlockState(pos, Blocks.GRASS.getDefaultState());
            return true;
        }
        return block == Blocks.GRASS && growPlantsOn(world, pos);
    }
    
    private static boolean growPlantsOn(final World world, final BlockPos pos) {
        final BlockPos above = pos.up();
        final IBlockState state = world.getBlockState(above);
        final Block block = state.getBlock();
        if (block.isAir(state, (IBlockAccess)world, above) || (block == Blocks.TALLGRASS && world.rand.nextInt(4) == 0)) {
            IBlockState plant = pickRandomPlant(world.rand);
            if (plant.getProperties().containsKey((Object)BlockDirectional.FACING)) {
                plant = plant.withProperty((IProperty)BlockDirectional.FACING, (Comparable)EnumFacing.HORIZONTALS[world.rand.nextInt(EnumFacing.HORIZONTALS.length)]);
            }
            if (plant.getBlock() instanceof BlockCrops) {
                world.setBlockState(pos, Blocks.FARMLAND.getDefaultState());
            }
            else if (plant.getBlock() == Blocks.DOUBLE_PLANT) {
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
    
    private static IBlockState pickRandomPlant(final Random random) {
        return Cultivation.plants.get(random.nextInt(Cultivation.plants.size()));
    }
    
    static {
        Cultivation.plants = new ArrayList<IBlockState>();
    }
}
