package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.IC2;
import ic2.core.block.comp.ComparatorEmitter;
import ic2.core.block.comp.RedstoneEmitter;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.ref.TeBlock.Delegated;
import ic2.core.util.Util;

@Delegated(current = TileEntityCableDetector.class, old = TileEntityClassicCableDetector.class)
public class TileEntityCableDetector extends TileEntityCable {
  private static final int tickRate = 32;
  
  protected final RedstoneEmitter rsEmitter;
  
  protected final ComparatorEmitter comparator;
  
  private int ticker;
  
  public static Class<? extends TileEntityCable> delegate() {
    return IC2.version.isClassic() ? (Class)TileEntityClassicCableDetector.class : (Class)TileEntityCableDetector.class;
  }
  
  public TileEntityCableDetector() {
    super(CableType.detector, 0);
    this.ticker = 0;
    this.rsEmitter = (RedstoneEmitter)addComponent((TileEntityComponent)new RedstoneEmitter(this));
    this.comparator = (ComparatorEmitter)addComponent((TileEntityComponent)new ComparatorEmitter(this));
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (++this.ticker % 32 == 0) {
      double energy = EnergyNet.instance.getNodeStats((IEnergyTile)this).getEnergyIn();
      if (energy > 0.0D) {
        setActive(true);
        this.rsEmitter.setLevel(15);
      } else {
        setActive(false);
        this.rsEmitter.setLevel(0);
      } 
      this.comparator.setLevel((int)Util.map(EnergyNet.instance.getNodeStats((IEnergyTile)this).getEnergyIn() / (getConductorBreakdownEnergy() - 1.0D), 1.0D, 15.0D));
    } 
  }
}
