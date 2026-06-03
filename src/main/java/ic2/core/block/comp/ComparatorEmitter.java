package ic2.core.block.comp;

import ic2.core.block.TileEntityBlock;
import net.minecraft.block.Block;

public class ComparatorEmitter extends BasicRedstoneComponent {
  public ComparatorEmitter(TileEntityBlock parent) {
    super(parent);
  }
  
  public void onChange() {
    this.parent.func_145831_w().func_175666_e(this.parent.func_174877_v(), (Block)this.parent.getBlockType());
  }
}
