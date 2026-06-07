package ic2.core.block.reactor.tileentity;

import ic2.api.reactor.IReactorChamber;
import ic2.core.block.comp.FluidReactorLookup;
import ic2.core.block.tileentity.Ic2TileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityReactorVessel extends Ic2TileEntity implements IReactorChamber
{
	protected final FluidReactorLookup lookup = this.addComponent(new FluidReactorLookup(this));

	protected TileEntityReactorVessel(BlockEntityType<? extends TileEntityReactorVessel> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	public TileEntityNuclearReactorElectric getReactorInstance()
	{
		return this.lookup.getReactor();
	}

	@Override
	public boolean isWall()
	{
		return true;
	}
}
