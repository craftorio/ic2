package ic2.core.block.kineticgenerator.tileentity;

import ic2.api.energy.tile.IKineticSource;
import ic2.core.block.tileentity.TileEntityInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityAbstractKineticGenerator extends TileEntityInventory implements IKineticSource
{
	protected int kuBuffer;
	protected int maxKuBuffer;
	protected int updateTicker;

	public TileEntityAbstractKineticGenerator(BlockEntityType<? extends TileEntityInventory> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	protected int getTickRate()
	{
		return 20;
	}

	@Override
	public int maxrequestkineticenergyTick(Direction directionFrom)
	{
		return this.getConnectionBandwidth(directionFrom);
	}

	@Override
	public int requestkineticenergy(Direction directionFrom, int requestKineticEnergy)
	{
		return this.drawKineticEnergy(directionFrom, requestKineticEnergy, false);
	}
}
