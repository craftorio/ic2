package ic2.core.block.wiring.tileentity;

import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityElectricCESU extends TileEntityElectricBlock
{
	public TileEntityElectricCESU(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.CESU, pos, state, 2, 128, 300000);
	}
}
