// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy.grid;

import net.minecraftforge.fml.common.gameevent.TickEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ic2.api.info.ILocatable;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import ic2.core.util.Util;
import ic2.api.energy.EnergyNet;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.api.energy.event.EnergyTileLoadEvent;
import net.minecraftforge.common.MinecraftForge;

public class EventHandler
{
    private static boolean initialized;
    
    public static void init() {
        if (EventHandler.initialized) {
            throw new IllegalStateException("already initialized");
        }
        EventHandler.initialized = true;
        MinecraftForge.EVENT_BUS.register((Object)new EventHandler());
    }
    
    private EventHandler() {
    }
    
    @SubscribeEvent
    public void onEnergyTileLoad(final EnergyTileLoadEvent event) {
        if (event.getWorld().isRemote) {
            IC2.log.warn(LogCategory.EnergyNet, "EnergyTileLoadEvent: posted for %s client-side, aborting", Util.toString(event.tile, (IBlockAccess)event.getWorld(), EnergyNet.instance.getPos(event.tile)));
            return;
        }
        if (event.tile instanceof TileEntity) {
            EnergyNet.instance.addTile((TileEntity)event.tile);
        }
        else {
            if (!(event.tile instanceof ILocatable)) {
                throw new IllegalArgumentException("invalid tile type: " + event.tile);
            }
            EnergyNet.instance.addTile((ILocatable)event.tile);
        }
    }
    
    @SubscribeEvent
    public void onEnergyTileUnload(final EnergyTileUnloadEvent event) {
        if (event.getWorld().isRemote) {
            IC2.log.warn(LogCategory.EnergyNet, "EnergyTileUnloadEvent: posted for %s client-side, aborting", Util.toString(event.tile, (IBlockAccess)event.getWorld(), EnergyNet.instance.getPos(event.tile)));
            return;
        }
        EnergyNet.instance.removeTile(event.tile);
    }
    
    @SubscribeEvent
    public void onWorldTick(final TickEvent.WorldTickEvent event) {
        final EnergyNetLocal enet = EnergyNetGlobal.getLocal(event.world);
        if (event.phase == TickEvent.Phase.START) {
            enet.onTickStart();
        }
        else {
            enet.onTickEnd();
        }
    }
}
