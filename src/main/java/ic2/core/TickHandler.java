// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import java.util.WeakHashMap;
import java.util.Iterator;
import java.util.Collection;
import ic2.core.item.tool.ItemNanoSaber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.World;
import ic2.core.network.NetworkManager;
import ic2.core.util.Util;
import ic2.core.util.LogCategory;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntity;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import java.util.Map;

public class TickHandler
{
    private static final boolean debugupdate;
    private static final Map<IWorldTickCallback, Throwable> debugTraces;
    private static Throwable lastDebugTrace;
    
    public TickHandler() {
        MinecraftForge.EVENT_BUS.register((Object)this);
    }
    
    @SubscribeEvent
    public void onWorldTick(final TickEvent.WorldTickEvent event) {
        final World world = event.world;
        final WorldData worldData = WorldData.get(world, false);
        if (worldData == null) {
            return;
        }
        if (event.phase == TickEvent.Phase.START) {
            IC2.platform.profilerStartSection("updates");
            processUpdates(world, worldData);
            if (!world.isRemote) {
                IC2.platform.profilerEndStartSection("retrogen");
                Ic2WorldDecorator.onTick(world, worldData);
                IC2.platform.profilerEndStartSection("Wind");
                worldData.windSim.updateWind();
                if (ConfigUtil.getBool(MainConfig.get(), "balance/disableEnderChest")) {
                    IC2.platform.profilerEndStartSection("EnderChestCheck");
                    for (int i = 0; i < world.tickableTileEntities.size(); ++i) {
                        final TileEntity te = world.tickableTileEntities.get(i);
                        if (te instanceof TileEntityEnderChest && !te.isInvalid() && !world.isAirBlock(te.getPos())) {
                            world.setBlockToAir(te.getPos());
                            IC2.log.info(LogCategory.General, "Removed vanilla ender chest at %s.", Util.formatPosition(te));
                        }
                    }
                }
            }
            IC2.platform.profilerEndSection();
        }
        else {
            IC2.platform.profilerStartSection("Networking");
            IC2.network.get(!world.isRemote).onTickEnd(worldData);
            IC2.platform.profilerEndSection();
        }
    }
    
    @SubscribeEvent
    public void onServerTick(final TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ++ItemNanoSaber.ticker;
        }
    }
    
    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            IC2.platform.profilerStartSection("Keyboard");
            IC2.keyboard.sendKeyUpdate();
            IC2.platform.profilerEndStartSection("AudioManager");
            IC2.audioManager.onTick();
            IC2.platform.profilerEndStartSection("updates");
            final World world = IC2.platform.getPlayerWorld();
            if (world != null) {
                processUpdates(world, WorldData.get(world));
            }
            IC2.platform.profilerEndSection();
        }
    }
    
    public void requestSingleWorldTick(final World world, final IWorldTickCallback callback) {
        WorldData.get(world).singleUpdates.add(callback);
        if (TickHandler.debugupdate) {
            TickHandler.debugTraces.put(callback, new Throwable());
        }
    }
    
    public void requestContinuousWorldTick(final World world, final IWorldTickCallback update) {
        final WorldData worldData = WorldData.get(world);
        if (!worldData.continuousUpdatesInUse) {
            worldData.continuousUpdates.add(update);
        }
        else {
            worldData.continuousUpdatesToRemove.remove(update);
            worldData.continuousUpdatesToAdd.add(update);
        }
        if (TickHandler.debugupdate) {
            TickHandler.debugTraces.put(update, new Throwable());
        }
    }
    
    public void removeContinuousWorldTick(final World world, final IWorldTickCallback update) {
        final WorldData worldData = WorldData.get(world);
        if (!worldData.continuousUpdatesInUse) {
            worldData.continuousUpdates.remove(update);
        }
        else {
            worldData.continuousUpdatesToAdd.remove(update);
            worldData.continuousUpdatesToRemove.add(update);
        }
    }
    
    public static Throwable getLastDebugTrace() {
        return TickHandler.lastDebugTrace;
    }
    
    private static void processUpdates(final World world, final WorldData worldData) {
        IC2.platform.profilerStartSection("single-update");
        IWorldTickCallback callback;
        while ((callback = worldData.singleUpdates.poll()) != null) {
            if (TickHandler.debugupdate) {
                TickHandler.lastDebugTrace = TickHandler.debugTraces.remove(callback);
            }
            callback.onTick(world);
        }
        IC2.platform.profilerEndStartSection("cont-update");
        worldData.continuousUpdatesInUse = true;
        for (final IWorldTickCallback update : worldData.continuousUpdates) {
            if (TickHandler.debugupdate) {
                TickHandler.lastDebugTrace = TickHandler.debugTraces.remove(update);
            }
            update.onTick(world);
        }
        worldData.continuousUpdatesInUse = false;
        if (TickHandler.debugupdate) {
            TickHandler.lastDebugTrace = null;
        }
        worldData.continuousUpdates.addAll(worldData.continuousUpdatesToAdd);
        worldData.continuousUpdatesToAdd.clear();
        worldData.continuousUpdates.removeAll(worldData.continuousUpdatesToRemove);
        worldData.continuousUpdatesToRemove.clear();
        IC2.platform.profilerEndSection();
    }
    
    static {
        debugupdate = (System.getProperty("ic2.debugupdate") != null);
        debugTraces = (TickHandler.debugupdate ? new WeakHashMap<IWorldTickCallback, Throwable>() : null);
    }
}
