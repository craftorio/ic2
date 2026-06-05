package ic2.api.energy.tile;

import net.minecraft.util.EnumFacing;

public interface IKineticSource {
   @Deprecated
   int maxrequestkineticenergyTick(EnumFacing var1);

   default int getConnectionBandwidth(EnumFacing side) {
      return this.maxrequestkineticenergyTick(side);
   }

   @Deprecated
   int requestkineticenergy(EnumFacing var1, int var2);

   default int drawKineticEnergy(EnumFacing side, int request, boolean simulate) {
      return !simulate ? this.requestkineticenergy(side, request) : this.maxrequestkineticenergyTick(side);
   }
}
