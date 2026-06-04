// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.energy.prefab;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class BasicEnergyTe<T extends BasicEnergyTile> extends TileEntity
{
    protected T energyBuffer;
    
    protected BasicEnergyTe() {
    }
    
    public T getEnergyBuffer() {
        return this.energyBuffer;
    }
    
    public void onLoad() {
        this.energyBuffer.onLoad();
    }
    
    public void invalidate() {
        super.invalidate();
        this.energyBuffer.invalidate();
    }
    
    public void onChunkUnload() {
        this.energyBuffer.onChunkUnload();
    }
    
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.energyBuffer.readFromNBT(nbt);
    }
    
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        return this.energyBuffer.writeToNBT(super.writeToNBT(nbt));
    }
    
    public static class Sink extends BasicEnergyTe<BasicSink>
    {
        public Sink(final int capacity, final int tier) {
            this.energyBuffer = (T)new BasicSink(this, capacity, tier);
        }
    }
    
    public static class Source extends BasicEnergyTe<BasicSource>
    {
        public Source(final int capacity, final int tier) {
            this.energyBuffer = (T)new BasicSource(this, capacity, tier);
        }
    }
}
