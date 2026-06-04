// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.network;

import java.util.concurrent.ConcurrentHashMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import io.netty.channel.ChannelHandlerContext;
import java.util.Iterator;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import java.io.IOException;
import ic2.core.util.LogCategory;
import ic2.api.network.IGrowingBuffer;
import net.minecraft.entity.player.EntityPlayerMP;
import ic2.core.IC2;
import java.util.concurrent.ConcurrentMap;
import io.netty.channel.ChannelHandler;
import net.minecraft.network.Packet;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class RpcHandler extends SimpleChannelInboundHandler<Packet<?>>
{
    private static ConcurrentMap<String, IRpcProvider<?>> providers;
    private static ConcurrentMap<Integer, Rpc<?>> pending;
    
    public static boolean registerProvider(final IRpcProvider<?> provider) {
        return RpcHandler.providers.putIfAbsent(provider.getClass().getName(), provider) == null;
    }
    
    public static <V> Rpc<V> run(final Class<? extends IRpcProvider<V>> provider, final Object... args) {
        final int id = IC2.random.nextInt();
        final Rpc<V> rpc = new Rpc<V>();
        final Rpc<V> prev = (Rpc<V>)RpcHandler.pending.putIfAbsent(id, rpc);
        if (prev != null) {
            return (Rpc<V>)run((Class<? extends IRpcProvider<Object>>)provider, args);
        }
        IC2.network.get(false).initiateRpc(id, provider, args);
        return rpc;
    }
    
    protected static void processRpcRequest(final GrowingBuffer is, final EntityPlayerMP player) throws IOException {
        final int id = is.readInt();
        final String providerClassName = is.readString();
        final Object[] args = (Object[])DataEncoder.decode(is);
        final IRpcProvider<?> provider = RpcHandler.providers.get(providerClassName);
        if (provider == null) {
            IC2.log.warn(LogCategory.Network, "Invalid RPC request from %s.", player.getName());
            return;
        }
        final Object result = provider.executeRpc(args);
        final GrowingBuffer buffer = new GrowingBuffer(256);
        SubPacketType.Rpc.writeTo(buffer);
        buffer.writeInt(id);
        DataEncoder.encode(buffer, result, true);
        buffer.flip();
        IC2.network.get(true).sendPacket(buffer, true, player);
    }
    
    public RpcHandler() {
        MinecraftForge.EVENT_BUS.register((Object)this);
    }
    
    @SubscribeEvent
    public void onConnect(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
        final String nettyHandlerName = "ic2_rpc_handler";
        if (event.getManager().channel().pipeline().get("ic2_rpc_handler") == null) {
            try {
                event.getManager().channel().pipeline().addBefore("packet_handler", "ic2_rpc_handler", (ChannelHandler)this);
            }
            catch (final Exception e) {
                throw new RuntimeException("Can't insert handler in " + event.getManager().channel().pipeline().names() + ".", e);
            }
        }
    }
    
    @SubscribeEvent
    public void onDisconnect(final FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        for (final Rpc<?> rpc : RpcHandler.pending.values()) {
            rpc.cancel(true);
        }
        RpcHandler.pending.clear();
    }
    
    protected void channelRead0(final ChannelHandlerContext ctx, final Packet<?> oPacket) throws Exception {
        FMLProxyPacket packet = null;
        if (oPacket instanceof FMLProxyPacket) {
            packet = (FMLProxyPacket)oPacket;
        }
        else if (oPacket instanceof SPacketCustomPayload) {
            packet = new FMLProxyPacket((SPacketCustomPayload)oPacket);
        }
        if (packet == null || !packet.channel().equals("ic2")) {
            ctx.fireChannelRead((Object)oPacket);
            return;
        }
        final ByteBuf payload = packet.payload();
        if (payload.isReadable() && payload.getByte(0) == SubPacketType.Rpc.getId()) {
            this.processRpcResponse(GrowingBuffer.wrap(packet.payload()));
        }
        else {
            ctx.fireChannelRead((Object)oPacket);
        }
    }
    
    private void processRpcResponse(final GrowingBuffer buffer) {
        try {
            buffer.readByte();
            final int id = buffer.readInt();
            final Object result = DataEncoder.decode(buffer);
            final Rpc<?> rpc = RpcHandler.pending.remove(id);
            if (rpc == null) {
                IC2.log.warn(LogCategory.Network, "RPC %d wasn't found while trying to process its response.", id);
            }
            else {
                rpc.finish(result);
            }
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    static {
        RpcHandler.providers = new ConcurrentHashMap<String, IRpcProvider<?>>();
        RpcHandler.pending = new ConcurrentHashMap<Integer, Rpc<?>>();
    }
}
