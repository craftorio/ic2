package ic2.core;

import net.minecraft.block.Block;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class IC2BucketHandler {
  @SubscribeEvent
  public void onBucketFill(FillBucketEvent event) {
    if (event.getTarget() != null) {
      Block block = event.getWorld().func_180495_p(event.getTarget().func_178782_a()).func_177230_c();
      if (block instanceof ic2.core.block.BlockIC2Fluid && event.isCancelable())
        event.setCanceled(true); 
    } 
  }
}
