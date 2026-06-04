// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import ic2.core.block.BlockIC2Fluid;
import net.minecraftforge.event.entity.player.FillBucketEvent;

public class IC2BucketHandler
{
    @SubscribeEvent
    public void onBucketFill(final FillBucketEvent event) {
        if (event.getTarget() != null) {
            final Block block = event.getWorld().getBlockState(event.getTarget().getBlockPos()).getBlock();
            if (block instanceof BlockIC2Fluid && event.isCancelable()) {
                event.setCanceled(true);
            }
        }
    }
}
