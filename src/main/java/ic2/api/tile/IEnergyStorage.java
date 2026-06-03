package ic2.api.tile;

import net.minecraft.util.EnumFacing;

public interface IEnergyStorage {
  int getStored();
  
  void setStored(int paramInt);
  
  int addEnergy(int paramInt);
  
  int getCapacity();
  
  int getOutput();
  
  double getOutputEnergyUnitsPerTick();
  
  boolean isTeleporterCompatible(EnumFacing paramEnumFacing);
}
