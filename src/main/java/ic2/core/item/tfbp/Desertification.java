package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Desertification extends TerraformerBase
{
	@Override
	boolean terraform(Level world, BlockPos pos)
	{
		pos = TileEntityTerra.getFirstBlockFrom(world, pos, 10);
		if (pos == null)
		{
			return false;
		}

		BlockState sand = Blocks.f_49992_.defaultBlockState();
		if (!TileEntityTerra.switchGround(world, pos, Blocks.f_50493_, sand, false)
			&& !TileEntityTerra.switchGround(world, pos, Blocks.f_50034_, sand, false)
			&& !TileEntityTerra.switchGround(world, pos, Blocks.f_50093_, sand, false))
		{
			BlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			if (block == Blocks.f_49990_ || block == Blocks.f_50125_ || state.m_204336_(BlockTags.f_13035_) || isPlant(block))
			{
				world.removeBlock(pos, false);
				if (isPlant(world.getBlockState(pos.m_7494_()).getBlock()))
				{
					world.removeBlock(pos.m_7494_(), false);
				}

				return true;
			} else if (block != Blocks.f_50126_ && block != Blocks.f_50125_)
			{
				if ((state.m_204336_(BlockTags.f_13090_) || state.m_204336_(BlockTags.f_13105_)) && world.random.nextInt(15) == 0)
				{
					world.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
					return true;
				} else
				{
					return false;
				}
			} else
			{
				world.setBlockAndUpdate(pos, Blocks.f_49990_.defaultBlockState());
				return true;
			}
		} else
		{
			TileEntityTerra.switchGround(world, pos, Blocks.f_50493_, sand, false);
			return true;
		}
	}

	private static boolean isPlant(Block block)
	{
		for (BlockState state : Cultivation.plants)
		{
			if (state.getBlock() == block)
			{
				return true;
			}
		}

		return block.m_204297_().m_203656_(BlockTags.f_13104_)
			|| block.m_204297_().m_203656_(BlockTags.f_13073_)
			|| block.m_204297_().m_203656_(BlockTags.f_13041_);
	}
}
