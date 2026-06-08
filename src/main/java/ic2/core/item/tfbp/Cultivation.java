package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.ref.Ic2Blocks;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class Cultivation extends TerraformerBase
{
	static List<BlockState> plants = new ArrayList<>();

	@Override
	void init()
	{
		plants.add(Blocks.TALL_GRASS.defaultBlockState());
		plants.add(Blocks.TALL_GRASS.defaultBlockState());
		plants.add(Blocks.FERN.defaultBlockState());
		plants.add(Blocks.POPPY.defaultBlockState());
		plants.add(Blocks.DANDELION.defaultBlockState());
		plants.add(Blocks.TALL_GRASS.defaultBlockState());
		plants.add(Blocks.ROSE_BUSH.defaultBlockState());
		plants.add(Blocks.SUNFLOWER.defaultBlockState());

		for (Holder<Block> entry : BuiltInRegistries.BLOCK.getTagOrEmpty(BlockTags.SAPLINGS))
		{
			Block block = (Block) entry.value();
			if (isVanilla(block))
			{
				plants.add(block.defaultBlockState());
			}
		}

		plants.add(Blocks.WHEAT.defaultBlockState());
		plants.add(Blocks.RED_MUSHROOM.defaultBlockState());
		plants.add(Blocks.BROWN_MUSHROOM.defaultBlockState());
		plants.add(Blocks.PUMPKIN.defaultBlockState());
		plants.add(Blocks.MELON.defaultBlockState());
		plants.add(Ic2Blocks.RUBBER_SAPLING.defaultBlockState());
	}

	@Override
	boolean terraform(Level world, BlockPos pos)
	{
		pos = TileEntityTerra.getFirstSolidBlockFrom(world, pos, 10);
		if (pos == null)
		{
			return false;
		}

		if (TileEntityTerra.switchGround(world, pos, Blocks.SAND, Blocks.DIRT.defaultBlockState(), true))
		{
			return true;
		}

		if (TileEntityTerra.switchGround(world, pos, Blocks.END_STONE, Blocks.DIRT.defaultBlockState(), true))
		{
			int i = 4;

			while (--i > 0 && TileEntityTerra.switchGround(world, pos, Blocks.END_STONE, Blocks.DIRT.defaultBlockState(), true))
			{
			}
		}

		Block block = world.getBlockState(pos).getBlock();
		if (block == Blocks.DIRT)
		{
			world.setBlockAndUpdate(pos, Blocks.GRASS.defaultBlockState());
			return true;
		} else
		{
			return block == Blocks.GRASS ? growPlantsOn(world, pos) : false;
		}
	}

	private static boolean growPlantsOn(Level world, BlockPos pos)
	{
		BlockPos above = pos.above();
		BlockState state = world.getBlockState(above);
		Block block = state.getBlock();
		if (state.isAir() || block == Blocks.TALL_GRASS && world.random.nextInt(4) == 0)
		{
			BlockState plant = pickRandomPlant(world.random);
			if (plant.getValues().containsKey(DirectionalBlock.FACING))
			{
				plant = (BlockState) plant.setValue(DirectionalBlock.FACING, Util.HORIZONTAL_DIRS[world.random.nextInt(Util.HORIZONTAL_DIRS.length)]);
			}

			if (plant.getBlock() instanceof CropBlock)
			{
				world.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
			} else if (plant.getBlock() instanceof DoublePlantBlock)
			{
				world.setBlockAndUpdate(above, (BlockState) plant.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
				world.setBlockAndUpdate(above.above(), (BlockState) plant.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
				return true;
			}

			world.setBlockAndUpdate(above, plant);
			return true;
		} else
		{
			return false;
		}
	}

	private static BlockState pickRandomPlant(RandomSource random)
	{
		return plants.get(random.nextInt(plants.size()));
	}
}
