package ic2.api.energy.tile;

import net.minecraft.util.EnumFacing;

public interface IHeatSource {
  @Deprecated
  int maxrequestHeatTick(EnumFacing paramEnumFacing);
  
  default int getConnectionBandwidth(EnumFacing side) {
    return maxrequestHeatTick(side);
  }
  
  @Deprecated
  int requestHeat(EnumFacing paramEnumFacing, int paramInt);
  
  default int drawHeat(EnumFacing side, int request, boolean simulate) {
    return !simulate ? requestHeat(side, request) : maxrequestHeatTick(side);
  }
}
