package ic2.api.info;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ILocatable {
  BlockPos getPosition();
  
  World getWorldObj();
}
