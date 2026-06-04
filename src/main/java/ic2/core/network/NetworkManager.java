// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.world.WorldServer;
import java.util.Collection;
import ic2.core.ExplosionIC2;
import net.minecraft.util.math.Vec3d;
import net.minecraft.item.Item;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.network.INetworkItemEventListener;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import ic2.core.block.ITeBlock;
import net.minecraft.world.World;
import ic2.core.item.tool.HandHeldInventory;
import net.minecraft.inventory.IContainerListener;
import ic2.core.ContainerBase;
import java.util.List;
import ic2.core.util.Util;
import ic2.api.network.INetworkDataProvider;
import ic2.core.item.IHandHeldSubInventory;
import net.minecraft.server.MinecraftServer;
import ic2.core.item.IHandHeldInventory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import ic2.core.IHasGui;
import ic2.core.util.StackUtil;
import java.util.Iterator;
import java.util.ArrayList;
import ic2.core.block.TileEntityBlock;
import ic2.api.network.ClientModifiable;
import ic2.core.util.ReflectionUtil;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import ic2.api.network.IGrowingBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import java.io.IOException;
import ic2.core.WorldData;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import java.lang.reflect.Field;
import ic2.api.network.INetworkManager;

public class NetworkManager implements INetworkManager
{
    private static Field playerInstancePlayers;
    private static FMLEventChannel channel;
    private static final int maxPacketDataLength = 32766;
    public static final String channelName = "ic2";
    
    public NetworkManager() {
        if (NetworkManager.channel == null) {
            NetworkManager.channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("ic2");
        }
        NetworkManager.channel.register((Object)this);
    }
    
    protected boolean isClient() {
        return false;
    }
    
    public void onTickEnd(final WorldData worldData) {
        try {
            TeUpdate.send(worldData, this);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public final void sendPlayerItemData(final EntityPlayer player, final int slot, final Object... data) {
        final GrowingBuffer buffer = new GrowingBuffer(256);
        try {
            SubPacketType.PlayerItemData.writeTo(buffer);
            buffer.writeByte(slot);
            DataEncoder.encode(buffer, ((ItemStack)player.inventory.mainInventory.get(slot)).getItem(), false);
            buffer.writeVarInt(data.length);
            for (final Object o : data) {
                DataEncoder.encode(buffer, o);
            }
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
        buffer.flip();
        if (!this.isClient()) {
            this.sendPacket(buffer, true, (EntityPlayerMP)player);
        }
        else {
            this.sendPacket(buffer);
        }
    }
    
    @Override
    public final void updateTileEntityField(final TileEntity te, final String field) {
        if (!this.isClient()) {
            getTeUpdateData(te).addGlobalField(field);
        }
        else if (this.getClientModifiableField(te.getClass(), field) == null) {
            IC2.log.warn(LogCategory.Network, "Field update for %s failed.", te);
        }
        else {
            final GrowingBuffer buffer = new GrowingBuffer(64);
            try {
                SubPacketType.TileEntityData.writeTo(buffer);
                DataEncoder.encode(buffer, te, false);
                writeFieldData(te, field, buffer);
            }
            catch (final IOException e) {
                throw new RuntimeException(e);
            }
            buffer.flip();
            this.sendPacket(buffer);
        }
    }
    
    private Field getClientModifiableField(final Class<?> cls, final String fieldName) {
        final Field field = ReflectionUtil.getFieldRecursive(cls, fieldName);
        if (field == null) {
            IC2.log.warn(LogCategory.Network, "Can't find field %s in %s.", fieldName, cls.getName());
            return null;
        }
        if (field.getAnnotation(ClientModifiable.class) == null) {
            IC2.log.warn(LogCategory.Network, "The field %s in %s is not modifiable.", fieldName, cls.getName());
            return null;
        }
        return field;
    }
    
    private static TeUpdateDataServer getTeUpdateData(final TileEntity te) {
        assert IC2.platform.isSimulating();
        if (te == null) {
            throw new NullPointerException();
        }
        final WorldData worldData = WorldData.get(te.getWorld());
        TeUpdateDataServer ret = worldData.tesToUpdate.get(te);
        if (ret == null) {
            ret = new TeUpdateDataServer();
            worldData.tesToUpdate.put(te, ret);
        }
        return ret;
    }
    
    public final void updateTileEntityFieldTo(final TileEntity te, final String field, final EntityPlayerMP player) {
        assert !this.isClient();
        getTeUpdateData(te).addPlayerField(field, player);
    }
    
    public final void sendComponentUpdate(final TileEntityBlock te, final String componentName, final EntityPlayerMP player, final GrowingBuffer data) {
        assert !this.isClient();
        if (player.getEntityWorld() != te.getWorld()) {
            throw new IllegalArgumentException("mismatched world (te " + te.getWorld() + ", player " + player.getEntityWorld() + ")");
        }
        final GrowingBuffer buffer = new GrowingBuffer(64);
        try {
            SubPacketType.TileEntityBlockComponent.writeTo(buffer);
            DataEncoder.encode(buffer, te, false);
            buffer.writeString(componentName);
            buffer.writeVarInt(data.available());
            data.writeTo(buffer);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
        buffer.flip();
        this.sendPacket(buffer, true, player);
    }
    
    @Override
    public final void initiateTileEntityEvent(final TileEntity te, final int event, final boolean limitRange) {
        assert !this.isClient();
        if (te.getWorld().playerEntities.isEmpty()) {
            return;
        }
        final GrowingBuffer buffer = new GrowingBuffer(32);
        try {
            SubPacketType.TileEntityEvent.writeTo(buffer);
            DataEncoder.encode(buffer, te, false);
            buffer.writeInt(event);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
        buffer.flip();
        for (final EntityPlayerMP target : getPlayersInRange(te.getWorld(), te.getPos(), new ArrayList())) {
            if (limitRange) {
                final int dX = (int)(te.getPos().getX() + 0.5 - target.posX);
                final int dZ = (int)(te.getPos().getZ() + 0.5 - target.posZ);
                if (dX * dX + dZ * dZ > 400) {
                    continue;
                }
            }
            this.sendPacket(buffer, false, target);
        }
    }
    
    @Override
    public final void initiateItemEvent(final EntityPlayer player, final ItemStack stack, final int event, final boolean limitRange) {
        if (StackUtil.isEmpty(stack)) {
            throw new NullPointerException("invalid stack: " + StackUtil.toStringSafe(stack));
        }
        assert !this.isClient();
        final GrowingBuffer buffer = new GrowingBuffer(256);
        try {
            SubPacketType.ItemEvent.writeTo(buffer);
            DataEncoder.encode(buffer, player.getGameProfile(), false);
            DataEncoder.encode(buffer, stack, false);
            buffer.writeInt(event);
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
        buffer.flip();
        for (final EntityPlayerMP target : getPlayersInRange(player.getEntityWorld(), player.getPosition(), new ArrayList())) {
            if (limitRange) {
                final int dX = (int)(player.posX - target.posX);
                final int dZ = (int)(player.posZ - target.posZ);
                if (dX * dX + dZ * dZ > 400) {
                    continue;
                }
            }
            this.sendPacket(buffer, false, target);
        }
    }
    
    @Override
    public void initiateClientItemEvent(final ItemStack stack, final int event) {
        assert false;
    }
    
    @Override
    public void initiateClientTileEntityEvent(final TileEntity te, final int event) {
        assert false;
    }
    
    public void initiateRpc(final int id, final Class<? extends IRpcProvider<?>> provider, final Object[] args) {
        assert false;
    }
    
    public void requestGUI(final IHasGui inventory) {
        assert false;
    }
    
    public final void initiateGuiDisplay(final EntityPlayerMP player, final IHasGui inventory, final int windowId) {
        this.initiateGuiDisplay(player, inventory, windowId, null);
    }
    
    public final void initiateGuiDisplay(final EntityPlayerMP player, final IHasGui inventory, final int windowId, final Integer ID) {
        assert !this.isClient();
        try {
            final GrowingBuffer buffer = new GrowingBuffer(32);
            SubPacketType.GuiDisplay.writeTo(buffer);
            final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            final boolean isAdmin = server.getPlayerList().canSendCommands(player.getGameProfile());
            buffer.writeBoolean(isAdmin);
            if (inventory instanceof TileEntity) {
                final TileEntity te = (TileEntity)inventory;
                buffer.writeByte(0);
                DataEncoder.encode(buffer, te, false);
            }
            else if (player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() instanceof IHandHeldInventory) {
                buffer.writeByte(1);
                buffer.writeInt(player.inventory.currentItem);
                this.handleSubData(buffer, player.inventory.getCurrentItem(), ID);
            }
            else if (player.getHeldItemOffhand() != null && player.getHeldItemOffhand().getItem() instanceof IHandHeldInventory) {
                buffer.writeByte(1);
                buffer.writeInt(-1);
                this.handleSubData(buffer, player.getHeldItemOffhand(), ID);
            }
            else {
                IC2.platform.displayError("An unknown GUI type was attempted to be displayed.\nThis could happen due to corrupted data from a player or a bug.\n\n(Technical information: " + inventory + ")", new Object[0]);
            }
            buffer.writeInt(windowId);
            buffer.flip();
            this.sendPacket(buffer, true, player);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private final void handleSubData(final GrowingBuffer buffer, final ItemStack stack, final Integer ID) {
        final boolean subInv = ID != null && stack.getItem() instanceof IHandHeldSubInventory;
        buffer.writeBoolean(subInv);
        if (subInv) {
            buffer.writeShort(ID);
        }
    }
    
    public final void sendInitialData(final TileEntity te, final EntityPlayerMP player) {
        assert !this.isClient();
        if (te instanceof INetworkDataProvider) {
            final TeUpdateDataServer updateData = getTeUpdateData(te);
            for (final String field : ((INetworkDataProvider)te).getNetworkedFields()) {
                updateData.addPlayerField(field, player);
            }
        }
    }
    
    @Override
    public final void sendInitialData(final TileEntity te) {
        assert !this.isClient();
        if (te instanceof INetworkDataProvider) {
            final TeUpdateDataServer updateData = getTeUpdateData(te);
            final List<String> fields = ((INetworkDataProvider)te).getNetworkedFields();
            for (final String field : fields) {
                updateData.addGlobalField(field);
            }
            if (TeUpdate.debug) {
                IC2.log.info(LogCategory.Network, "Sending initial TE data for %s (%s).", Util.formatPosition(te), fields);
            }
        }
    }
    
    public final void sendChat(final EntityPlayerMP player, final String message) {
        assert !this.isClient();
        final GrowingBuffer buffer = new GrowingBuffer(message.length() * 2);
        buffer.writeString(message);
        buffer.flip();
        this.sendLargePacket(player, 1, buffer);
    }
    
    public final void sendConsole(final EntityPlayerMP player, final String message) {
        assert !this.isClient();
        final GrowingBuffer buffer = new GrowingBuffer(message.length() * 2);
        buffer.writeString(message);
        buffer.flip();
        this.sendLargePacket(player, 2, buffer);
    }
    
    public final void sendContainerFields(final ContainerBase<?> container, final String... fieldNames) {
        for (final String fieldName : fieldNames) {
            this.sendContainerField(container, fieldName);
        }
    }
    
    public final void sendContainerField(final ContainerBase<?> container, final String fieldName) {
        if (this.isClient() && this.getClientModifiableField(container.getClass(), fieldName) == null) {
            IC2.log.warn(LogCategory.Network, "Field update for %s failed.", container);
            return;
        }
        final GrowingBuffer buffer = new GrowingBuffer(256);
        try {
            SubPacketType.ContainerData.writeTo(buffer);
            buffer.writeInt(container.windowId);
            writeFieldData(container, fieldName, buffer);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
        buffer.flip();
        if (!this.isClient()) {
            for (final IContainerListener listener : container.getListeners()) {
                if (listener instanceof EntityPlayerMP) {
                    this.sendPacket(buffer, false, (EntityPlayerMP)listener);
                }
            }
        }
        else {
            this.sendPacket(buffer);
        }
    }
    
    public final void sendContainerEvent(final ContainerBase<?> container, final String event) {
        final GrowingBuffer buffer = new GrowingBuffer(64);
        SubPacketType.ContainerEvent.writeTo(buffer);
        buffer.writeInt(container.windowId);
        buffer.writeString(event);
        buffer.flip();
        if (!this.isClient()) {
            for (final IContainerListener listener : container.getListeners()) {
                if (listener instanceof EntityPlayerMP) {
                    this.sendPacket(buffer, false, (EntityPlayerMP)listener);
                }
            }
        }
        else {
            this.sendPacket(buffer);
        }
    }
    
    public final void sendHandHeldInvField(final ContainerBase<?> container, final String fieldName) {
        if (!(container.base instanceof HandHeldInventory)) {
            IC2.log.warn(LogCategory.Network, "Invalid container (%s) sent for field update.", container);
            return;
        }
        if (this.isClient() && this.getClientModifiableField(container.base.getClass(), fieldName) == null) {
            IC2.log.warn(LogCategory.Network, "Field update for %s failed.", container);
            return;
        }
        final GrowingBuffer buffer = new GrowingBuffer(256);
        try {
            SubPacketType.HandHeldInvData.writeTo(buffer);
            buffer.writeInt(container.windowId);
            writeFieldData(container.base, fieldName, buffer);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
        buffer.flip();
        if (!this.isClient()) {
            for (final IContainerListener listener : container.getListeners()) {
                if (listener instanceof EntityPlayerMP) {
                    this.sendPacket(buffer, false, (EntityPlayerMP)listener);
                }
            }
        }
        else {
            this.sendPacket(buffer);
        }
    }
    
    public final void initiateTeblockLandEffect(final World world, final double x, final double y, final double z, final int count, final ITeBlock teBlock) {
        this.initiateTeblockLandEffect(world, null, x, y, z, count, teBlock);
    }
    
    public final void initiateTeblockLandEffect(final World world, final BlockPos pos, final double x, final double y, final double z, final int count, final ITeBlock teBlock) {
        assert !this.isClient();
        final GrowingBuffer buffer = new GrowingBuffer(64);
        try {
            SubPacketType.TileEntityBlockLandEffect.writeTo(buffer);
            DataEncoder.encode(buffer, world, false);
            if (pos != null) {
                buffer.writeBoolean(true);
                DataEncoder.encode(buffer, pos, false);
            }
            else {
                buffer.writeBoolean(false);
            }
            buffer.writeDouble(x);
            buffer.writeDouble(y);
            buffer.writeDouble(z);
            buffer.writeInt(count);
            buffer.writeString(teBlock.getName());
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
        buffer.flip();
        for (final EntityPlayer player : world.playerEntities) {
            if (!(player instanceof EntityPlayerMP)) {
                continue;
            }
            final double distance = player.getDistanceSq(x, y, z);
            if (distance > 1024.0) {
                continue;
            }
            this.sendPacket(buffer, false, (EntityPlayerMP)player);
        }
    }
    
    public final void initiateTeblockRunEffect(final World world, final Entity entity, final ITeBlock teBlock) {
        this.initiateTeblockRunEffect(world, null, entity, teBlock);
    }
    
    public final void initiateTeblockRunEffect(final World world, final BlockPos pos, final Entity entity, final ITeBlock teBlock) {
        assert !this.isClient();
        final GrowingBuffer buffer = new GrowingBuffer(64);
        try {
            SubPacketType.TileEntityBlockRunEffect.writeTo(buffer);
            DataEncoder.encode(buffer, world, false);
            if (pos != null) {
                buffer.writeBoolean(true);
                DataEncoder.encode(buffer, pos, false);
            }
            else {
                buffer.writeBoolean(false);
            }
            buffer.writeDouble(entity.posX + (IC2.random.nextFloat() - 0.5) * entity.width);
            buffer.writeDouble(entity.getEntityBoundingBox().minY + 0.1);
            buffer.writeDouble(entity.posZ + (IC2.random.nextFloat() - 0.5) * entity.width);
            buffer.writeDouble(-entity.motionX * 4.0);
            buffer.writeDouble(-entity.motionZ * 4.0);
            buffer.writeString(teBlock.getName());
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
        buffer.flip();
        for (final EntityPlayer player : world.playerEntities) {
            if (!(player instanceof EntityPlayerMP)) {
                continue;
            }
            final double distance = player.getDistanceSq(entity.posX, entity.posY, entity.posZ);
            if (distance > 1024.0) {
                continue;
            }
            this.sendPacket(buffer, false, (EntityPlayerMP)player);
        }
    }
    
    final void sendLargePacket(final EntityPlayerMP player, final int id, final GrowingBuffer data) {
        final GrowingBuffer buffer = new GrowingBuffer(16384);
        buffer.writeShort(0);
        try {
            final DeflaterOutputStream deflate = new DeflaterOutputStream(buffer);
            data.writeTo(deflate);
            deflate.close();
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
        buffer.flip();
        boolean firstPacket = true;
        boolean lastPacket;
        do {
            lastPacket = (buffer.available() <= 32766);
            if (!firstPacket) {
                buffer.skipBytes(-2);
            }
            SubPacketType.LargePacket.writeTo(buffer);
            int state = 0;
            if (firstPacket) {
                state |= 0x1;
            }
            if (lastPacket) {
                state |= 0x2;
            }
            state |= id << 2;
            buffer.write(state);
            buffer.skipBytes(-2);
            if (lastPacket) {
                this.sendPacket(buffer, true, player);
                assert !buffer.hasAvailable();
            }
            else {
                this.sendPacket(buffer.copy(32766), true, player);
            }
            firstPacket = false;
        } while (!lastPacket);
    }
    
    @SubscribeEvent
    public void onPacket(final FMLNetworkEvent.ServerCustomPacketEvent event) {
        if (this.getClass() == NetworkManager.class) {
            try {
                this.onPacketData(GrowingBuffer.wrap(event.getPacket().payload()), (EntityPlayer)((NetHandlerPlayServer)event.getHandler()).player);
            }
            catch (final Throwable t) {
                IC2.log.warn(LogCategory.Network, t, "Network read failed");
                throw new RuntimeException(t);
            }
            event.getPacket().payload().release();
        }
    }
    
    private void onPacketData(final GrowingBuffer is, final EntityPlayer player) throws IOException {
        if (!is.hasAvailable()) {
            return;
        }
        final SubPacketType packetType = SubPacketType.read(is, true);
        if (packetType == null) {
            return;
        }
        switch (packetType) {
            case ItemEvent: {
                final ItemStack stack = DataEncoder.decode(is, ItemStack.class);
                final int event = is.readInt();
                if (stack.getItem() instanceof INetworkItemEventListener) {
                    IC2.platform.requestTick(true, new Runnable() {
                        @Override
                        public void run() {
                            ((INetworkItemEventListener)stack.getItem()).onNetworkEvent(stack, player, event);
                        }
                    });
                    break;
                }
                break;
            }
            case KeyUpdate: {
                final int keyState = is.readInt();
                IC2.platform.requestTick(true, new Runnable() {
                    @Override
                    public void run() {
                        IC2.keyboard.processKeyUpdate(player, keyState);
                    }
                });
                break;
            }
            case TileEntityEvent: {
                final Object teDeferred = DataEncoder.decodeDeferred(is, TileEntity.class);
                final int event = is.readInt();
                IC2.platform.requestTick(true, new Runnable() {
                    @Override
                    public void run() {
                        final TileEntity te = DataEncoder.getValue(teDeferred);
                        if (te instanceof INetworkClientTileEntityEventListener) {
                            ((INetworkClientTileEntityEventListener)te).onNetworkEvent(player, event);
                        }
                    }
                });
                break;
            }
            case RequestGUI: {
                final boolean hand = is.readBoolean();
                final Object teDeferred2 = hand ? null : DataEncoder.decodeDeferred(is, TileEntity.class);
                IC2.platform.requestTick(true, new Runnable() {
                    private IHasGui tryFindGUI(final ItemStack stack) {
                        if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IHandHeldInventory) {
                            return ((IHandHeldInventory)stack.getItem()).getInventory(player, stack);
                        }
                        return null;
                    }
                    
                    @Override
                    public void run() {
                        if (hand) {
                            for (final ItemStack stack : player.getHeldEquipment()) {
                                final IHasGui gui = this.tryFindGUI(stack);
                                if (gui != null) {
                                    IC2.platform.launchGui(player, gui);
                                    break;
                                }
                            }
                        }
                        else {
                            final TileEntity te = DataEncoder.getValue(teDeferred2);
                            if (te instanceof IHasGui) {
                                IC2.platform.launchGui(player, (IHasGui)te);
                            }
                        }
                    }
                });
                break;
            }
            case Rpc: {
                RpcHandler.processRpcRequest(is, (EntityPlayerMP)player);
                break;
            }
            default: {
                this.onCommonPacketData(packetType, true, is, player);
                break;
            }
        }
    }
    
    protected void onCommonPacketData(final SubPacketType packetType, final boolean simulating, final GrowingBuffer is, final EntityPlayer player) throws IOException {
        switch (packetType) {
            case PlayerItemData: {
                final int slot = is.readByte();
                final Item item = DataEncoder.decode(is, Item.class);
                final int dataCount = is.readVarInt();
                final Object[] subData = new Object[dataCount];
                for (int i = 0; i < dataCount; ++i) {
                    subData[i] = DataEncoder.decode(is);
                }
                if (slot >= 0 && slot < 9) {
                    IC2.platform.requestTick(simulating, new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < subData.length; ++i) {
                                subData[i] = DataEncoder.getValue(subData[i]);
                            }
                            final ItemStack stack = (ItemStack)player.inventory.mainInventory.get(slot);
                            if (!StackUtil.isEmpty(stack) && stack.getItem() == item && item instanceof IPlayerItemDataListener) {
                                ((IPlayerItemDataListener)item).onPlayerItemNetworkData(player, slot, subData);
                            }
                        }
                    });
                    break;
                }
                break;
            }
            case ContainerData: {
                final int windowId = is.readInt();
                final String fieldName = is.readString();
                final Object value = DataEncoder.decode(is);
                IC2.platform.requestTick(simulating, new Runnable() {
                    @Override
                    public void run() {
                        if (player.openContainer instanceof ContainerBase && player.openContainer.windowId == windowId && (NetworkManager.this.isClient() || NetworkManager.this.getClientModifiableField(player.openContainer.getClass(), fieldName) != null)) {
                            ReflectionUtil.setValueRecursive(player.openContainer, fieldName, DataEncoder.getValue(value));
                        }
                    }
                });
                break;
            }
            case ContainerEvent: {
                final int windowId = is.readInt();
                final String event = is.readString();
                IC2.platform.requestTick(simulating, new Runnable() {
                    @Override
                    public void run() {
                        if (player.openContainer instanceof ContainerBase && player.openContainer.windowId == windowId) {
                            ((ContainerBase)player.openContainer).onContainerEvent(event);
                        }
                    }
                });
                break;
            }
            case HandHeldInvData: {
                final int windowId = is.readInt();
                final String fieldName = is.readString();
                final Object value = DataEncoder.decode(is);
                IC2.platform.requestTick(simulating, new Runnable() {
                    @Override
                    public void run() {
                        if (player.openContainer instanceof ContainerBase && player.openContainer.windowId == windowId) {
                            final ContainerBase<?> container = (ContainerBase<?>)player.openContainer;
                            if (container.base instanceof HandHeldInventory && (NetworkManager.this.isClient() || NetworkManager.this.getClientModifiableField(container.base.getClass(), fieldName) != null)) {
                                ReflectionUtil.setValueRecursive(container.base, fieldName, DataEncoder.getValue(value));
                            }
                        }
                    }
                });
                break;
            }
            case TileEntityData: {
                final Object teDeferred = DataEncoder.decodeDeferred(is, TileEntity.class);
                final String fieldName = is.readString();
                final Object value = DataEncoder.decode(is);
                IC2.platform.requestTick(simulating, new Runnable() {
                    @Override
                    public void run() {
                        final TileEntity te = DataEncoder.getValue(teDeferred);
                        if (te != null && (NetworkManager.this.isClient() || NetworkManager.this.getClientModifiableField(te.getClass(), fieldName) != null)) {
                            ReflectionUtil.setValueRecursive(te, fieldName, DataEncoder.getValue(value));
                        }
                    }
                });
                break;
            }
            default: {
                IC2.log.warn(LogCategory.Network, "Unhandled packet type: %s", packetType.name());
                break;
            }
        }
    }
    
    public void initiateKeyUpdate(final int keyState) {
    }
    
    public void sendLoginData() {
    }
    
    public final void initiateExplosionEffect(final World world, final Vec3d pos, final ExplosionIC2.Type type) {
        assert !this.isClient();
        try {
            final GrowingBuffer buffer = new GrowingBuffer(32);
            SubPacketType.ExplosionEffect.writeTo(buffer);
            DataEncoder.encode(buffer, world, false);
            DataEncoder.encode(buffer, pos, false);
            DataEncoder.encode(buffer, type, false);
            buffer.flip();
            for (final Object obj : world.playerEntities) {
                if (!(obj instanceof EntityPlayerMP)) {
                    continue;
                }
                final EntityPlayerMP player = (EntityPlayerMP)obj;
                if (player.getDistanceSq(pos.x, pos.y, pos.z) >= 128.0) {
                    continue;
                }
                this.sendPacket(buffer, false, player);
            }
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected final void sendPacket(final GrowingBuffer buffer) {
        if (!this.isClient()) {
            NetworkManager.channel.sendToAll(makePacket(buffer, true));
        }
        else {
            NetworkManager.channel.sendToServer(makePacket(buffer, true));
        }
    }
    
    protected final void sendPacket(final GrowingBuffer buffer, final boolean advancePos, final EntityPlayerMP player) {
        assert !this.isClient();
        NetworkManager.channel.sendTo(makePacket(buffer, advancePos), player);
    }
    
    static <T extends Collection<EntityPlayerMP>> T getPlayersInRange(final World world, final BlockPos pos, final T result) {
        if (!(world instanceof WorldServer)) {
            return result;
        }
        final PlayerChunkMap playerManager = ((WorldServer)world).getPlayerChunkMap();
        final PlayerChunkMapEntry instance = playerManager.getEntry(pos.getX() >> 4, pos.getZ() >> 4);
        if (instance == null) {
            return result;
        }
        result.addAll(ReflectionUtil.getFieldValue(NetworkManager.playerInstancePlayers, instance));
        return result;
    }
    
    static void writeFieldData(final Object object, final String fieldName, final GrowingBuffer out) throws IOException {
        final int pos = fieldName.indexOf(61);
        if (pos != -1) {
            out.writeString(fieldName.substring(0, pos));
            DataEncoder.encode(out, fieldName.substring(pos + 1));
        }
        else {
            out.writeString(fieldName);
            try {
                DataEncoder.encode(out, ReflectionUtil.getValueRecursive(object, fieldName));
            }
            catch (final NoSuchFieldException e) {
                throw new RuntimeException("Can't find field " + fieldName + " in " + object.getClass().getName(), e);
            }
        }
    }
    
    private static FMLProxyPacket makePacket(final GrowingBuffer buffer, final boolean advancePos) {
        return new FMLProxyPacket(new PacketBuffer(buffer.toByteBuf(advancePos)), "ic2");
    }
    
    static {
        NetworkManager.playerInstancePlayers = ReflectionUtil.getField(PlayerChunkMapEntry.class, List.class);
    }
}
