package ic2.core;

import ic2.core.block.BlockIC2Fluid;
import net.minecraft.block.Block;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class IC2BucketHandler
{
	@SubscribeEvent
	public void onBucketFill(FillBucketEvent event)
	{
		if (event.getTarget() != null)
		{
			Block block = event.getWorld().getBlockState(event.getTarget().getBlockPos()).getBlock();
			if (block instanceof BlockIC2Fluid && event.isCancelable())
			{
				event.setCanceled(true);
			}
		}
	}
}
