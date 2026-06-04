// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.network;

import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileDescriptor;
import ic2.core.util.ParticleUtil;
import ic2.core.block.ITeBlock;
import ic2.core.block.TeBlockRegistry;
import java.io.DataInput;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Components;
import ic2.core.audio.PositionSpec;
import ic2.core.audio.AudioPosition;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import ic2.core.ExplosionIC2;
import net.minecraft.util.math.Vec3d;
import ic2.core.item.IHandHeldSubInventory;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.client.entity.EntityPlayerSP;
import java.util.Iterator;
import net.minecraft.world.World;
import ic2.api.network.INetworkItemEventListener;
import com.mojang.authlib.GameProfile;
import ic2.api.network.INetworkTileEntityEventListener;
import java.io.OutputStream;
import java.util.zip.InflaterOutputStream;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ic2.core.util.LogCategory;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.IC2;
import ic2.core.item.IHandHeldInventory;
import ic2.core.util.StackUtil;
import net.minecraft.client.Minecraft;
import ic2.core.IHasGui;
import net.minecraft.tileentity.TileEntity;
import java.io.IOException;
import ic2.api.network.IGrowingBuffer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class NetworkManagerClient extends NetworkManager
{
    private GrowingBuffer largePacketBuffer;
    
    @Override
    protected boolean isClient() {
        return true;
    }
    
    @Override
    public void initiateClientItemEvent(final ItemStack stack, final int event) {
        try {
            final GrowingBuffer buffer = new GrowingBuffer(256);
            SubPacketType.ItemEvent.writeTo(buffer);
            DataEncoder.encode(buffer, stack, false);
            buffer.writeInt(event);
            buffer.flip();
            this.sendPacket(buffer);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void initiateKeyUpdate(final int keyState) {
        final GrowingBuffer buffer = new GrowingBuffer(5);
        SubPacketType.KeyUpdate.writeTo(buffer);
        buffer.writeInt(keyState);
        buffer.flip();
        this.sendPacket(buffer);
    }
    
    @Override
    public void initiateClientTileEntityEvent(final TileEntity te, final int event) {
        try {
            final GrowingBuffer buffer = new GrowingBuffer(32);
            SubPacketType.TileEntityEvent.writeTo(buffer);
            DataEncoder.encode(buffer, te, false);
            buffer.writeInt(event);
            buffer.flip();
            this.sendPacket(buffer);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void initiateRpc(final int id, final Class<? extends IRpcProvider<?>> provider, final Object[] args) {
        try {
            final GrowingBuffer buffer = new GrowingBuffer(256);
            SubPacketType.Rpc.writeTo(buffer);
            buffer.writeInt(id);
            buffer.writeString(provider.getName());
            DataEncoder.encode(buffer, args);
            buffer.flip();
            this.sendPacket(buffer);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void requestGUI(final IHasGui inventory) {
        try {
            final GrowingBuffer buffer = new GrowingBuffer(32);
            SubPacketType.RequestGUI.writeTo(buffer);
            if (inventory instanceof TileEntity) {
                final TileEntity te = (TileEntity)inventory;
                buffer.writeBoolean(false);
                DataEncoder.encode(buffer, te, false);
            }
            else {
                final EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().player;
                if ((!StackUtil.isEmpty(player.inventory.getCurrentItem()) && player.inventory.getCurrentItem().getItem() instanceof IHandHeldInventory) || (!StackUtil.isEmpty(player.getHeldItemOffhand()) && player.getHeldItemOffhand().getItem() instanceof IHandHeldInventory)) {
                    buffer.writeBoolean(true);
                }
                else {
                    IC2.platform.displayError("An unknown GUI type was attempted to be displayed.\nThis could happen due to corrupted data from a player or a bug.\n\n(Technical information: " + inventory + ")", new Object[0]);
                }
            }
            buffer.flip();
            this.sendPacket(buffer);
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @SubscribeEvent
    public void onPacket(final FMLNetworkEvent.ClientCustomPacketEvent event) {
        assert !this.getClass().getName().equals(NetworkManager.class.getName());
        try {
            this.onPacketData(GrowingBuffer.wrap(event.getPacket().payload()), (EntityPlayer)Minecraft.getMinecraft().player);
        }
        catch (final Throwable t) {
            IC2.log.warn(LogCategory.Network, t, "Network read failed");
            throw new RuntimeException(t);
        }
        event.getPacket().payload().release();
    }
    
    private void onPacketData(final GrowingBuffer is, final EntityPlayer player) throws IOException {
        if (!is.hasAvailable()) {
            return;
        }
        final SubPacketType packetType = SubPacketType.read(is, false);
        if (packetType == null) {
            return;
        }
        switch (packetType) {
            case LargePacket: {
                final int state = is.readUnsignedByte();
                if ((state & 0x2) != 0x0) {
                    GrowingBuffer input;
                    if ((state & 0x1) != 0x0) {
                        input = is;
                    }
                    else {
                        input = this.largePacketBuffer;
                        if (input == null) {
                            throw new IOException("unexpected large packet continuation");
                        }
                        is.writeTo(input);
                        input.flip();
                        this.largePacketBuffer = null;
                    }
                    final GrowingBuffer decompBuffer = new GrowingBuffer(input.available() * 2);
                    final InflaterOutputStream inflate = new InflaterOutputStream(decompBuffer);
                    input.writeTo(inflate);
                    inflate.close();
                    decompBuffer.flip();
                    switch (state >> 2) {
                        case 0: {
                            TeUpdate.receive(decompBuffer);
                            break;
                        }
                        case 1: {
                            processChatPacket(decompBuffer);
                            break;
                        }
                        case 2: {
                            processConsolePacket(decompBuffer);
                            break;
                        }
                    }
                    break;
                }
                if ((state & 0x1) != 0x0) {
                    assert this.largePacketBuffer == null;
                    this.largePacketBuffer = new GrowingBuffer(32752);
                }
                if (this.largePacketBuffer == null) {
                    throw new IOException("unexpected large packet continuation");
                }
                is.writeTo(this.largePacketBuffer);
                break;
            }
            case TileEntityEvent: {
                final Object teDeferred = DataEncoder.decodeDeferred(is, TileEntity.class);
                final int event = is.readInt();
                IC2.platform.requestTick(false, new Runnable() {
                    @Override
                    public void run() {
                        final TileEntity te = DataEncoder.getValue(teDeferred);
                        if (te instanceof INetworkTileEntityEventListener) {
                            ((INetworkTileEntityEventListener)te).onNetworkEvent(event);
                        }
                    }
                });
                break;
            }
            case ItemEvent: {
                final GameProfile profile = DataEncoder.decode(is, GameProfile.class);
                final ItemStack stack = DataEncoder.decode(is, ItemStack.class);
                final int event2 = is.readInt();
                IC2.platform.requestTick(false, new Runnable() {
                    @Override
                    public void run() {
                        final World world = (World)Minecraft.getMinecraft().world;
                        for (final Object obj : world.playerEntities) {
                            final EntityPlayer player = (EntityPlayer)obj;
                            if ((profile.getId() != null && profile.getId().equals(player.getGameProfile().getId())) || (profile.getId() == null && profile.getName().equals(player.getGameProfile().getName()))) {
                                if (stack.getItem() instanceof INetworkItemEventListener) {
                                    ((INetworkItemEventListener)stack.getItem()).onNetworkEvent(stack, player, event2);
                                    break;
                                }
                                break;
                            }
                        }
                    }
                });
                break;
            }
            case GuiDisplay: {
                final boolean isAdmin = is.readBoolean();
                switch (is.readByte()) {
                    case 0: {
                        final Object teDeferred2 = DataEncoder.decodeDeferred(is, TileEntity.class);
                        final int windowId = is.readInt();
                        IC2.platform.requestTick(false, new Runnable() {
                            @Override
                            public void run() {
                                final EntityPlayer player = IC2.platform.getPlayerInstance();
                                final TileEntity te = DataEncoder.getValue(teDeferred2);
                                if (te instanceof IHasGui) {
                                    IC2.platform.launchGuiClient(player, (IHasGui)te, isAdmin);
                                    player.openContainer.windowId = windowId;
                                }
                                else if (player instanceof EntityPlayerSP) {
                                    ((EntityPlayerSP)player).connection.sendPacket((Packet)new CPacketCloseWindow(windowId));
                                }
                            }
                        });
                        break;
                    }
                    case 1: {
                        final int currentItemPosition = is.readInt();
                        final boolean subGUI = is.readBoolean();
                        final short ID = (short)(subGUI ? is.readShort() : 0);
                        final int windowId2 = is.readInt();
                        IC2.platform.requestTick(false, new Runnable() {
                            @Override
                            public void run() {
                                final EntityPlayer player = IC2.platform.getPlayerInstance();
                                ItemStack currentItem;
                                if (currentItemPosition < 0) {
                                    final int actualItemPosition = ~currentItemPosition;
                                    if (actualItemPosition > player.inventory.offHandInventory.size() - 1) {
                                        return;
                                    }
                                    currentItem = (ItemStack)player.inventory.offHandInventory.get(actualItemPosition);
                                }
                                else {
                                    if (currentItemPosition != player.inventory.currentItem) {
                                        return;
                                    }
                                    currentItem = player.inventory.getCurrentItem();
                                }
                                if (currentItem != null && currentItem.getItem() instanceof IHandHeldInventory) {
                                    if (subGUI && currentItem.getItem() instanceof IHandHeldSubInventory) {
                                        IC2.platform.launchGuiClient(player, ((IHandHeldSubInventory)currentItem.getItem()).getSubInventory(player, currentItem, ID), isAdmin);
                                    }
                                    else {
                                        IC2.platform.launchGuiClient(player, ((IHandHeldInventory)currentItem.getItem()).getInventory(player, currentItem), isAdmin);
                                    }
                                }
                                else if (player instanceof EntityPlayerSP) {
                                    ((EntityPlayerSP)player).connection.sendPacket((Packet)new CPacketCloseWindow(windowId2));
                                }
                                player.openContainer.windowId = windowId2;
                            }
                        });
                        break;
                    }
                }
                break;
            }
            case ExplosionEffect: {
                final Object worldDeferred = DataEncoder.decodeDeferred(is, World.class);
                final Vec3d pos = DataEncoder.decode(is, Vec3d.class);
                final ExplosionIC2.Type type = DataEncoder.decodeEnum(is, ExplosionIC2.Type.class);
                IC2.platform.requestTick(false, new Runnable() {
                    @Override
                    public void run() {
                        final World world = DataEncoder.getValue(worldDeferred);
                        if (world != null) {
                            switch (type) {
                                case Normal: {
                                    world.playSound(player, new BlockPos(pos), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0f, (1.0f + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2f) * 0.7f);
                                    world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0, new int[0]);
                                    break;
                                }
                                case Electrical: {
                                    IC2.audioManager.playOnce(new AudioPosition(world, (float)pos.x, (float)pos.y, (float)pos.z), PositionSpec.Center, "Machines/MachineOverload.ogg", true, IC2.audioManager.getDefaultVolume());
                                    world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0, new int[0]);
                                    break;
                                }
                                case Heat: {
                                    world.playSound(player, new BlockPos(pos), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 4.0f, (1.0f + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2f) * 0.7f);
                                    world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0, new int[0]);
                                    break;
                                }
                                case Nuclear: {
                                    IC2.audioManager.playOnce(new AudioPosition(world, (float)pos.x, (float)pos.y, (float)pos.z), PositionSpec.Center, "Tools/NukeExplosion.ogg", true, IC2.audioManager.getDefaultVolume());
                                    world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0, new int[0]);
                                    break;
                                }
                            }
                        }
                    }
                });
                break;
            }
            case Rpc: {
                throw new RuntimeException("Received unexpected RPC packet");
            }
            case TileEntityBlockComponent: {
                final int dimensionId = is.readInt();
                final BlockPos pos2 = DataEncoder.decode(is, BlockPos.class);
                final String componentName = is.readString();
                final Class<? extends TileEntityComponent> componentCls = Components.getClass(componentName);
                if (componentCls == null) {
                    throw new IOException("invalid component: " + componentName);
                }
                final int dataLen = is.readVarInt();
                if (dataLen > 65536) {
                    throw new IOException("data length limit exceeded");
                }
                final byte[] data = new byte[dataLen];
                is.readFully(data);
                IC2.platform.requestTick(false, new Runnable() {
                    @Override
                    public void run() {
                        final World world = (World)Minecraft.getMinecraft().world;
                        if (world.provider.getDimension() != dimensionId) {
                            return;
                        }
                        final TileEntity teRaw = world.getTileEntity(pos2);
                        if (!(teRaw instanceof TileEntityBlock)) {
                            return;
                        }
                        final TileEntityComponent component = ((TileEntityBlock)teRaw).getComponent(componentCls);
                        if (component == null) {
                            return;
                        }
                        final DataInputStream dataIs = new DataInputStream(new ByteArrayInputStream(data));
                        try {
                            component.onNetworkUpdate(dataIs);
                        }
                        catch (final IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                break;
            }
            case TileEntityBlockLandEffect: {
                final Object worldDeferred = DataEncoder.decodeDeferred(is, World.class);
                BlockPos pos2;
                if (is.readBoolean()) {
                    pos2 = (BlockPos)DataEncoder.decode(is, DataEncoder.EncodedType.BlockPos);
                }
                else {
                    pos2 = null;
                }
                final double x = is.readDouble();
                final double y = is.readDouble();
                final double z = is.readDouble();
                final int count = is.readInt();
                final ITeBlock teBlock = TeBlockRegistry.get(is.readString());
                IC2.platform.requestTick(false, new Runnable() {
                    @Override
                    public void run() {
                        final World world = DataEncoder.getValue(worldDeferred);
                        if (world == null) {
                            return;
                        }
                        ParticleUtil.spawnBlockLandParticles(world, pos2, x, y, z, count, teBlock);
                    }
                });
                break;
            }
            case TileEntityBlockRunEffect: {
                final Object worldDeferred = DataEncoder.decodeDeferred(is, World.class);
                BlockPos pos2;
                if (is.readBoolean()) {
                    pos2 = (BlockPos)DataEncoder.decode(is, DataEncoder.EncodedType.BlockPos);
                }
                else {
                    pos2 = null;
                }
                final double x = is.readDouble();
                final double y = is.readDouble();
                final double z = is.readDouble();
                final double xSpeed = is.readDouble();
                final double zSpeed = is.readDouble();
                final ITeBlock teBlock2 = TeBlockRegistry.get(is.readString());
                IC2.platform.requestTick(false, new Runnable() {
                    @Override
                    public void run() {
                        final World world = DataEncoder.getValue(worldDeferred);
                        if (world == null) {
                            return;
                        }
                        ParticleUtil.spawnBlockRunParticles(world, pos2, x, y, z, xSpeed, zSpeed, teBlock2);
                    }
                });
                break;
            }
            default: {
                this.onCommonPacketData(packetType, false, is, player);
                break;
            }
        }
    }
    
    private static void processChatPacket(final GrowingBuffer buffer) {
        final String messages = buffer.readString();
        IC2.platform.requestTick(false, new Runnable() {
            @Override
            public void run() {
                for (final String line : messages.split("[\\r\\n]+")) {
                    IC2.platform.messagePlayer(null, line, new Object[0]);
                }
            }
        });
    }
    
    private static void processConsolePacket(final GrowingBuffer buffer) {
        final String messages = buffer.readString();
        final PrintStream console = new PrintStream(new FileOutputStream(FileDescriptor.out));
        for (final String line : messages.split("[\\r\\n]+")) {
            console.println(line);
        }
        console.flush();
    }
}
