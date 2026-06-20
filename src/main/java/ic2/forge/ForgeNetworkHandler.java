package ic2.forge;

import ic2.core.IC2;
import net.neoforged.bus.api.SubscribeEvent;
final class ForgeNetworkHandler
{
	@SubscribeEvent
	public void onS2CPacket(NetworkEvent.ServerCustomPayloadEvent event)
	{
		NetworkEvent.Context context = event.getSource().get();
		IC2.network.get(false).onPacket(event.getPayload(), IC2.sideProxy.getPlayerInstance());
		context.setPacketHandled(true);
	}

	@SubscribeEvent
	public void onC2SPacket(NetworkEvent.ClientCustomPayloadEvent event)
	{
		NetworkEvent.Context context = event.getSource().get();
		IC2.network.get(true).onPacket(event.getPayload(), context.getSender());
		context.setPacketHandled(true);
	}
}
