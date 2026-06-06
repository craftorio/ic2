package ic2.core.block.reactor.tileentity;

import ic2.api.reactor.IReactorChamber;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.FluidReactorLookup;

public class TileEntityReactorVessel extends TileEntityBlock implements IReactorChamber
{
	protected final FluidReactorLookup lookup = this.addComponent(new FluidReactorLookup(this));

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
