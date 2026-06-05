package ic2.api.energy.tile;

import net.minecraft.util.EnumFacing;

public interface IHeatSource {
   @Deprecated
   int maxrequestHeatTick(EnumFacing var1);

   default int getConnectionBandwidth(EnumFacing side) {
      return this.maxrequestHeatTick(side);
   }

   @Deprecated
   int requestHeat(EnumFacing var1, int var2);

   default int drawHeat(EnumFacing side, int request, boolean simulate) {
      return !simulate ? this.requestHeat(side, request) : this.maxrequestHeatTick(side);
   }
}
