package ic2.api.energy.tile;

import net.minecraft.util.EnumFacing;

public interface IEnergySink extends IEnergyAcceptor {
   double getDemandedEnergy();

   int getSinkTier();

   double injectEnergy(EnumFacing var1, double var2, double var4);
}
