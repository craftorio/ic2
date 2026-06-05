package ic2.core.block.comp;

import ic2.core.block.TileEntityBlock;

public class RedstoneEmitter extends BasicRedstoneComponent {
   public RedstoneEmitter(TileEntityBlock parent) {
      super(parent);
   }

   @Override
   public void onChange() {
      this.parent.getWorld().notifyNeighborsOfStateChange(this.parent.getPos(), this.parent.getBlockType(), false);
   }
}
