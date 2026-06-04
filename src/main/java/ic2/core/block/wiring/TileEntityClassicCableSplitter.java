// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import ic2.api.energy.event.EnergyTileLoadEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import net.minecraftforge.common.MinecraftForge;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Redstone;
import ic2.core.ref.TeBlock;

@TeBlock.Delegated(current = TileEntityCableSplitter.class, old = TileEntityClassicCableSplitter.class)
public class TileEntityClassicCableSplitter extends TileEntityClassicCable
{
    public final Redstone redstone;
    
    public TileEntityClassicCableSplitter() {
        super(CableType.splitter, 0);
        this.addComponent(this.redstone = new Redstone(this));
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (this.redstone.hasRedstoneInput() == this.addedToEnergyNet) {
            if (this.addedToEnergyNet) {
                MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent(this));
                this.addedToEnergyNet = false;
            }
            else {
                MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent(this));
                this.addedToEnergyNet = true;
            }
        }
        this.setActive(this.addedToEnergyNet);
    }
}
