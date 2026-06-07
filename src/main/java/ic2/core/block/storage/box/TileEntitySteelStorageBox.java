package ic2.core.block.storage.box;

import ic2.core.ref.Ic2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySteelStorageBox extends TileEntityStorageBox
{
	public TileEntitySteelStorageBox(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.STEEL_STORAGE_BOX, pos, state, 63);
	}
}
