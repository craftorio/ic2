package ic2.core.block.wiring.tileentity;

import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityElectricMFE extends TileEntityElectricBlock
{
	public TileEntityElectricMFE(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.MFE, pos, state, 3, 512, 4000000);
	}
}
