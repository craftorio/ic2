package ic2.core.network;

import com.mojang.authlib.GameProfile;
import ic2.api.network.INetworkItemEventListener;
import ic2.api.network.INetworkTileEntityEventListener;
import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioPosition;
import ic2.core.audio.PositionSpec;
import ic2.core.block.ITeBlock;
import ic2.core.block.TeBlockRegistry;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Components;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.item.IHandHeldInventory;
import ic2.core.item.IHandHeldSubInventory;
import ic2.core.util.LogCategory;
import ic2.core.util.ParticleUtil;
import ic2.core.util.StackUtil;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.zip.InflaterOutputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class NetworkManagerClient extends NetworkManager {
  private GrowingBuffer largePacketBuffer;
  
  protected boolean isClient() {
    return true;
  }
  
  public void initiateClientItemEvent(ItemStack stack, int event) {
    try {
      GrowingBuffer buffer = new GrowingBuffer(256);
      SubPacketType.ItemEvent.writeTo(buffer);
      DataEncoder.encode(buffer, stack, false);
      buffer.writeInt(event);
      buffer.flip();
      sendPacket(buffer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public void initiateKeyUpdate(int keyState) {
    GrowingBuffer buffer = new GrowingBuffer(5);
    SubPacketType.KeyUpdate.writeTo(buffer);
    buffer.writeInt(keyState);
    buffer.flip();
    sendPacket(buffer);
  }
  
  public void initiateClientTileEntityEvent(TileEntity te, int event) {
    try {
      GrowingBuffer buffer = new GrowingBuffer(32);
      SubPacketType.TileEntityEvent.writeTo(buffer);
      DataEncoder.encode(buffer, te, false);
      buffer.writeInt(event);
      buffer.flip();
      sendPacket(buffer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public void initiateRpc(int id, Class<? extends IRpcProvider<?>> provider, Object[] args) {
    try {
      GrowingBuffer buffer = new GrowingBuffer(256);
      SubPacketType.Rpc.writeTo(buffer);
      buffer.writeInt(id);
      buffer.writeString(provider.getName());
      DataEncoder.encode(buffer, args);
      buffer.flip();
      sendPacket(buffer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public void requestGUI(IHasGui inventory) {
    try {
      GrowingBuffer buffer = new GrowingBuffer(32);
      SubPacketType.RequestGUI.writeTo(buffer);
      if (inventory instanceof TileEntity) {
        TileEntity te = (TileEntity)inventory;
        buffer.writeBoolean(false);
        DataEncoder.encode(buffer, te, false);
      } else {
        EntityPlayerSP entityPlayerSP = (Minecraft.getMinecraft()).player;
        if ((!StackUtil.isEmpty(((EntityPlayer)entityPlayerSP).inventory.getCurrentItem()) && ((EntityPlayer)entityPlayerSP).inventory.getCurrentItem().getItem() instanceof IHandHeldInventory) || (
          !StackUtil.isEmpty(entityPlayerSP.getHeldItemOffhand()) && entityPlayerSP.getHeldItemOffhand().getItem() instanceof IHandHeldInventory)) {
          buffer.writeBoolean(true);
        } else {
          IC2.platform.displayError("An unknown GUI type was attempted to be displayed.\nThis could happen due to corrupted data from a player or a bug.\n\n(Technical information: " + inventory + ")", new Object[0]);
        } 
      } 
      buffer.flip();
      sendPacket(buffer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
  }
  
  @SubscribeEvent
  public void onPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
    assert !getClass().getName().equals(NetworkManager.class.getName());
    try {
      onPacketData(GrowingBuffer.wrap(event.getPacket().payload()), (EntityPlayer)(Minecraft.getMinecraft()).player);
    } catch (Throwable t) {
      IC2.log.warn(LogCategory.Network, t, "Network read failed");
      throw new RuntimeException(t);
    } 
    event.getPacket().payload().release();
  }
  
  private void onPacketData(GrowingBuffer is, final EntityPlayer player) throws IOException {
    int state;
    final Object teDeferred;
    final GameProfile profile;
    final boolean isAdmin;
    final Object worldDeferred;
    final int dimensionId;
    final Object worldDeferred;
    final int event;
    final ItemStack stack;
    final Object teDeferred;
    final int currentItemPosition;
    final Vec3d pos;
    final BlockPos pos;
    final int event, windowId;
    final boolean subGUI;
    final ExplosionIC2.Type type;
    String componentName;
    final double x;
    final short ID;
    final Class<? extends TileEntityComponent> componentCls;
    final int windowId, dataLen;
    final double y;
    final byte[] data;
    final double z;
    final int count;
    final double xSpeed;
    final ITeBlock teBlock;
    final double zSpeed;
    final ITeBlock teBlock;
    if (!is.hasAvailable())
      return; 
    SubPacketType packetType = SubPacketType.read(is, false);
    if (packetType == null)
      return; 
    switch (packetType) {
      case LargePacket:
        state = is.readUnsignedByte();
        if ((state & 0x2) != 0) {
          GrowingBuffer input;
          if ((state & 0x1) != 0) {
            input = is;
          } else {
            input = this.largePacketBuffer;
            if (input == null)
              throw new IOException("unexpected large packet continuation"); 
            is.writeTo(input);
            input.flip();
            this.largePacketBuffer = null;
          } 
          GrowingBuffer decompBuffer = new GrowingBuffer(input.available() * 2);
          InflaterOutputStream inflate = new InflaterOutputStream(decompBuffer);
          input.writeTo(inflate);
          inflate.close();
          decompBuffer.flip();
          switch (state >> 2) {
            case 0:
              TeUpdate.receive(decompBuffer);
              break;
            case 1:
              processChatPacket(decompBuffer);
              break;
            case 2:
              processConsolePacket(decompBuffer);
              break;
          } 
        } else {
          if ((state & 0x1) != 0) {
            assert this.largePacketBuffer == null;
            this.largePacketBuffer = new GrowingBuffer(32752);
          } 
          if (this.largePacketBuffer == null)
            throw new IOException("unexpected large packet continuation"); 
          is.writeTo(this.largePacketBuffer);
        } 
        return;
      case TileEntityEvent:
        teDeferred = DataEncoder.decodeDeferred(is, TileEntity.class);
        event = is.readInt();
        IC2.platform.requestTick(false, new Runnable() {
              public void run() {
                TileEntity te = DataEncoder.<TileEntity>getValue(teDeferred);
                if (te instanceof INetworkTileEntityEventListener)
                  ((INetworkTileEntityEventListener)te).onNetworkEvent(event); 
              }
            });
        return;
      case ItemEvent:
        profile = DataEncoder.<GameProfile>decode(is, GameProfile.class);
        stack = DataEncoder.<ItemStack>decode(is, ItemStack.class);
        i = is.readInt();
        IC2.platform.requestTick(false, new Runnable() {
              public void run() {
                WorldClient worldClient = (Minecraft.getMinecraft()).world;
                for (Object obj : ((World)worldClient).playerEntities) {
                  EntityPlayer player = (EntityPlayer)obj;
                  if ((profile.getId() != null && profile.getId().equals(player.getGameProfile().getId())) || (profile
                    .getId() == null && profile.getName().equals(player.getGameProfile().getName()))) {
                    if (stack.getItem() instanceof INetworkItemEventListener)
                      ((INetworkItemEventListener)stack.getItem()).onNetworkEvent(stack, player, event); 
                    break;
                  } 
                } 
              }
            });
        return;
      case GuiDisplay:
        isAdmin = is.readBoolean();
        switch (is.readByte()) {
          case 0:
            object2 = DataEncoder.decodeDeferred(is, TileEntity.class);
            windowId = is.readInt();
            IC2.platform.requestTick(false, new Runnable() {
                  public void run() {
                    EntityPlayer player = IC2.platform.getPlayerInstance();
                    TileEntity te = DataEncoder.<TileEntity>getValue(teDeferred);
                    if (te instanceof IHasGui) {
                      IC2.platform.launchGuiClient(player, (IHasGui)te, isAdmin);
                      player.openContainer.windowId = windowId;
                    } else if (player instanceof EntityPlayerSP) {
                      ((EntityPlayerSP)player).connection.sendPacket((Packet)new CPacketCloseWindow(windowId));
                    } 
                  }
                });
            break;
          case 1:
            currentItemPosition = is.readInt();
            subGUI = is.readBoolean();
            ID = subGUI ? is.readShort() : 0;
            j = is.readInt();
            IC2.platform.requestTick(false, new Runnable() {
                  public void run() {
                    ItemStack currentItem;
                    EntityPlayer player = IC2.platform.getPlayerInstance();
                    if (currentItemPosition < 0) {
                      int actualItemPosition = currentItemPosition ^ 0xFFFFFFFF;
                      if (actualItemPosition > player.inventory.offHandInventory.size() - 1)
                        return; 
                      currentItem = (ItemStack)player.inventory.offHandInventory.get(actualItemPosition);
                    } else {
                      if (currentItemPosition != player.inventory.currentItem)
                        return; 
                      currentItem = player.inventory.getCurrentItem();
                    } 
                    if (currentItem != null && currentItem.getItem() instanceof IHandHeldInventory) {
                      if (subGUI && currentItem.getItem() instanceof IHandHeldSubInventory) {
                        IC2.platform.launchGuiClient(player, ((IHandHeldSubInventory)currentItem.getItem()).getSubInventory(player, currentItem, ID), isAdmin);
                      } else {
                        IC2.platform.launchGuiClient(player, ((IHandHeldInventory)currentItem.getItem()).getInventory(player, currentItem), isAdmin);
                      } 
                    } else if (player instanceof EntityPlayerSP) {
                      ((EntityPlayerSP)player).connection.sendPacket((Packet)new CPacketCloseWindow(windowId));
                    } 
                    player.openContainer.windowId = windowId;
                  }
                });
            break;
        } 
        return;
      case ExplosionEffect:
        object1 = DataEncoder.decodeDeferred(is, World.class);
        vec3d = DataEncoder.<Vec3d>decode(is, Vec3d.class);
        type = DataEncoder.<ExplosionIC2.Type>decodeEnum(is, ExplosionIC2.Type.class);
        IC2.platform.requestTick(false, new Runnable() {
              public void run() {
                World world = DataEncoder.<World>getValue(worldDeferred);
                if (world != null)
                  switch (type) {
                    case LargePacket:
                      world.playSound(player, new BlockPos(pos), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
                      world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, pos.x, pos.y, pos.z, 0.0D, 0.0D, 0.0D, new int[0]);
                      break;
                    case TileEntityEvent:
                      IC2.audioManager.playOnce(new AudioPosition(world, (float)pos.x, (float)pos.y, (float)pos.z), PositionSpec.Center, "Machines/MachineOverload.ogg", true, IC2.audioManager.getDefaultVolume());
                      world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, pos.x, pos.y, pos.z, 0.0D, 0.0D, 0.0D, new int[0]);
                      break;
                    case ItemEvent:
                      world.playSound(player, new BlockPos(pos), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 4.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
                      world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, pos.x, pos.y, pos.z, 0.0D, 0.0D, 0.0D, new int[0]);
                      break;
                    case GuiDisplay:
                      IC2.audioManager.playOnce(new AudioPosition(world, (float)pos.x, (float)pos.y, (float)pos.z), PositionSpec.Center, "Tools/NukeExplosion.ogg", true, IC2.audioManager.getDefaultVolume());
                      world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, pos.x, pos.y, pos.z, 0.0D, 0.0D, 0.0D, new int[0]);
                      break;
                  }  
              }
            });
        return;
      case Rpc:
        throw new RuntimeException("Received unexpected RPC packet");
      case TileEntityBlockComponent:
        dimensionId = is.readInt();
        pos = DataEncoder.<BlockPos>decode(is, BlockPos.class);
        componentName = is.readString();
        componentCls = Components.getClass(componentName);
        if (componentCls == null)
          throw new IOException("invalid component: " + componentName); 
        dataLen = is.readVarInt();
        if (dataLen > 65536)
          throw new IOException("data length limit exceeded"); 
        data = new byte[dataLen];
        is.readFully(data);
        IC2.platform.requestTick(false, new Runnable() {
              public void run() {
                WorldClient worldClient = (Minecraft.getMinecraft()).world;
                if (((World)worldClient).provider.getDimension() != dimensionId)
                  return; 
                TileEntity teRaw = worldClient.getTileEntity(pos);
                if (!(teRaw instanceof TileEntityBlock))
                  return; 
                TileEntityComponent component = ((TileEntityBlock)teRaw).getComponent(componentCls);
                if (component == null)
                  return; 
                DataInputStream dataIs = new DataInputStream(new ByteArrayInputStream(data));
                try {
                  component.onNetworkUpdate(dataIs);
                } catch (IOException e) {
                  throw new RuntimeException(e);
                } 
              }
            });
        return;
      case TileEntityBlockLandEffect:
        worldDeferred = DataEncoder.decodeDeferred(is, World.class);
        if (is.readBoolean()) {
          pos = (BlockPos)DataEncoder.decode(is, DataEncoder.EncodedType.BlockPos);
        } else {
          pos = null;
        } 
        x = is.readDouble();
        y = is.readDouble();
        z = is.readDouble();
        count = is.readInt();
        teBlock = TeBlockRegistry.get(is.readString());
        IC2.platform.requestTick(false, new Runnable() {
              public void run() {
                World world = DataEncoder.<World>getValue(worldDeferred);
                if (world == null)
                  return; 
                ParticleUtil.spawnBlockLandParticles(world, pos, x, y, z, count, teBlock);
              }
            });
        return;
      case TileEntityBlockRunEffect:
        worldDeferred = DataEncoder.decodeDeferred(is, World.class);
        if (is.readBoolean()) {
          pos = (BlockPos)DataEncoder.decode(is, DataEncoder.EncodedType.BlockPos);
        } else {
          pos = null;
        } 
        x = is.readDouble();
        y = is.readDouble();
        z = is.readDouble();
        xSpeed = is.readDouble();
        zSpeed = is.readDouble();
        iTeBlock1 = TeBlockRegistry.get(is.readString());
        IC2.platform.requestTick(false, new Runnable() {
              public void run() {
                World world = DataEncoder.<World>getValue(worldDeferred);
                if (world == null)
                  return; 
                ParticleUtil.spawnBlockRunParticles(world, pos, x, y, z, xSpeed, zSpeed, teBlock);
              }
            });
        return;
    } 
    onCommonPacketData(packetType, false, is, player);
  }
  
  private static void processChatPacket(GrowingBuffer buffer) {
    final String messages = buffer.readString();
    IC2.platform.requestTick(false, new Runnable() {
          public void run() {
            for (String line : messages.split("[\\r\\n]+"))
              IC2.platform.messagePlayer(null, line, new Object[0]); 
          }
        });
  }
  
  private static void processConsolePacket(GrowingBuffer buffer) {
    String messages = buffer.readString();
    PrintStream console = new PrintStream(new FileOutputStream(FileDescriptor.out));
    for (String line : messages.split("[\\r\\n]+"))
      console.println(line); 
    console.flush();
  }
}
