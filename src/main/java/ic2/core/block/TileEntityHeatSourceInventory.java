package ic2.core.block;

import ic2.api.energy.tile.IHeatSource;
import ic2.core.IC2;
import ic2.core.network.GuiSynced;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public abstract class TileEntityHeatSourceInventory extends TileEntityInventory implements IHeatSource {
  @GuiSynced
  protected int transmitHeat;
  
  @GuiSynced
  protected int maxHeatEmitpeerTick;
  
  protected int HeatBuffer;
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    int amount = getMaxHeatEmittedPerTick() - this.HeatBuffer;
    if (amount > 0)
      addtoHeatBuffer(fillHeatBuffer(amount)); 
  }
  
  public int maxrequestHeatTick(EnumFacing directionFrom) {
    return getConnectionBandwidth(directionFrom);
  }
  
  public int getConnectionBandwidth(EnumFacing side) {
    if (facingMatchesDirection(side))
      return getMaxHeatEmittedPerTick(); 
    return 0;
  }
  
  public int requestHeat(EnumFacing directionFrom, int requestheat) {
    return drawHeat(directionFrom, requestheat, false);
  }
  
  public int drawHeat(EnumFacing side, int request, boolean simulate) {
    if (facingMatchesDirection(side)) {
      int heatBuffer = getHeatBuffer();
      if (heatBuffer >= request) {
        if (!simulate) {
          setHeatBuffer(heatBuffer - request);
          this.transmitHeat = request;
        } 
        return request;
      } 
      if (!simulate) {
        this.transmitHeat = heatBuffer;
        setHeatBuffer(0);
      } 
      return heatBuffer;
    } 
    return 0;
  }
  
  public void func_145839_a(NBTTagCompound nbtTagCompound) {
    super.func_145839_a(nbtTagCompound);
    this.HeatBuffer = nbtTagCompound.func_74762_e("HeatBuffer");
  }
  
  public NBTTagCompound func_189515_b(NBTTagCompound nbt) {
    super.func_189515_b(nbt);
    nbt.func_74768_a("HeatBuffer", this.HeatBuffer);
    return nbt;
  }
  
  public void func_70296_d() {
    super.func_70296_d();
    if (IC2.platform.isSimulating())
      this.maxHeatEmitpeerTick = getMaxHeatEmittedPerTick(); 
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if (IC2.platform.isSimulating())
      this.maxHeatEmitpeerTick = getMaxHeatEmittedPerTick(); 
  }
  
  public boolean facingMatchesDirection(EnumFacing direction) {
    return (direction == getFacing());
  }
  
  public int getHeatBuffer() {
    return this.HeatBuffer;
  }
  
  public void setHeatBuffer(int HeatBuffer) {
    this.HeatBuffer = HeatBuffer;
  }
  
  public void addtoHeatBuffer(int heat) {
    setHeatBuffer(getHeatBuffer() + heat);
  }
  
  public int gettransmitHeat() {
    return this.transmitHeat;
  }
  
  public String getOutput() {
    return gettransmitHeat() + " / " + getMaxHeatEmittedPerTick();
  }
  
  protected abstract int fillHeatBuffer(int paramInt);
  
  public abstract int getMaxHeatEmittedPerTick();
}
