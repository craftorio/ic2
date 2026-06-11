package ic2.core.block.kineticgenerator.tileentity;

import ic2.api.energy.tile.IKineticSource;
import ic2.core.block.tileentity.TileEntityInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityAbstractKineticGenerator extends TileEntityInventory implements IKineticSource
{
	public TileEntityAbstractKineticGenerator(BlockEntityType<? extends TileEntityInventory> waterKineticGenerator, BlockPos pos, BlockState state)
	{
		super(waterKineticGenerator, pos, state);
	}
}
