package ic2.core.block.invslot;

import ic2.core.IC2;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.network.IRpcProvider;
import ic2.core.network.RpcHandler;
import ic2.core.util.StackUtil;
import ic2.core.uu.UuGraph;
import ic2.core.uu.UuIndex;

import net.minecraft.world.item.ItemStack;

public class InvSlotScannable extends InvSlotConsumable
{
	public InvSlotScannable(IInventorySlotHolder<?> base1, String name1, int count)
	{
		super(base1, name1, count);
		this.setStackSizeLimit(1);
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		if (IC2.sideProxy.isSimulating())
		{
			return isValidStack(stack);
		}

		// Client side: accept everything, server will validate via isValidStack
		return true;
	}

	private static boolean isValidStack(ItemStack stack)
	{
		stack = UuGraph.find(stack);
		return !StackUtil.isEmpty(stack) && UuIndex.instance.get(stack) < Double.POSITIVE_INFINITY;
	}

	static
	{
		RpcHandler.registerProvider(new InvSlotScannable.ServerScannableCheck());
	}

	public static class ServerScannableCheck implements IRpcProvider<Boolean>
	{
		public Boolean executeRpc(Object... args)
		{
			ItemStack stack = (ItemStack) args[0];
			return InvSlotScannable.isValidStack(stack);
		}
	}
}
