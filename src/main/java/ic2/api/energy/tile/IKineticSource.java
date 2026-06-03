package ic2.api.energy.tile;

import net.minecraft.util.EnumFacing;

public interface IKineticSource {
  @Deprecated
  int maxrequestkineticenergyTick(EnumFacing paramEnumFacing);
  
  default int getConnectionBandwidth(EnumFacing side) {
    return maxrequestkineticenergyTick(side);
  }
  
  @Deprecated
  int requestkineticenergy(EnumFacing paramEnumFacing, int paramInt);
  
  default int drawKineticEnergy(EnumFacing side, int request, boolean simulate) {
    return !simulate ? requestkineticenergy(side, request) : maxrequestkineticenergyTick(side);
  }
}
