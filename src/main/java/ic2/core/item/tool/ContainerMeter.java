// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.api.energy.NodeStats;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import ic2.api.energy.EnergyNet;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.api.network.ClientModifiable;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.ContainerFullInv;

public class ContainerMeter extends ContainerFullInv<HandHeldMeter>
{
    private IEnergyTile uut;
    private double resultAvg;
    private double resultMin;
    private double resultMax;
    private int resultCount;
    @ClientModifiable
    private Mode mode;
    
    public ContainerMeter(final EntityPlayer player, final HandHeldMeter meter) {
        super(player, (IInventory)meter, 218);
        this.resultCount = 0;
        this.mode = Mode.EnergyIn;
    }
    
    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.uut == null) {
            return;
        }
        final NodeStats stats = EnergyNet.instance.getNodeStats(this.uut);
        if (stats == null) {
            ((HandHeldMeter)this.base).closeGUI();
            return;
        }
        double result = 0.0;
        switch (this.mode) {
            case EnergyIn: {
                result = stats.getEnergyIn();
                break;
            }
            case EnergyOut: {
                result = stats.getEnergyOut();
                break;
            }
            case EnergyGain: {
                result = stats.getEnergyIn() - stats.getEnergyOut();
                break;
            }
            case Voltage: {
                result = stats.getVoltage();
                break;
            }
        }
        if (this.resultCount == 0) {
            final double resultAvg = result;
            this.resultMax = resultAvg;
            this.resultMin = resultAvg;
            this.resultAvg = resultAvg;
        }
        else {
            if (result < this.resultMin) {
                this.resultMin = result;
            }
            if (result > this.resultMax) {
                this.resultMax = result;
            }
            this.resultAvg = (this.resultAvg * this.resultCount + result) / (this.resultCount + 1);
        }
        ++this.resultCount;
        IC2.network.get(true).sendContainerFields(this, "resultAvg", "resultMin", "resultMax", "resultCount");
    }
    
    public double getResultAvg() {
        return this.resultAvg;
    }
    
    public double getResultMin() {
        return this.resultMin;
    }
    
    public double getResultMax() {
        return this.resultMax;
    }
    
    public int getResultCount() {
        return this.resultCount;
    }
    
    public Mode getMode() {
        return this.mode;
    }
    
    public void setMode(final Mode mode) {
        this.mode = mode;
        IC2.network.get(false).sendContainerField(this, "mode");
        this.reset();
    }
    
    public void reset() {
        if (IC2.platform.isSimulating()) {
            this.resultCount = 0;
        }
        else {
            IC2.network.get(false).sendContainerEvent(this, "reset");
        }
    }
    
    public void setUut(final IEnergyTile uut) {
        assert this.uut == null;
        this.uut = uut;
    }
    
    @Override
    public void onContainerEvent(final String event) {
        super.onContainerEvent(event);
        if ("reset".equals(event)) {
            this.reset();
        }
    }
    
    public enum Mode
    {
        EnergyIn, 
        EnergyOut, 
        EnergyGain, 
        Voltage;
    }
}
