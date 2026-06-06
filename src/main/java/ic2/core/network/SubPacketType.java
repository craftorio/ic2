package ic2.core.network;

import ic2.core.IC2;
import ic2.core.util.LogCategory;

public enum SubPacketType
{
	Rpc(true, true),
	TileEntityEvent(true, true),
	ItemEvent(true, true),
	PlayerItemData(true, true),
	ContainerData(true, true),
	ContainerEvent(true, true),
	HandHeldInvData(true, true),
	LargePacket(true, false),
	GuiDisplay(true, false),
	ExplosionEffect(true, false),
	TileEntityBlockComponent(true, false),
	TileEntityBlockLandEffect(true, false),
	TileEntityBlockRunEffect(true, false),
	KeyUpdate(false, true),
	TileEntityData(false, true),
	RequestGUI(false, true);

	private boolean serverToClient;
	private boolean clientToServer;
	private static final SubPacketType[] values = values();

	SubPacketType(boolean serverToClient, boolean clientToServer)
	{
		this.serverToClient = serverToClient;
		this.clientToServer = clientToServer;
	}

	public void writeTo(GrowingBuffer out)
	{
		out.writeByte(this.getId());
	}

	public int getId()
	{
		return this.ordinal() + 1;
	}

	public static SubPacketType read(GrowingBuffer in, boolean simulating)
	{
		int id = in.readUnsignedByte() - 1;
		if (id >= 0 && id < values.length)
		{
			SubPacketType ret = values[id];
			if (simulating && !ret.clientToServer || !simulating && !ret.serverToClient)
			{
				IC2.log.warn(LogCategory.Network, "Invalid sub packet type %s for side %s", ret.name(), simulating ? "server" : "client");
				return null;
			} else
			{
				return ret;
			}
		} else
		{
			IC2.log.warn(LogCategory.Network, "Invalid sub packet type: %d", id);
			return null;
		}
	}

	static
	{
		if (values.length > 255)
		{
			throw new RuntimeException("too many sub packet types");
		}
	}
}
