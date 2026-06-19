package ic2.core.block.wiring.tileentity;

import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityChargePadBatBox extends TileEntityChargePadBlock
{
	public TileEntityChargePadBatBox(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.BATBOX_CHARGEPAD, pos, state, 1, 32, 40000);
	}
}
