package ic2.api.energy.prefab;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class BasicEnergyTe<T extends BasicEnergyTile> extends TileEntity {
  protected T energyBuffer;
  
  public static class Sink extends BasicEnergyTe<BasicSink> {
    public Sink(int capacity, int tier) {
      this.energyBuffer = new BasicSink(this, capacity, tier);
    }
  }
  
  public static class Source extends BasicEnergyTe<BasicSource> {
    public Source(int capacity, int tier) {
      this.energyBuffer = new BasicSource(this, capacity, tier);
    }
  }
  
  public T getEnergyBuffer() {
    return this.energyBuffer;
  }
  
  public void onLoad() {
    this.energyBuffer.onLoad();
  }
  
  public void func_145843_s() {
    super.func_145843_s();
    this.energyBuffer.invalidate();
  }
  
  public void onChunkUnload() {
    this.energyBuffer.onChunkUnload();
  }
  
  public void func_145839_a(NBTTagCompound nbt) {
    super.func_145839_a(nbt);
    this.energyBuffer.readFromNBT(nbt);
  }
  
  public NBTTagCompound func_189515_b(NBTTagCompound nbt) {
    return this.energyBuffer.writeToNBT(super.func_189515_b(nbt));
  }
}
