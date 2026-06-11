package ic2.core.block.wiring.tileentity;

import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityElectricMFSU extends TileEntityElectricBlock
{
	public TileEntityElectricMFSU(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.MFSU, pos, state, 4, 2048, 40000000);
	}
}
