package ic2.core;

import net.minecraft.world.World;

public interface IWorldTickCallback {
  void onTick(World paramWorld);
}
