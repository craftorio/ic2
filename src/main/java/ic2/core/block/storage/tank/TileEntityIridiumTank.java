package ic2.core.block.storage.tank;

import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityIridiumTank extends TileEntityTank
{
	public TileEntityIridiumTank(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.IRIDIUM_TANK, pos, state, 1024);
	}
}
