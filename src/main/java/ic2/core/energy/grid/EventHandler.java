package ic2.core.energy.grid;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.IEnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.info.ILocatable;
import ic2.core.IC2;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import java.lang.reflect.Method;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventHandler {
   private static boolean initialized;

   public static void init() {
      if (initialized) {
         throw new IllegalStateException("already initialized");
      }

      initialized = true;
      MinecraftForge.EVENT_BUS.register(new EventHandler());
   }

   private EventHandler() {
   }

   @SubscribeEvent
   public void onEnergyTileLoad(EnergyTileLoadEvent event) {
      if (event.getWorld().isRemote) {
         IC2.log
            .warn(
               LogCategory.EnergyNet,
               "EnergyTileLoadEvent: posted for %s client-side, aborting",
               Util.toString(event.tile, event.getWorld(), EnergyNet.instance.getPos(event.tile))
            );
      } else {
         if (event.tile instanceof TileEntity) {
            addTileToNet(event.tile);
         } else {
            if (!(event.tile instanceof ILocatable)) {
               throw new IllegalArgumentException("invalid tile type: " + event.tile);
            }

            addTileToNet(event.tile);
         }
      }
   }

   private static void addTileToNet(IEnergyTile tile) {
      try {
         Method addTileMethod = EnergyNet.instance.getClass().getDeclaredMethod("addTile", IEnergyTile.class);
         addTileMethod.setAccessible(true);
         addTileMethod.invoke(EnergyNet.instance, tile);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   @SubscribeEvent
   public void onEnergyTileUnload(EnergyTileUnloadEvent event) {
      if (event.getWorld().isRemote) {
         IC2.log
            .warn(
               LogCategory.EnergyNet,
               "EnergyTileUnloadEvent: posted for %s client-side, aborting",
               Util.toString(event.tile, event.getWorld(), EnergyNet.instance.getPos(event.tile))
            );
      } else {
         EnergyNet.instance.removeTile(event.tile);
      }
   }

   @SubscribeEvent
   public void onWorldTick(TickEvent.WorldTickEvent event) {
      EnergyNetLocal enet = EnergyNetGlobal.getLocal(event.world);
      if (event.phase == TickEvent.Phase.START) {
         enet.onTickStart();
      } else {
         enet.onTickEnd();
      }
   }
}
