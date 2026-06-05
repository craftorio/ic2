package ic2.core.block.beam;

import ic2.core.block.machine.tileentity.TileEntityElectricMachine;

public class TileEmitter extends TileEntityElectricMachine {
   private int progress;

   public TileEmitter() {
      super(5000, 1);
   }

   @Override
   protected void updateEntityServer() {
      super.updateEntityServer();
      if (this.progress < 100) {
         this.progress++;
      }

      if (this.progress == 100 && this.getWorld().isBlockPowered(this.pos)) {
         this.progress = 0;
         this.getWorld().spawnEntity(new EntityParticle(this));
      }
   }
}
