package ic2.core;

import ic2.core.init.MainConfig;
import ic2.core.item.tool.ItemNanoSaber;
import ic2.core.network.NetworkManager;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TickHandler {
  public TickHandler() {
    MinecraftForge.EVENT_BUS.register(this);
  }
  
  @SubscribeEvent
  public void onWorldTick(TickEvent.WorldTickEvent event) {
    World world = event.world;
    WorldData worldData = WorldData.get(world, false);
    if (worldData == null)
      return; 
    if (event.phase == TickEvent.Phase.START) {
      IC2.platform.profilerStartSection("updates");
      processUpdates(world, worldData);
      if (!world.field_72995_K) {
        IC2.platform.profilerEndStartSection("retrogen");
        Ic2WorldDecorator.onTick(world, worldData);
        IC2.platform.profilerEndStartSection("Wind");
        worldData.windSim.updateWind();
        if (ConfigUtil.getBool(MainConfig.get(), "balance/disableEnderChest")) {
          IC2.platform.profilerEndStartSection("EnderChestCheck");
          for (int i = 0; i < world.field_175730_i.size(); i++) {
            TileEntity te = world.field_175730_i.get(i);
            if (te instanceof net.minecraft.tileentity.TileEntityEnderChest && !te.func_145837_r() && !world.func_175623_d(te.func_174877_v())) {
              world.func_175698_g(te.func_174877_v());
              IC2.log.info(LogCategory.General, "Removed vanilla ender chest at %s.", new Object[] { Util.formatPosition(te) });
            } 
          } 
        } 
      } 
      IC2.platform.profilerEndSection();
    } else {
      IC2.platform.profilerStartSection("Networking");
      ((NetworkManager)IC2.network.get(!world.field_72995_K)).onTickEnd(worldData);
      IC2.platform.profilerEndSection();
    } 
  }
  
  @SubscribeEvent
  public void onServerTick(TickEvent.ServerTickEvent event) {
    if (event.phase == TickEvent.Phase.START)
      ItemNanoSaber.ticker++; 
  }
  
  @SubscribeEvent
  public void onClientTick(TickEvent.ClientTickEvent event) {
    if (event.phase == TickEvent.Phase.START) {
      IC2.platform.profilerStartSection("Keyboard");
      IC2.keyboard.sendKeyUpdate();
      IC2.platform.profilerEndStartSection("AudioManager");
      IC2.audioManager.onTick();
      IC2.platform.profilerEndStartSection("updates");
      World world = IC2.platform.getPlayerWorld();
      if (world != null)
        processUpdates(world, WorldData.get(world)); 
      IC2.platform.profilerEndSection();
    } 
  }
  
  public void requestSingleWorldTick(World world, IWorldTickCallback callback) {
    (WorldData.get(world)).singleUpdates.add(callback);
    if (debugupdate)
      debugTraces.put(callback, new Throwable()); 
  }
  
  public void requestContinuousWorldTick(World world, IWorldTickCallback update) {
    WorldData worldData = WorldData.get(world);
    if (!worldData.continuousUpdatesInUse) {
      worldData.continuousUpdates.add(update);
    } else {
      worldData.continuousUpdatesToRemove.remove(update);
      worldData.continuousUpdatesToAdd.add(update);
    } 
    if (debugupdate)
      debugTraces.put(update, new Throwable()); 
  }
  
  public void removeContinuousWorldTick(World world, IWorldTickCallback update) {
    WorldData worldData = WorldData.get(world);
    if (!worldData.continuousUpdatesInUse) {
      worldData.continuousUpdates.remove(update);
    } else {
      worldData.continuousUpdatesToAdd.remove(update);
      worldData.continuousUpdatesToRemove.add(update);
    } 
  }
  
  public static Throwable getLastDebugTrace() {
    return lastDebugTrace;
  }
  
  private static void processUpdates(World world, WorldData worldData) {
    IC2.platform.profilerStartSection("single-update");
    IWorldTickCallback callback;
    while ((callback = worldData.singleUpdates.poll()) != null) {
      if (debugupdate)
        lastDebugTrace = debugTraces.remove(callback); 
      callback.onTick(world);
    } 
    IC2.platform.profilerEndStartSection("cont-update");
    worldData.continuousUpdatesInUse = true;
    for (IWorldTickCallback update : worldData.continuousUpdates) {
      if (debugupdate)
        lastDebugTrace = debugTraces.remove(update); 
      update.onTick(world);
    } 
    worldData.continuousUpdatesInUse = false;
    if (debugupdate)
      lastDebugTrace = null; 
    worldData.continuousUpdates.addAll(worldData.continuousUpdatesToAdd);
    worldData.continuousUpdatesToAdd.clear();
    worldData.continuousUpdates.removeAll(worldData.continuousUpdatesToRemove);
    worldData.continuousUpdatesToRemove.clear();
    IC2.platform.profilerEndSection();
  }
  
  private static final boolean debugupdate = (System.getProperty("ic2.debugupdate") != null);
  
  private static final Map<IWorldTickCallback, Throwable> debugTraces = debugupdate ? new WeakHashMap<>() : null;
  
  private static Throwable lastDebugTrace;
}
