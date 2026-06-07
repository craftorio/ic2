package ic2.core.block.wiring.tileentity;

import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityElectricBatBox extends TileEntityElectricBlock
{
	public TileEntityElectricBatBox(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.BATBOX, pos, state, 1, 32, 40000);
	}
}
