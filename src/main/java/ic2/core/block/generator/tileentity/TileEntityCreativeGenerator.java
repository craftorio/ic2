// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.generator.tileentity;

import ic2.api.energy.event.EnergyTileUnloadEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.event.EnergyTileLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.util.EnumFacing;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.core.profile.NotClassic;
import ic2.api.energy.tile.IMultiEnergySource;
import ic2.core.block.TileEntityBlock;

@NotClassic
public class TileEntityCreativeGenerator extends TileEntityBlock implements IMultiEnergySource
{
    public double getOfferedEnergy() {
        return Double.POSITIVE_INFINITY;
    }
    
    public void drawEnergy(final double amount) {
    }
    
    public int getSourceTier() {
        return 1;
    }
    
    public boolean emitsEnergyTo(final IEnergyAcceptor receiver, final EnumFacing side) {
        return true;
    }
    
    @Override
    public boolean sendMultipleEnergyPackets() {
        return true;
    }
    
    @Override
    public int getMultipleEnergyPacketAmount() {
        return 10;
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        if (!this.getWorld().isRemote) {
            MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent(this));
        }
    }
    
    @Override
    protected void onUnloaded() {
        if (!this.getWorld().isRemote) {
            MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent(this));
        }
        super.onUnloaded();
    }
}
