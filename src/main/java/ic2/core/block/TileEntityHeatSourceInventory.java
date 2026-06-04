// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import ic2.core.IC2;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import ic2.core.network.GuiSynced;
import ic2.api.energy.tile.IHeatSource;

public abstract class TileEntityHeatSourceInventory extends TileEntityInventory implements IHeatSource
{
    @GuiSynced
    protected int transmitHeat;
    @GuiSynced
    protected int maxHeatEmitpeerTick;
    protected int HeatBuffer;
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        final int amount = this.getMaxHeatEmittedPerTick() - this.HeatBuffer;
        if (amount > 0) {
            this.addtoHeatBuffer(this.fillHeatBuffer(amount));
        }
    }
    
    @Override
    public int maxrequestHeatTick(final EnumFacing directionFrom) {
        return this.getConnectionBandwidth(directionFrom);
    }
    
    @Override
    public int getConnectionBandwidth(final EnumFacing side) {
        if (this.facingMatchesDirection(side)) {
            return this.getMaxHeatEmittedPerTick();
        }
        return 0;
    }
    
    @Override
    public int requestHeat(final EnumFacing directionFrom, final int requestheat) {
        return this.drawHeat(directionFrom, requestheat, false);
    }
    
    @Override
    public int drawHeat(final EnumFacing side, final int request, final boolean simulate) {
        if (!this.facingMatchesDirection(side)) {
            return 0;
        }
        final int heatBuffer = this.getHeatBuffer();
        if (heatBuffer >= request) {
            if (!simulate) {
                this.setHeatBuffer(heatBuffer - request);
                this.transmitHeat = request;
            }
            return request;
        }
        if (!simulate) {
            this.transmitHeat = heatBuffer;
            this.setHeatBuffer(0);
        }
        return heatBuffer;
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
        this.HeatBuffer = nbtTagCompound.getInteger("HeatBuffer");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("HeatBuffer", this.HeatBuffer);
        return nbt;
    }
    
    @Override
    public void markDirty() {
        super.markDirty();
        if (IC2.platform.isSimulating()) {
            this.maxHeatEmitpeerTick = this.getMaxHeatEmittedPerTick();
        }
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        if (IC2.platform.isSimulating()) {
            this.maxHeatEmitpeerTick = this.getMaxHeatEmittedPerTick();
        }
    }
    
    public boolean facingMatchesDirection(final EnumFacing direction) {
        return direction == this.getFacing();
    }
    
    public int getHeatBuffer() {
        return this.HeatBuffer;
    }
    
    public void setHeatBuffer(final int HeatBuffer) {
        this.HeatBuffer = HeatBuffer;
    }
    
    public void addtoHeatBuffer(final int heat) {
        this.setHeatBuffer(this.getHeatBuffer() + heat);
    }
    
    public int gettransmitHeat() {
        return this.transmitHeat;
    }
    
    public String getOutput() {
        return this.gettransmitHeat() + " / " + this.getMaxHeatEmittedPerTick();
    }
    
    protected abstract int fillHeatBuffer(final int p0);
    
    public abstract int getMaxHeatEmittedPerTick();
}
