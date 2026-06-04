package ic2.core.block.comp;

import ic2.core.block.TileEntityBlock;
import net.minecraft.block.Block;

public class RedstoneEmitter extends BasicRedstoneComponent {
  public RedstoneEmitter(TileEntityBlock parent) {
    super(parent);
  }
  
  public void onChange() {
    this.parent.getWorld().func_175685_c(this.parent.getPos(), (Block)this.parent.func_145838_q(), false);
  }
}
