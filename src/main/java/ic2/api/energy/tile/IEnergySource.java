package ic2.api.energy.tile;

public interface IEnergySource extends IEnergyEmitter {
  double getOfferedEnergy();
  
  void drawEnergy(double paramDouble);
  
  int getSourceTier();
}
