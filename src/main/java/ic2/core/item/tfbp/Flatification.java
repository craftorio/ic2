package ic2.core.item.tfbp;

import ic2.core.block.machine.tileentity.TileEntityTerra;
import ic2.core.ref.Ic2Blocks;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class Flatification extends TerraformerBase
{
	static Set<Block> removable = Collections.newSetFromMap(new IdentityHashMap<>());

	@Override
	void init()
	{
		removable.add(Blocks.f_50125_);
		removable.add(Blocks.f_50126_);
		removable.add(Blocks.f_50034_);
		removable.add(Blocks.f_50069_);
		removable.add(Blocks.f_49994_);
		removable.add(Blocks.f_49992_);
		removable.add(Blocks.f_50493_);
		removable.add(Blocks.f_50050_);
		removable.add(Blocks.f_50051_);
		removable.add(Blocks.f_50052_);
		removable.add(Blocks.f_50053_);
		removable.add(Blocks.f_50054_);
		removable.add(Blocks.f_50055_);
		removable.add(Blocks.f_50359_);
		removable.add(Blocks.f_50112_);
		removable.add(Blocks.f_50111_);
		removable.add(Blocks.f_50092_);
		removable.add(Blocks.f_50073_);
		removable.add(Blocks.f_50072_);
		removable.add(Blocks.f_50133_);
		removable.add(Blocks.f_50186_);
		removable.add(Ic2Blocks.RUBBER_LEAVES);
		removable.add(Ic2Blocks.RUBBER_SAPLING);
		removable.add(Ic2Blocks.RUBBER_LOG);
	}

	@Override
	boolean terraform(Level world, BlockPos pos)
	{
		BlockPos workPos = TileEntityTerra.getFirstBlockFrom(world, pos, 20);
		if (workPos == null)
		{
			return false;
		}

		if (world.getBlockState(workPos).getBlock() == Blocks.f_50125_)
		{
			workPos = workPos.m_7495_();
		}

		if (pos.getY() == workPos.getY())
		{
			return false;
		} else if (workPos.getY() < pos.getY())
		{
			world.setBlockAndUpdate(workPos.m_7494_(), Blocks.f_50493_.defaultBlockState());
			return true;
		} else if (canRemove(world.getBlockState(workPos).getBlock()))
		{
			world.removeBlock(workPos, false);
			return true;
		} else
		{
			return false;
		}
	}

	private static boolean canRemove(Block block)
	{
		return removable.contains(block) || block.m_204297_().m_203656_(BlockTags.f_13104_) || block.m_204297_().m_203656_(BlockTags.f_13106_);
	}
}
