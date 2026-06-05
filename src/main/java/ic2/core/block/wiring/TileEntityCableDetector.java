package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.core.IC2;
import ic2.core.block.comp.ComparatorEmitter;
import ic2.core.block.comp.RedstoneEmitter;
import ic2.core.ref.TeBlock;
import ic2.core.util.Util;

@TeBlock.Delegated(current = TileEntityCableDetector.class, old = TileEntityClassicCableDetector.class)
public class TileEntityCableDetector extends TileEntityCable {
   private static final int tickRate = 32;
   protected final RedstoneEmitter rsEmitter;
   protected final ComparatorEmitter comparator;
   private int ticker = 0;

   public static Class<? extends TileEntityCable> delegate() {
      return IC2.version.isClassic() ? TileEntityClassicCableDetector.class : TileEntityCableDetector.class;
   }

   public TileEntityCableDetector() {
      super(CableType.detector, 0);
      this.rsEmitter = this.addComponent(new RedstoneEmitter(this));
      this.comparator = this.addComponent(new ComparatorEmitter(this));
   }

   @Override
   protected void updateEntityServer() {
      super.updateEntityServer();
      if (++this.ticker % 32 == 0) {
         double energy = EnergyNet.instance.getNodeStats(this).getEnergyIn();
         if (energy > 0.0) {
            this.setActive(true);
            this.rsEmitter.setLevel(15);
         } else {
            this.setActive(false);
            this.rsEmitter.setLevel(0);
         }

         this.comparator.setLevel((int)Util.map(EnergyNet.instance.getNodeStats(this).getEnergyIn() / (this.getConductorBreakdownEnergy() - 1.0), 1.0, 15.0));
      }
   }
}
