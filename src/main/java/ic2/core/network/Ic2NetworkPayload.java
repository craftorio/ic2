package ic2.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record Ic2NetworkPayload(byte[] data) implements CustomPacketPayload {
	public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("ic2", "m");

	public static final CustomPacketPayload.Type<Ic2NetworkPayload> TYPE = new CustomPacketPayload.Type<>(ID);

	public static final StreamCodec<FriendlyByteBuf, Ic2NetworkPayload> STREAM_CODEC = StreamCodec.of(
			(buf, payload) -> buf.writeBytes(payload.data()),
			buf -> {
				byte[] bytes = new byte[buf.readableBytes()];
				buf.readBytes(bytes);
				return new Ic2NetworkPayload(bytes);
			});

	@Override
	public Type<Ic2NetworkPayload> type() {
		return TYPE;
	}
}
