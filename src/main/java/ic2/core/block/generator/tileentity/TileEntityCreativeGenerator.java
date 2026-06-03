package ic2.core.block.generator.tileentity;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IMultiEnergySource;
import ic2.core.block.TileEntityBlock;
import ic2.core.profile.NotClassic;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

@NotClassic
public class TileEntityCreativeGenerator extends TileEntityBlock implements IMultiEnergySource {
  public double getOfferedEnergy() {
    return Double.POSITIVE_INFINITY;
  }
  
  public void drawEnergy(double amount) {}
  
  public int getSourceTier() {
    return 1;
  }
  
  public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing side) {
    return true;
  }
  
  public boolean sendMultipleEnergyPackets() {
    return true;
  }
  
  public int getMultipleEnergyPacketAmount() {
    return 10;
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if (!(func_145831_w()).field_72995_K)
      MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent((IEnergyTile)this)); 
  }
  
  protected void onUnloaded() {
    if (!(func_145831_w()).field_72995_K)
      MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent((IEnergyTile)this)); 
    super.onUnloaded();
  }
}
