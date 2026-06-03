package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.core.util.Ic2Color;
import java.util.List;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityClassicCable extends TileEntityCable {
  public TileEntityClassicCable(CableType cableType, int insulation) {
    super(cableType, insulation);
  }
  
  public TileEntityClassicCable(CableType cableType, int insulation, Ic2Color color) {
    super(cableType, insulation, color);
  }
  
  public TileEntityClassicCable() {}
  
  protected List<AxisAlignedBB> getAabbs(boolean forCollision) {
    boolean cheat = false;
    if (forCollision && this.cableType == CableType.tin) {
      cheat = true;
      this.insulation = -1;
    } 
    List<AxisAlignedBB> ret = super.getAabbs(forCollision);
    if (cheat)
      this.insulation = 0; 
    return ret;
  }
  
  public boolean tryAddInsulation() {
    return (this.cableType != CableType.tin && super.tryAddInsulation());
  }
  
  public double getConductionLoss() {
    return getConductionLoss(this.cableType, this.insulation);
  }
  
  public static double getConductionLoss(CableType type, int insulation) {
    switch (type) {
      case tin:
      case glass:
        return 0.025D;
      case copper:
        return 0.3D - 0.1D * insulation;
      case gold:
        return 0.5D - 0.05D * insulation;
      case iron:
        return (insulation <= 0) ? 1.0D : (1.0D - 0.05D * (1 << insulation - 1));
      case detector:
      case splitter:
        return 0.5D;
    } 
    throw new IllegalStateException("Type was " + type + ", " + insulation);
  }
  
  public double getInsulationEnergyAbsorption() {
    switch (this.cableType) {
      case tin:
        assert this.insulation == 0;
        return 3.0D;
      case copper:
      case gold:
        return EnergyNet.instance.getPowerFromTier(this.insulation);
      case iron:
        if (this.insulation == 0)
          return 0.0D; 
        return EnergyNet.instance.getPowerFromTier(this.insulation + 1);
      case glass:
      case detector:
      case splitter:
        return 9001.0D;
    } 
    return super.getInsulationEnergyAbsorption();
  }
  
  public double getConductorBreakdownEnergy() {
    return (getCableCapacity(this.cableType) + 1);
  }
  
  public static int getCableCapacity(CableType type) {
    switch (type) {
      case tin:
        return 5;
      case copper:
        return 32;
      case gold:
        return 128;
      case glass:
        return 512;
      case iron:
      case detector:
      case splitter:
        return 2048;
    } 
    throw new IllegalStateException("Type was " + type);
  }
}
