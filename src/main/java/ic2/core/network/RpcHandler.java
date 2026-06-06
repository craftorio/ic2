package ic2.core.network;

import ic2.core.IC2;
import ic2.core.util.LogCategory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

@Sharable
public class RpcHandler extends SimpleChannelInboundHandler<Packet<?>>
{
	private static final ConcurrentMap<String, IRpcProvider<?>> providers = new ConcurrentHashMap<>();
	private static final ConcurrentMap<Integer, Rpc<?>> pending = new ConcurrentHashMap<>();

	public static boolean registerProvider(IRpcProvider<?> provider)
	{
		return providers.putIfAbsent(provider.getClass().getName(), provider) == null;
	}

	public static <V> Rpc<V> run(Class<? extends IRpcProvider<V>> provider, Object... args)
	{
		int id = IC2.random.nextInt();
		Rpc<V> rpc = new Rpc<>();
		Rpc<V> prev = (Rpc<V>) pending.putIfAbsent(id, rpc);
		if (prev != null)
		{
			return run(provider, args);
		}

		IC2.network.get(false).initiateRpc(id, provider, args);
		return rpc;
	}

	protected static void processRpcRequest(GrowingBuffer is, EntityPlayerMP player) throws IOException
	{
		int id = is.readInt();
		String providerClassName = is.readString();
		Object[] args = (Object[]) DataEncoder.decode(is);
		IRpcProvider<?> provider = providers.get(providerClassName);
		if (provider == null)
		{
			IC2.log.warn(LogCategory.Network, "Invalid RPC request from %s.", player.getName());
		} else
		{
			Object result = provider.executeRpc(args);
			GrowingBuffer buffer = new GrowingBuffer(256);
			SubPacketType.Rpc.writeTo(buffer);
			buffer.writeInt(id);
			DataEncoder.encode(buffer, result, true);
			buffer.flip();
			IC2.network.get(true).sendPacket(buffer, true, player);
		}
	}

	public RpcHandler()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event)
	{
		String nettyHandlerName = "ic2_rpc_handler";
		if (event.getManager().channel().pipeline().get("ic2_rpc_handler") == null)
		{
			try
			{
				event.getManager().channel().pipeline().addBefore("packet_handler", "ic2_rpc_handler", this);
			} catch (Exception e)
			{
				throw new RuntimeException("Can't insert handler in " + event.getManager().channel().pipeline().names() + ".", e);
			}
		}
	}

	@SubscribeEvent
	public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
	{
		for (Rpc<?> rpc : pending.values())
		{
			rpc.cancel(true);
		}

		pending.clear();
	}

	protected void channelRead0(ChannelHandlerContext ctx, Packet<?> oPacket) throws Exception
	{
		FMLProxyPacket packet = null;
		if (oPacket instanceof FMLProxyPacket)
		{
			packet = (FMLProxyPacket) oPacket;
		} else if (oPacket instanceof SPacketCustomPayload)
		{
			packet = new FMLProxyPacket((SPacketCustomPayload) oPacket);
		}

		if (packet != null && packet.channel().equals("ic2"))
		{
			ByteBuf payload = packet.payload();
			if (payload.isReadable() && payload.getByte(0) == SubPacketType.Rpc.getId())
			{
				this.processRpcResponse(GrowingBuffer.wrap(packet.payload()));
			} else
			{
				ctx.fireChannelRead(oPacket);
			}
		} else
		{
			ctx.fireChannelRead(oPacket);
		}
	}

	private void processRpcResponse(GrowingBuffer buffer)
	{
		try
		{
			buffer.readByte();
			int id = buffer.readInt();
			Object result = DataEncoder.decode(buffer);
			Rpc<?> rpc = pending.remove(id);
			if (rpc == null)
			{
				IC2.log.warn(LogCategory.Network, "RPC %d wasn't found while trying to process its response.", id);
			} else
			{
				rpc.finish(result);
			}
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
