// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.network;

import ic2.api.network.INetworkUpdateListener;
import ic2.core.block.TileEntityBlock;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import ic2.core.util.Util;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.core.util.ReflectionUtil;
import ic2.core.block.TeBlockRegistry;
import net.minecraft.util.math.BlockPos;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import ic2.api.network.IGrowingBuffer;
import net.minecraft.tileentity.TileEntity;
import java.util.Map;
import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayerMP;
import java.util.IdentityHashMap;
import ic2.core.WorldData;

class TeUpdate
{
    static final boolean debug;
    
    public static void send(final WorldData worldData, final NetworkManager network) throws IOException {
        if (worldData.tesToUpdate.isEmpty()) {
            return;
        }
        final Map<EntityPlayerMP, GrowingBuffer> buffers = new IdentityHashMap<EntityPlayerMP, GrowingBuffer>();
        final List<EntityPlayerMP> playersInRange = new ArrayList<EntityPlayerMP>();
        final GrowingBuffer commonBuffer = new GrowingBuffer();
        for (final Map.Entry<TileEntity, TeUpdateDataServer> entry : worldData.tesToUpdate.entrySet()) {
            final TileEntity te = entry.getKey();
            NetworkManager.getPlayersInRange(te.getWorld(), te.getPos(), playersInRange);
            if (playersInRange.isEmpty()) {
                continue;
            }
            final TeUpdateDataServer updateData = entry.getValue();
            DataEncoder.encode(commonBuffer, te.getPos(), false);
            commonBuffer.mark();
            commonBuffer.writeShort(0);
            for (final String field : updateData.getGlobalFields()) {
                NetworkManager.writeFieldData(te, field, commonBuffer);
            }
            commonBuffer.flip();
            for (final EntityPlayerMP player : playersInRange) {
                final Collection<String> playerFields = updateData.getPlayerFields(player);
                final int fieldCount = updateData.getGlobalFields().size() + playerFields.size();
                if (fieldCount == 0) {
                    continue;
                }
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
                for (final String field2 : playerFields) {
                    NetworkManager.writeFieldData(te, field2, playerBuffer);
                }
            }
            commonBuffer.clear();
            playersInRange.clear();
        }
        worldData.tesToUpdate.clear();
        for (final Map.Entry<EntityPlayerMP, GrowingBuffer> entry2 : buffers.entrySet()) {
            final EntityPlayerMP player2 = entry2.getKey();
            final GrowingBuffer playerBuffer2 = entry2.getValue();
            playerBuffer2.flip();
            network.sendLargePacket(player2, 0, playerBuffer2);
        }
    }
    
    static void receive(final GrowingBuffer buffer) throws IOException {
        final int dimensionId = buffer.readInt();
        final TeUpdateDataClient updateData = new TeUpdateDataClient();
        while (buffer.hasAvailable()) {
            final BlockPos pos = DataEncoder.decode(buffer, BlockPos.class);
            final int fieldCount = buffer.readUnsignedShort();
            final TeUpdateDataClient.TeData teData = updateData.addTe(pos, fieldCount);
            for (int i = 0; i < fieldCount; ++i) {
                final String fieldName = buffer.readString();
                final Object value = DataEncoder.decode(buffer);
                if (fieldName.equals("teBlk")) {
                    final String name = (String)value;
                    if (name.startsWith("Old-")) {
                        teData.teClass = TeBlockRegistry.getOld(name);
                    }
                    else {
                        teData.teClass = TeBlockRegistry.get(name).getTeClass();
                    }
                }
                else {
                    teData.addField(fieldName, value);
                }
            }
            if (teData.teClass != null) {
                for (final TeUpdateDataClient.FieldData fieldData : teData.getFields()) {
                    fieldData.field = ReflectionUtil.getFieldRecursive(teData.teClass, fieldData.name);
                }
            }
        }
        if (TeUpdate.debug) {
            printDebugOutput(dimensionId, updateData);
        }
        IC2.platform.requestTick(false, new Runnable() {
            @Override
            public void run() {
                final World world = IC2.platform.getPlayerWorld();
                if (world == null || world.provider.getDimension() != dimensionId) {
                    return;
                }
                for (final TeUpdateDataClient.TeData update : updateData.getTes()) {
                    try {
                        apply(update, world);
                    }
                    catch (final Throwable t) {
                        IC2.log.warn(LogCategory.Network, t, "TE update at %s failed.", Util.formatPosition((IBlockAccess)world, update.pos));
                    }
                }
            }
        });
    }
    
    private static void printDebugOutput(final int dimensionId, final TeUpdateDataClient data) {
        final StringBuilder out = new StringBuilder();
        out.append("dimension: ");
        out.append(dimensionId);
        out.append(", ");
        out.append(data.getTes().size());
        out.append(" tes:\n");
        for (final TeUpdateDataClient.TeData te : data.getTes()) {
            out.append("  pos: ");
            out.append(te.pos.getX());
            out.append('/');
            out.append(te.pos.getY());
            out.append('/');
            out.append(te.pos.getZ());
            out.append(", ");
            out.append(te.getFields().size());
            out.append(" fields:\n");
            for (final TeUpdateDataClient.FieldData field : te.getFields()) {
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
            }
            else {
                out.append("    no TE Class\n");
            }
        }
        out.setLength(out.length() - 1);
        IC2.log.info(LogCategory.Network, "Received TE Update:\n" + out.toString());
    }
    
    private static void apply(final TeUpdateDataClient.TeData update, final World world) {
        if (!world.isBlockLoaded(update.pos, false)) {
            if (TeUpdate.debug) {
                IC2.log.info(LogCategory.Network, "Skipping update at %s, chunk not loaded.", Util.formatPosition((IBlockAccess)world, update.pos));
            }
            return;
        }
        TileEntity te = world.getTileEntity(update.pos);
        if (update.teClass != null && (te == null || te.getClass() != update.teClass || te.isInvalid() || te.getWorld() != world)) {
            if (TeUpdate.debug) {
                IC2.log.info(LogCategory.Network, "Instantiating %s with %s.", Util.formatPosition((IBlockAccess)world, update.pos), update.teClass.getName());
            }
            te = TileEntityBlock.instantiate((Class<TileEntity>)update.teClass);
            world.setTileEntity(update.pos, te);
            assert !te.isInvalid();
            assert te.getWorld() == world;
        }
        else {
            if (te == null) {
                if (TeUpdate.debug) {
                    IC2.log.info(LogCategory.Network, "Can't apply update at %s, no te and no teClass.", Util.formatPosition((IBlockAccess)world, update.pos));
                }
                return;
            }
            if (te.isInvalid() || te.getWorld() != world) {
                if (TeUpdate.debug) {
                    IC2.log.warn(LogCategory.Network, "Can't apply update at %s, invalid te and no teClass.", Util.formatPosition((IBlockAccess)world, update.pos));
                }
                return;
            }
            if (TeUpdate.debug) {
                IC2.log.info(LogCategory.Network, "TE class at %s unchanged.", Util.formatPosition((IBlockAccess)world, update.pos));
            }
        }
        for (final TeUpdateDataClient.FieldData fieldUpdate : update.getFields()) {
            final Object value = DataEncoder.getValue(fieldUpdate.value);
            if (fieldUpdate.field != null) {
                ReflectionUtil.setValue(te, fieldUpdate.field, value);
            }
            else {
                ReflectionUtil.setValueRecursive(te, fieldUpdate.name, value);
            }
            if (te instanceof INetworkUpdateListener) {
                ((INetworkUpdateListener)te).onNetworkUpdate(fieldUpdate.name);
            }
        }
    }
    
    static {
        debug = (System.getProperty("ic2.network.debug.teupdate") != null);
    }
}
