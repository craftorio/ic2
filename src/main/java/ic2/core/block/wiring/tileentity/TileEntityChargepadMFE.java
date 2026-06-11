package ic2.core.block.wiring.tileentity;

import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityChargepadMFE extends TileEntityChargepadBlock
{
	public TileEntityChargepadMFE(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.MFE_CHARGEPAD, pos, state, 3, 512, 4000000);
	}
}
