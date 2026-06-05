package ic2.core.network;

import ic2.api.network.INetworkUpdateListener;
import ic2.core.IC2;
import ic2.core.WorldData;
import ic2.core.block.TeBlockRegistry;
import ic2.core.block.TileEntityBlock;
import ic2.core.util.LogCategory;
import ic2.core.util.ReflectionUtil;
import ic2.core.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

class TeUpdate {
   static final boolean debug = System.getProperty("ic2.network.debug.teupdate") != null;

   public static void send(WorldData worldData, NetworkManager network) throws IOException {
      if (!worldData.tesToUpdate.isEmpty()) {
         Map<EntityPlayerMP, GrowingBuffer> buffers = new IdentityHashMap<>();
         List<EntityPlayerMP> playersInRange = new ArrayList<>();
         GrowingBuffer commonBuffer = new GrowingBuffer();

         for (Entry<TileEntity, TeUpdateDataServer> entry : worldData.tesToUpdate.entrySet()) {
            TileEntity te = entry.getKey();
            NetworkManager.getPlayersInRange(te.getWorld(), te.getPos(), playersInRange);
            if (!playersInRange.isEmpty()) {
               TeUpdateDataServer updateData = entry.getValue();
               DataEncoder.encode(commonBuffer, te.getPos(), false);
               commonBuffer.mark();
               commonBuffer.writeShort(0);

               for (String field : updateData.getGlobalFields()) {
                  NetworkManager.writeFieldData(te, field, commonBuffer);
               }

               commonBuffer.flip();

               for (EntityPlayerMP player : playersInRange) {
                  Collection<String> playerFields = updateData.getPlayerFields(player);
                  int fieldCount = updateData.getGlobalFields().size() + playerFields.size();
                  if (fieldCount != 0) {
                     if (fieldCount > 65535) {
                        throw new RuntimeException("too many fields for " + te + ": " + fieldCount);
                     }

                     commonBuffer.reset();
                     commonBuffer.writeShort(fieldCount);
                     commonBuffer.rewind();
                     GrowingBuffer playerBuffer = buffers.get(player);
                     if (playerBuffer == null) {
                        playerBuffer = new GrowingBuffer(0);
                        buffers.put(player, playerBuffer);
                        playerBuffer.writeInt(player.getEntityWorld().provider.getDimension());
                     }

                     commonBuffer.writeTo(playerBuffer);
                     commonBuffer.rewind();

                     for (String field : playerFields) {
                        NetworkManager.writeFieldData(te, field, playerBuffer);
                     }
                  }
               }

               commonBuffer.clear();
               playersInRange.clear();
            }
         }

         worldData.tesToUpdate.clear();

         for (Entry<EntityPlayerMP, GrowingBuffer> entry : buffers.entrySet()) {
            EntityPlayerMP player = entry.getKey();
            GrowingBuffer playerBuffer = entry.getValue();
            playerBuffer.flip();
            network.sendLargePacket(player, 0, playerBuffer);
         }
      }
   }

   static void receive(GrowingBuffer buffer) throws IOException {
      final int dimensionId = buffer.readInt();
      final TeUpdateDataClient updateData = new TeUpdateDataClient();

      while (buffer.hasAvailable()) {
         BlockPos pos = DataEncoder.decode(buffer, BlockPos.class);
         int fieldCount = buffer.readUnsignedShort();
         TeUpdateDataClient.TeData teData = updateData.addTe(pos, fieldCount);

         for (int i = 0; i < fieldCount; i++) {
            String fieldName = buffer.readString();
            Object value = DataEncoder.decode(buffer);
            if (fieldName.equals("teBlk")) {
               String name = (String)value;
               if (name.startsWith("Old-")) {
                  teData.teClass = TeBlockRegistry.getOld(name);
               } else {
                  teData.teClass = TeBlockRegistry.get(name).getTeClass();
               }
            } else {
               teData.addField(fieldName, value);
            }
         }

         if (teData.teClass != null) {
            for (TeUpdateDataClient.FieldData fieldData : teData.getFields()) {
               fieldData.field = ReflectionUtil.getFieldRecursive(teData.teClass, fieldData.name);
            }
         }
      }

      if (debug) {
         printDebugOutput(dimensionId, updateData);
      }

      IC2.platform.requestTick(false, new Runnable() {
         @Override
         public void run() {
            World world = IC2.platform.getPlayerWorld();
            if (world != null && world.provider.getDimension() == dimensionId) {
               for (TeUpdateDataClient.TeData update : updateData.getTes()) {
                  try {
                     TeUpdate.apply(update, world);
                  } catch (Throwable t) {
                     IC2.log.warn(LogCategory.Network, t, "TE update at %s failed.", Util.formatPosition(world, update.pos));
                  }
               }
            }
         }
      });
   }

   private static void printDebugOutput(int dimensionId, TeUpdateDataClient data) {
      StringBuilder out = new StringBuilder();
      out.append("dimension: ");
      out.append(dimensionId);
      out.append(", ");
      out.append(data.getTes().size());
      out.append(" tes:\n");

      for (TeUpdateDataClient.TeData te : data.getTes()) {
         out.append("  pos: ");
         out.append(te.pos.getX());
         out.append('/');
         out.append(te.pos.getY());
         out.append('/');
         out.append(te.pos.getZ());
         out.append(", ");
         out.append(te.getFields().size());
         out.append(" fields:\n");

         for (TeUpdateDataClient.FieldData field : te.getFields()) {
            out.append("    ");
            out.append(field.name);
            out.append(" = ");
            out.append(field.value);
            if (field.value != null) {
               out.append(" (");
               out.append(field.value.getClass().getSimpleName());
               out.append(')');
            }

            out.append('\n');
         }

         if (te.teClass != null) {
            out.append("    TE Class: ");
            out.append(te.teClass.getName());
            out.append('\n');
         } else {
            out.append("    no TE Class\n");
         }
      }

      out.setLength(out.length() - 1);
      IC2.log.info(LogCategory.Network, "Received TE Update:\n" + out.toString());
   }

   private static void apply(TeUpdateDataClient.TeData update, World world) {
      if (!world.isBlockLoaded(update.pos, false)) {
         if (debug) {
            IC2.log.info(LogCategory.Network, "Skipping update at %s, chunk not loaded.", Util.formatPosition(world, update.pos));
         }
      } else {
         TileEntity te = world.getTileEntity(update.pos);
         if (update.teClass != null && (te == null || te.getClass() != update.teClass || te.isInvalid() || te.getWorld() != world)) {
            if (debug) {
               IC2.log.info(LogCategory.Network, "Instantiating %s with %s.", Util.formatPosition(world, update.pos), update.teClass.getName());
            }

            te = TileEntityBlock.instantiate(update.teClass);
            world.setTileEntity(update.pos, te);
            assert !te.isInvalid();
            assert te.getWorld() == world;
         } else {
            if (te == null) {
               if (debug) {
                  IC2.log.info(LogCategory.Network, "Can't apply update at %s, no te and no teClass.", Util.formatPosition(world, update.pos));
               }

               return;
            }

            if (te.isInvalid() || te.getWorld() != world) {
               if (debug) {
                  IC2.log.warn(LogCategory.Network, "Can't apply update at %s, invalid te and no teClass.", Util.formatPosition(world, update.pos));
               }

               return;
            }

            if (debug) {
               IC2.log.info(LogCategory.Network, "TE class at %s unchanged.", Util.formatPosition(world, update.pos));
            }
         }

         for (TeUpdateDataClient.FieldData fieldUpdate : update.getFields()) {
            Object value = DataEncoder.getValue(fieldUpdate.value);
            if (fieldUpdate.field != null) {
               ReflectionUtil.setValue(te, fieldUpdate.field, value);
            } else {
               ReflectionUtil.setValueRecursive(te, fieldUpdate.name, value);
            }

            if (te instanceof INetworkUpdateListener) {
               ((INetworkUpdateListener)te).onNetworkUpdate(fieldUpdate.name);
            }
         }
      }
   }
}
