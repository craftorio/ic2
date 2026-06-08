package ic2.core.network;

import ic2.core.IC2;
import ic2.core.util.LogCategory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.server.level.ServerPlayer;

public class RpcHandler
{
	private static ConcurrentMap<String, IRpcProvider<?>> providers = new ConcurrentHashMap<>();
	private static ConcurrentMap<Integer, Rpc<?>> pending = new ConcurrentHashMap<>();

	public static boolean registerProvider(IRpcProvider<?> provider)
	{
		return providers.putIfAbsent(provider.getClass().getName(), provider) == null;
	}

	public static <V> Rpc<V> run(Class<? extends IRpcProvider<V>> provider, Object... args)
	{
		Rpc<V> rpc = new Rpc<>();

		int id;
		Rpc<?> existing;
		do
		{
			existing = pending.putIfAbsent(id = IC2.random.nextInt(), rpc);
		} while (existing != null);

		IC2.network.get(false).initiateRpc(id, provider, args);
		return rpc;
	}

	static void processRpcRequest(GrowingBuffer is, ServerPlayer player) throws IOException
	{
		int id = is.readInt();
		String providerClassName = is.readString();
		IRpcProvider<?> provider = providers.get(providerClassName);
		if (provider == null)
		{
			IC2.log.warn(LogCategory.Network, "Invalid RPC request from %s.", player.getName());
		} else
		{
			Object[] args = (Object[]) DataEncoder.decode(is);
			Object result = provider.executeRpc(args);
			GrowingBuffer buffer = new GrowingBuffer(256);
			SubPacketType.Rpc.writeTo(buffer);
			buffer.writeInt(id);
			DataEncoder.encode(buffer, result, true);
			buffer.flip();
			IC2.network.get(true).sendS2CPacket(player, buffer, true);
		}
	}

	public static void onDisconnect()
	{
		for (Rpc<?> rpc : pending.values())
		{
			rpc.cancel(true);
		}

		pending.clear();
	}

	static void processRpcResponse(GrowingBuffer buffer) throws IOException
	{
		int id = buffer.readInt();
		Rpc<?> rpc = pending.remove(id);
		if (rpc == null)
		{
			IC2.log.warn(LogCategory.Network, "RPC %d wasn't found while trying to process its response.", id);
		} else
		{
			Object result = DataEncoder.decode(buffer);
			rpc.finish(result);
		}
	}
}
