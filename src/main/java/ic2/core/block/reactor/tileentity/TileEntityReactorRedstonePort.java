package ic2.core.block.reactor.tileentity;

import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.profile.NotClassic;

@NotClassic
public class TileEntityReactorRedstonePort extends TileEntityReactorVessel {
  public final Redstone redstone = (Redstone)addComponent((TileEntityComponent)new Redstone(this));
  
  protected void onLoaded() {
    super.onLoaded();
    updateRedstoneLink();
  }
  
  private void updateRedstoneLink() {
    if ((func_145831_w()).field_72995_K)
      return; 
    TileEntityNuclearReactorElectric reactor = this.lookup.getReactor();
    if (reactor != null)
      this.redstone.linkTo(reactor.redstone); 
  }
}
