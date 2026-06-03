package ic2.core.block.wiring;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.ref.TeBlock.Delegated;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

@Delegated(current = TileEntityCableSplitter.class, old = TileEntityClassicCableSplitter.class)
public class TileEntityClassicCableSplitter extends TileEntityClassicCable {
  public final Redstone redstone;
  
  public TileEntityClassicCableSplitter() {
    super(CableType.splitter, 0);
    addComponent((TileEntityComponent)(this.redstone = new Redstone(this)));
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (this.redstone.hasRedstoneInput() == this.addedToEnergyNet)
      if (this.addedToEnergyNet) {
        MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent((IEnergyTile)this));
        this.addedToEnergyNet = false;
      } else {
        MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent((IEnergyTile)this));
        this.addedToEnergyNet = true;
      }  
    setActive(this.addedToEnergyNet);
  }
}
