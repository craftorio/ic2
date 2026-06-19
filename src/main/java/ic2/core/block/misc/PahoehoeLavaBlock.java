package ic2.core.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class PahoehoeLavaBlock extends LiquidBlock
{
	public PahoehoeLavaBlock(FlowingFluid fluid, Block.Properties properties)
	{
		super(fluid, properties);
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean movedByPiston)
	{
		if (state.getValue(LiquidBlock.LEVEL) == 0)
		{
			world.scheduleTick(pos, this, 360);
		}
	}

	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
	{
		if (state.getValue(LiquidBlock.LEVEL) == 0)
		{
			world.setBlockAndUpdate(pos, Blocks.BASALT.defaultBlockState());
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston)
	{
		super.neighborChanged(state, world, pos, neighborBlock, neighborPos, movedByPiston);
		if (state.getValue(LiquidBlock.LEVEL) == 0 && isTouchingWater(world, pos))
		{
			world.setBlockAndUpdate(pos, Blocks.BASALT.defaultBlockState());
		}
	}

	@Override
	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
	{
		if (!world.isClientSide)
		{
			entity.hurt(world.damageSources().lava(), 4.0F);
			entity.setSecondsOnFire(30);
		}
	}

	private static boolean isTouchingWater(LevelAccessor world, BlockPos pos)
	{
		for (Direction dir : Direction.values())
		{
			if (world.getFluidState(pos.relative(dir)).is(FluidTags.WATER))
			{
				return true;
			}
		}

		return false;
	}
}
