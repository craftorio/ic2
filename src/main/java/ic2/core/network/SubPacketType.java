// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.network;

import ic2.core.util.LogCategory;
import ic2.core.IC2;

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
    private static final SubPacketType[] values;
    
    private SubPacketType(final boolean serverToClient, final boolean clientToServer) {
        this.serverToClient = serverToClient;
        this.clientToServer = clientToServer;
    }
    
    public void writeTo(final GrowingBuffer out) {
        out.writeByte(this.getId());
    }
    
    public int getId() {
        return this.ordinal() + 1;
    }
    
    public static SubPacketType read(final GrowingBuffer in, final boolean simulating) {
        final int id = in.readUnsignedByte() - 1;
        if (id < 0 || id >= SubPacketType.values.length) {
            IC2.log.warn(LogCategory.Network, "Invalid sub packet type: %d", id);
            return null;
        }
        final SubPacketType ret = SubPacketType.values[id];
        if ((simulating && !ret.clientToServer) || (!simulating && !ret.serverToClient)) {
            IC2.log.warn(LogCategory.Network, "Invalid sub packet type %s for side %s", ret.name(), simulating ? "server" : "client");
            return null;
        }
        return ret;
    }
    
    static {
        values = values();
        if (SubPacketType.values.length > 255) {
            throw new RuntimeException("too many sub packet types");
        }
    }
}
