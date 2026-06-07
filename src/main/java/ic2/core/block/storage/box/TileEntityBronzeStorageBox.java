package ic2.core.block.storage.box;

import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityBronzeStorageBox extends TileEntityStorageBox
{
	public TileEntityBronzeStorageBox(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.BRONZE_STORAGE_BOX, pos, state, 45);
	}
}
