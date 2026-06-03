package ic2.core.block.wiring;

import ic2.core.IC2;
import ic2.core.ref.TeBlock.Delegated;

@Delegated(current = TileEntityElectricMFE.class, old = TileEntityElectricMFE.TileEntityElectricClassicMFE.class)
public class TileEntityElectricMFE extends TileEntityElectricBlock {
  @Delegated(current = TileEntityElectricMFE.class, old = TileEntityElectricClassicMFE.class)
  public static class TileEntityElectricClassicMFE extends TileEntityElectricBlock {
    public TileEntityElectricClassicMFE() {
      super(2, 128, 600000);
      this.chargeSlot.setTier(3);
      this.dischargeSlot.setTier(3);
    }
  }
  
  public static Class<? extends TileEntityElectricBlock> delegate() {
    return IC2.version.isClassic() ? (Class)TileEntityElectricClassicMFE.class : (Class)TileEntityElectricMFE.class;
  }
  
  public TileEntityElectricMFE() {
    super(3, 512, 4000000);
  }
}
