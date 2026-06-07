package ic2.api.network;

import ic2.api.util.CoreAccessRef;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class NetworkHelper
{
	private static INetworkManager serverInstance;
	private static INetworkManager clientInstance;

	public static void updateTileEntityField(BlockEntity te, String field)
	{
		getNetworkManager(te.getLevel().isClientSide).updateTileEntityField(te, field);
	}

	public static void initiateTileEntityEvent(BlockEntity te, int event, boolean limitRange)
	{
		getNetworkManager(te.getLevel().isClientSide).initiateTileEntityEvent(te, event, limitRange);
	}

	public static void initiateItemEvent(Player player, ItemStack stack, int event, boolean limitRange)
	{
		getNetworkManager(player.getCommandSenderWorld().isClientSide).initiateItemEvent(player, stack, event, limitRange);
	}

	public static void sendInitialData(BlockEntity te)
	{
		getNetworkManager(te.getLevel().isClientSide).sendInitialData(te);
	}

	public static void initiateClientTileEntityEvent(BlockEntity te, int event)
	{
		getNetworkManager(te.getLevel().isClientSide).initiateClientTileEntityEvent(te, event);
	}

	public static void initiateClientItemEvent(ItemStack stack, int event)
	{
		getNetworkManager(true).initiateClientItemEvent(stack, event);
	}

	public static INetworkManager getNetworkManager(boolean forClient)
	{
		INetworkManager ret;
		if (forClient)
		{
			ret = clientInstance;
			if (ret == null)
			{
				clientInstance = ret = CoreAccessRef.get().getClientNetworkManager();
			}
		} else
		{
			ret = serverInstance;
			if (ret == null)
			{
				serverInstance = ret = CoreAccessRef.get().getServerNetworkManager();
			}
		}

		return ret;
	}
}
