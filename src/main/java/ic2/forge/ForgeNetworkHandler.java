package ic2.forge;

import ic2.core.IC2;
import ic2.core.network.Ic2NetworkPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

final class ForgeNetworkHandler {
	static final IPayloadHandler<Ic2NetworkPayload> HANDLER = (payload, context) -> {
		boolean simulating = context.flow() == PacketFlow.SERVERBOUND;
		context.enqueueWork(() -> {
			IC2.network.get(simulating).onPacket(Unpooled.wrappedBuffer(payload.data()),
					simulating ? context.player() : null);
		});
	};
}
