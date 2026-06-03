package ic2.core.block.reactor.tileentity;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.FluidReactorLookup;
import ic2.core.block.comp.TileEntityComponent;

public class TileEntityReactorVessel extends TileEntityBlock implements IReactorChamber {
  protected final FluidReactorLookup lookup = (FluidReactorLookup)addComponent((TileEntityComponent)new FluidReactorLookup(this));
  
  public TileEntityNuclearReactorElectric getReactorInstance() {
    return this.lookup.getReactor();
  }
  
  public boolean isWall() {
    return true;
  }
}
