package ic2.core.block.wiring.tileentity;

import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityChargePadMFSU extends TileEntityChargePadBlock
{
	public TileEntityChargePadMFSU(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.MFSU_CHARGEPAD, pos, state, 4, 2048, 40000000);
	}
}
