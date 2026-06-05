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

   public TileEntityClassicCable() {
   }

   @Override
   protected List<AxisAlignedBB> getAabbs(boolean forCollision) {
      boolean cheat = false;
      if (forCollision && this.cableType == CableType.tin) {
         cheat = true;
         this.insulation = -1;
      }

      List<AxisAlignedBB> ret = super.getAabbs(forCollision);
      if (cheat) {
         this.insulation = 0;
      }

      return ret;
   }

   @Override
   public boolean tryAddInsulation() {
      return this.cableType != CableType.tin && super.tryAddInsulation();
   }

   @Override
   public double getConductionLoss() {
      return getConductionLoss(this.cableType, this.insulation);
   }

   public static double getConductionLoss(CableType type, int insulation) {
      switch (type) {
         case tin:
         case glass:
            return 0.025;
         case copper:
            return 0.3 - 0.1 * insulation;
         case gold:
            return 0.5 - 0.05 * insulation;
         case iron:
            return insulation <= 0 ? 1.0 : 1.0 - 0.05 * (1 << insulation - 1);
         case detector:
         case splitter:
            return 0.5;
         default:
            throw new IllegalStateException("Type was " + type + ", " + insulation);
      }
   }

   @Override
   public double getInsulationEnergyAbsorption() {
      switch (this.cableType) {
         case tin:
            assert this.insulation == 0;
            return 3.0;
         case glass:
         case detector:
         case splitter:
            return 9001.0;
         case copper:
         case gold:
            return EnergyNet.instance.getPowerFromTier(this.insulation);
         case iron:
            if (this.insulation == 0) {
               return 0.0;
            }

            return EnergyNet.instance.getPowerFromTier(this.insulation + 1);
         default:
            return super.getInsulationEnergyAbsorption();
      }
   }

   @Override
   public double getConductorBreakdownEnergy() {
      return getCableCapacity(this.cableType) + 1;
   }

   public static int getCableCapacity(CableType type) {
      switch (type) {
         case tin:
            return 5;
         case glass:
            return 512;
         case copper:
            return 32;
         case gold:
            return 128;
         case iron:
         case detector:
         case splitter:
            return 2048;
         default:
            throw new IllegalStateException("Type was " + type);
      }
   }
}
