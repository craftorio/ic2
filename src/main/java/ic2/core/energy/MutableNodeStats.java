package ic2.core.energy;

import ic2.api.energy.NodeStats;

class MutableNodeStats extends NodeStats {
  protected MutableNodeStats() {
    super(0.0D, 0.0D, 0.0D);
  }
  
  protected void set(double energyIn, double energyOut, double voltage) {
    this.energyIn = energyIn;
    this.energyOut = energyOut;
    this.voltage = voltage;
  }
}
