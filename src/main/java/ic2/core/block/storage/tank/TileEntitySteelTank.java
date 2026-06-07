package ic2.core.block.storage.tank;

import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySteelTank extends TileEntityTank
{
	public TileEntitySteelTank(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.STEEL_TANK, pos, state, 128);
	}
}
