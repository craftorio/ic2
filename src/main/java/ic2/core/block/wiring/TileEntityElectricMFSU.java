package ic2.core.block.wiring;

import ic2.core.IC2;
import ic2.core.ref.TeBlock.Delegated;

@Delegated(current = TileEntityElectricMFSU.class, old = TileEntityElectricMFSU.TileEntityElectricClassicMFSU.class)
public class TileEntityElectricMFSU extends TileEntityElectricBlock {
  @Delegated(current = TileEntityElectricMFSU.class, old = TileEntityElectricClassicMFSU.class)
  public static class TileEntityElectricClassicMFSU extends TileEntityElectricBlock {
    public TileEntityElectricClassicMFSU() {
      super(3, 512, 10000000);
      this.chargeSlot.setTier(4);
      this.dischargeSlot.setTier(4);
    }
  }
  
  public static Class<? extends TileEntityElectricBlock> delegate() {
    return IC2.version.isClassic() ? (Class)TileEntityElectricClassicMFSU.class : (Class)TileEntityElectricMFSU.class;
  }
  
  public TileEntityElectricMFSU() {
    super(4, 2048, 40000000);
  }
}
