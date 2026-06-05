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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
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

   @Override
   protected boolean isClient() {
      return true;
   }

   @Override
   public void initiateClientItemEvent(ItemStack stack, int event) {
      try {
         GrowingBuffer buffer = new GrowingBuffer(256);
         SubPacketType.ItemEvent.writeTo(buffer);
         DataEncoder.encode(buffer, stack, false);
         buffer.writeInt(event);
         buffer.flip();
         this.sendPacket(buffer);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public void initiateKeyUpdate(int keyState) {
      GrowingBuffer buffer = new GrowingBuffer(5);
      SubPacketType.KeyUpdate.writeTo(buffer);
      buffer.writeInt(keyState);
      buffer.flip();
      this.sendPacket(buffer);
   }

   @Override
   public void initiateClientTileEntityEvent(TileEntity te, int event) {
      try {
         GrowingBuffer buffer = new GrowingBuffer(32);
         SubPacketType.TileEntityEvent.writeTo(buffer);
         DataEncoder.encode(buffer, te, false);
         buffer.writeInt(event);
         buffer.flip();
         this.sendPacket(buffer);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public void initiateRpc(int id, Class<? extends IRpcProvider<?>> provider, Object[] args) {
      try {
         GrowingBuffer buffer = new GrowingBuffer(256);
         SubPacketType.Rpc.writeTo(buffer);
         buffer.writeInt(id);
         buffer.writeString(provider.getName());
         DataEncoder.encode(buffer, args);
         buffer.flip();
         this.sendPacket(buffer);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public void requestGUI(IHasGui inventory) {
      try {
         GrowingBuffer buffer = new GrowingBuffer(32);
         SubPacketType.RequestGUI.writeTo(buffer);
         if (inventory instanceof TileEntity) {
            TileEntity te = (TileEntity)inventory;
            buffer.writeBoolean(false);
            DataEncoder.encode(buffer, te, false);
         } else {
            EntityPlayer player = Minecraft.getMinecraft().player;
            if ((
                  StackUtil.isEmpty(player.inventory.getCurrentItem())
                     || !(player.inventory.getCurrentItem().getItem() instanceof IHandHeldInventory)
               )
               && (StackUtil.isEmpty(player.getHeldItemOffhand()) || !(player.getHeldItemOffhand().getItem() instanceof IHandHeldInventory))) {
               IC2.platform
                  .displayError(
                     "An unknown GUI type was attempted to be displayed.\nThis could happen due to corrupted data from a player or a bug.\n\n(Technical information: "
                        + inventory
                        + ")"
                  );
            } else {
               buffer.writeBoolean(true);
            }
         }

         buffer.flip();
         this.sendPacket(buffer);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @SubscribeEvent
   public void onPacket(FMLNetworkEvent.ClientCustomPacketEvent event) {
      assert !this.getClass().getName().equals(NetworkManager.class.getName());

      try {
         this.onPacketData(GrowingBuffer.wrap(event.getPacket().payload()), Minecraft.getMinecraft().player);
      } catch (Throwable t) {
         IC2.log.warn(LogCategory.Network, t, "Network read failed");
         throw new RuntimeException(t);
      }

      event.getPacket().payload().release();
   }

   private void onPacketData(GrowingBuffer is, final EntityPlayer player) throws IOException {
      if (is.hasAvailable()) {
         SubPacketType packetType = SubPacketType.read(is, false);
         if (packetType != null) {
            switch (packetType) {
               case LargePacket:
                  int state = is.readUnsignedByte();
                  if ((state & 2) != 0) {
                     GrowingBuffer input;
                     if ((state & 1) != 0) {
                        input = is;
                     } else {
                        input = this.largePacketBuffer;
                        if (input == null) {
                           throw new IOException("unexpected large packet continuation");
                        }

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
                           return;
                        case 1:
                           processChatPacket(decompBuffer);
                           return;
                        case 2:
                           processConsolePacket(decompBuffer);
                     }
                  } else {
                     if ((state & 1) != 0) {
                        assert this.largePacketBuffer == null;
                        this.largePacketBuffer = new GrowingBuffer(32752);
                     }

                     if (this.largePacketBuffer == null) {
                        throw new IOException("unexpected large packet continuation");
                     }

                     is.writeTo(this.largePacketBuffer);
                  }
                  break;
               case TileEntityEvent: {
                  final Object teDeferred = DataEncoder.decodeDeferred(is, TileEntity.class);
                  final int event = is.readInt();
                  IC2.platform.requestTick(false, new Runnable() {
                     @Override
                     public void run() {
                        TileEntity te = DataEncoder.getValue(teDeferred);
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
                  final int event = is.readInt();
                  IC2.platform
                     .requestTick(
                        false,
                        new Runnable() {
                           @Override
                           public void run() {
                              World world = Minecraft.getMinecraft().world;

                              for (Object obj : world.playerEntities) {
                                 EntityPlayer player = (EntityPlayer)obj;
                                 if (profile.getId() != null && profile.getId().equals(player.getGameProfile().getId())
                                    || profile.getId() == null && profile.getName().equals(player.getGameProfile().getName())) {
                                    if (stack.getItem() instanceof INetworkItemEventListener) {
                                       ((INetworkItemEventListener)stack.getItem()).onNetworkEvent(stack, player, event);
                                    }
                                    break;
                                 }
                              }
                           }
                        }
                     );
                  break;
               }
               case GuiDisplay:
                  final boolean isAdmin = is.readBoolean();
                  switch (is.readByte()) {
                     case 0: {
                        final Object teDeferredx = DataEncoder.decodeDeferred(is, TileEntity.class);
                        final int windowId = is.readInt();
                        IC2.platform.requestTick(false, new Runnable() {
                           @Override
                           public void run() {
                              EntityPlayer player = IC2.platform.getPlayerInstance();
                              TileEntity te = DataEncoder.getValue(teDeferredx);
                              if (te instanceof IHasGui) {
                                 IC2.platform.launchGuiClient(player, (IHasGui)te, isAdmin);
                                 player.openContainer.windowId = windowId;
                              } else if (player instanceof EntityPlayerSP) {
                                 ((EntityPlayerSP)player).connection.sendPacket(new CPacketCloseWindow(windowId));
                              }
                           }
                        });
                        return;
                     }
                     case 1: {
                        final int currentItemPosition = is.readInt();
                        final boolean subGUI = is.readBoolean();
                        final short ID = subGUI ? is.readShort() : 0;
                        final int windowId = is.readInt();
                        IC2.platform
                           .requestTick(
                              false,
                              new Runnable() {
                                 @Override
                                 public void run() {
                                    EntityPlayer player = IC2.platform.getPlayerInstance();
                                    ItemStack currentItem;
                                    if (currentItemPosition < 0) {
                                       int actualItemPosition = ~currentItemPosition;
                                       if (actualItemPosition > player.inventory.offHandInventory.size() - 1) {
                                          return;
                                       }

                                       currentItem = (ItemStack)player.inventory.offHandInventory.get(actualItemPosition);
                                    } else {
                                       if (currentItemPosition != player.inventory.currentItem) {
                                          return;
                                       }

                                       currentItem = player.inventory.getCurrentItem();
                                    }

                                    if (currentItem != null && currentItem.getItem() instanceof IHandHeldInventory) {
                                       if (subGUI && currentItem.getItem() instanceof IHandHeldSubInventory) {
                                          IC2.platform
                                             .launchGuiClient(
                                                player, ((IHandHeldSubInventory)currentItem.getItem()).getSubInventory(player, currentItem, ID), isAdmin
                                             );
                                       } else {
                                          IC2.platform
                                             .launchGuiClient(
                                                player, ((IHandHeldInventory)currentItem.getItem()).getInventory(player, currentItem), isAdmin
                                             );
                                       }
                                    } else if (player instanceof EntityPlayerSP) {
                                       ((EntityPlayerSP)player).connection.sendPacket(new CPacketCloseWindow(windowId));
                                    }

                                    player.openContainer.windowId = windowId;
                                 }
                              }
                           );
                        return;
                     }
                     default:
                        return;
                  }
               case ExplosionEffect: {
                  final Object worldDeferred = DataEncoder.decodeDeferred(is, World.class);
                  final Vec3d pos = DataEncoder.decode(is, Vec3d.class);
                  final ExplosionIC2.Type type = DataEncoder.decodeEnum(is, ExplosionIC2.Type.class);
                  IC2.platform
                     .requestTick(
                        false,
                        new Runnable() {
                           @Override
                           public void run() {
                              World world = DataEncoder.getValue(worldDeferred);
                              if (world != null) {
                                 switch (type) {
                                    case Normal:
                                       world.playSound(
                                          player,
                                          new BlockPos(pos),
                                          SoundEvents.ENTITY_GENERIC_EXPLODE,
                                          SoundCategory.BLOCKS,
                                          4.0F,
                                          (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F
                                       );
                                       world.spawnParticle(
                                          EnumParticleTypes.EXPLOSION_HUGE, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0, new int[0]
                                       );
                                       break;
                                    case Electrical:
                                       IC2.audioManager
                                          .playOnce(
                                             new AudioPosition(world, (float)pos.x, (float)pos.y, (float)pos.z),
                                             PositionSpec.Center,
                                             "Machines/MachineOverload.ogg",
                                             true,
                                             IC2.audioManager.getDefaultVolume()
                                          );
                                       world.spawnParticle(
                                          EnumParticleTypes.EXPLOSION_HUGE, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0, new int[0]
                                       );
                                       break;
                                    case Heat:
                                       world.playSound(
                                          player,
                                          new BlockPos(pos),
                                          SoundEvents.BLOCK_FIRE_EXTINGUISH,
                                          SoundCategory.BLOCKS,
                                          4.0F,
                                          (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F
                                       );
                                       world.spawnParticle(
                                          EnumParticleTypes.EXPLOSION_HUGE, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0, new int[0]
                                       );
                                       break;
                                    case Nuclear:
                                       IC2.audioManager
                                          .playOnce(
                                             new AudioPosition(world, (float)pos.x, (float)pos.y, (float)pos.z),
                                             PositionSpec.Center,
                                             "Tools/NukeExplosion.ogg",
                                             true,
                                             IC2.audioManager.getDefaultVolume()
                                          );
                                       world.spawnParticle(
                                          EnumParticleTypes.EXPLOSION_HUGE, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0, new int[0]
                                       );
                                 }
                              }
                           }
                        }
                     );
                  break;
               }
               case Rpc:
                  throw new RuntimeException("Received unexpected RPC packet");
               case TileEntityBlockComponent: {
                  final int dimensionId = is.readInt();
                  final BlockPos pos = DataEncoder.decode(is, BlockPos.class);
                  String componentName = is.readString();
                  final Class<? extends TileEntityComponent> componentCls = Components.getClass(componentName);
                  if (componentCls == null) {
                     throw new IOException("invalid component: " + componentName);
                  }

                  int dataLen = is.readVarInt();
                  if (dataLen > 65536) {
                     throw new IOException("data length limit exceeded");
                  }

                  final byte[] data = new byte[dataLen];
                  is.readFully(data);
                  IC2.platform.requestTick(false, new Runnable() {
                     @Override
                     public void run() {
                        World world = Minecraft.getMinecraft().world;
                        if (world.provider.getDimension() == dimensionId) {
                           TileEntity teRaw = world.getTileEntity(pos);
                           if (teRaw instanceof TileEntityBlock) {
                              TileEntityComponent component = ((TileEntityBlock)teRaw).getComponent(componentCls);
                              if (component != null) {
                                 DataInputStream dataIs = new DataInputStream(new ByteArrayInputStream(data));

                                 try {
                                    component.onNetworkUpdate(dataIs);
                                 } catch (IOException e) {
                                    throw new RuntimeException(e);
                                 }
                              }
                           }
                        }
                     }
                  });
                  break;
               }
               case TileEntityBlockLandEffect: {
                  final Object worldDeferred = DataEncoder.decodeDeferred(is, World.class);
                  final BlockPos pos;
                  if (is.readBoolean()) {
                     pos = (BlockPos)DataEncoder.decode(is, DataEncoder.EncodedType.BlockPos);
                  } else {
                     pos = null;
                  }

                  final double x = is.readDouble();
                  final double y = is.readDouble();
                  final double z = is.readDouble();
                  final int count = is.readInt();
                  final ITeBlock teBlock = TeBlockRegistry.get(is.readString());
                  IC2.platform.requestTick(false, new Runnable() {
                     @Override
                     public void run() {
                        World world = DataEncoder.getValue(worldDeferred);
                        if (world != null) {
                           ParticleUtil.spawnBlockLandParticles(world, pos, x, y, z, count, teBlock);
                        }
                     }
                  });
                  break;
               }
               case TileEntityBlockRunEffect: {
                  final Object worldDeferred = DataEncoder.decodeDeferred(is, World.class);
                  final BlockPos pos;
                  if (is.readBoolean()) {
                     pos = (BlockPos)DataEncoder.decode(is, DataEncoder.EncodedType.BlockPos);
                  } else {
                     pos = null;
                  }

                  final double x = is.readDouble();
                  final double y = is.readDouble();
                  final double z = is.readDouble();
                  final double xSpeed = is.readDouble();
                  final double zSpeed = is.readDouble();
                  final ITeBlock teBlock = TeBlockRegistry.get(is.readString());
                  IC2.platform.requestTick(false, new Runnable() {
                     @Override
                     public void run() {
                        World world = DataEncoder.getValue(worldDeferred);
                        if (world != null) {
                           ParticleUtil.spawnBlockRunParticles(world, pos, x, y, z, xSpeed, zSpeed, teBlock);
                        }
                     }
                  });
                  break;
               }
               default:
                  this.onCommonPacketData(packetType, false, is, player);
            }
         }
      }
   }

   private static void processChatPacket(GrowingBuffer buffer) {
      final String messages = buffer.readString();
      IC2.platform.requestTick(false, new Runnable() {
         @Override
         public void run() {
            for (String line : messages.split("[\\r\\n]+")) {
               IC2.platform.messagePlayer(null, line);
            }
         }
      });
   }

   private static void processConsolePacket(GrowingBuffer buffer) {
      String messages = buffer.readString();
      PrintStream console = new PrintStream(new FileOutputStream(FileDescriptor.out));

      for (String line : messages.split("[\\r\\n]+")) {
         console.println(line);
      }

      console.flush();
   }
}
