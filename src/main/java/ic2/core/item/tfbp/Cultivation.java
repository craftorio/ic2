package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.ref.Ic2Blocks;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
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
		plants.add(Blocks.f_50359_.defaultBlockState());
		plants.add(Blocks.f_50359_.defaultBlockState());
		plants.add(Blocks.f_50035_.defaultBlockState());
		plants.add(Blocks.f_50112_.defaultBlockState());
		plants.add(Blocks.f_50111_.defaultBlockState());
		plants.add(Blocks.f_50359_.defaultBlockState());
		plants.add(Blocks.f_50357_.defaultBlockState());
		plants.add(Blocks.f_50355_.defaultBlockState());

		for (Holder<Block> entry : Registry.BLOCK.m_206058_(BlockTags.f_13104_))
		{
			Block block = (Block) entry.m_203334_();
			if (isVanilla(block))
			{
				plants.add(block.defaultBlockState());
			}
		}

		plants.add(Blocks.f_50092_.defaultBlockState());
		plants.add(Blocks.f_50073_.defaultBlockState());
		plants.add(Blocks.f_50072_.defaultBlockState());
		plants.add(Blocks.f_50133_.defaultBlockState());
		plants.add(Blocks.f_50186_.defaultBlockState());
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

		if (TileEntityTerra.switchGround(world, pos, Blocks.f_49992_, Blocks.f_50493_.defaultBlockState(), true))
		{
			return true;
		}

		if (TileEntityTerra.switchGround(world, pos, Blocks.f_50259_, Blocks.f_50493_.defaultBlockState(), true))
		{
			int i = 4;

			while (--i > 0 && TileEntityTerra.switchGround(world, pos, Blocks.f_50259_, Blocks.f_50493_.defaultBlockState(), true))
			{
			}
		}

		Block block = world.getBlockState(pos).getBlock();
		if (block == Blocks.f_50493_)
		{
			world.setBlockAndUpdate(pos, Blocks.f_50034_.defaultBlockState());
			return true;
		} else
		{
			return block == Blocks.f_50034_ ? growPlantsOn(world, pos) : false;
		}
	}

	private static boolean growPlantsOn(Level world, BlockPos pos)
	{
		BlockPos above = pos.m_7494_();
		BlockState state = world.getBlockState(above);
		Block block = state.getBlock();
		if (state.isAir() || block == Blocks.f_50359_ && world.random.nextInt(4) == 0)
		{
			BlockState plant = pickRandomPlant(world.random);
			if (plant.m_61148_().containsKey(DirectionalBlock.f_52588_))
			{
				plant = (BlockState) plant.setValue(DirectionalBlock.f_52588_, Util.HORIZONTAL_DIRS[world.random.nextInt(Util.HORIZONTAL_DIRS.length)]);
			}

			if (plant.getBlock() instanceof CropBlock)
			{
				world.setBlockAndUpdate(pos, Blocks.f_50093_.defaultBlockState());
			} else if (plant.getBlock() instanceof DoublePlantBlock)
			{
				world.setBlockAndUpdate(above, (BlockState) plant.setValue(DoublePlantBlock.f_52858_, DoubleBlockHalf.LOWER));
				world.setBlockAndUpdate(above.m_7494_(), (BlockState) plant.setValue(DoublePlantBlock.f_52858_, DoubleBlockHalf.UPPER));
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
