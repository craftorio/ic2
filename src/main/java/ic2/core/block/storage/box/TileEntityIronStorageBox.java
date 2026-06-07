package ic2.core.block.storage.box;

import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityIronStorageBox extends TileEntityStorageBox
{
	public TileEntityIronStorageBox(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.IRON_STORAGE_BOX, pos, state, 45);
	}
}
