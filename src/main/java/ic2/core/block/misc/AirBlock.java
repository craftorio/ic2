package ic2.core.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class AirBlock extends LiquidBlock
{
	public AirBlock(FlowingFluid fluid, Properties properties)
	{
		super(fluid, properties);
	}

	@Override
	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
	{
	}
}
