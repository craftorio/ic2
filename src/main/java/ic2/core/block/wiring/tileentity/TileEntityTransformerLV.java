package ic2.core.block.wiring.tileentity;

import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityTransformerLV extends TileEntityTransformer
{
	public TileEntityTransformerLV(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.LV_TRANSFORMER, pos, state, 1);
	}
}
