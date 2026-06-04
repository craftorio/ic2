package ic2.core.block.comp;

import ic2.core.block.TileEntityBlock;
import net.minecraft.block.Block;

public class ComparatorEmitter extends BasicRedstoneComponent {
  public ComparatorEmitter(TileEntityBlock parent) {
    super(parent);
  }
  
  public void onChange() {
    this.parent.getWorld().func_175666_e(this.parent.getPos(), (Block)this.parent.func_145838_q());
  }
}
