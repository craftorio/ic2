package ic2.core.block.wiring.tileentity;

import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityTransformerMV extends TileEntityTransformer
{
	public TileEntityTransformerMV(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.MV_TRANSFORMER, pos, state, 2);
	}
}
