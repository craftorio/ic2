package ic2.core.item.tfbp;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

abstract class TerraformerBase {
  abstract boolean terraform(World paramWorld, BlockPos paramBlockPos);
  
  void init() {}
}
