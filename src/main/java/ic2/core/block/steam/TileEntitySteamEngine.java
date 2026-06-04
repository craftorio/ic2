// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.steam;

import net.minecraft.util.EnumFacing;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.ref.FluidName;
import net.minecraftforge.fluids.Fluid;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.TileEntityBlock;
import ic2.core.IC2;
import ic2.core.block.comp.Fluids;
import ic2.core.block.TileEntityInventory;

public class TileEntitySteamEngine extends TileEntityInventory implements IKineticProvider
{
    protected int power;
    protected int delta;
    protected int activityMeter;
    protected int ticksSinceLastActiveUpdate;
    protected final Fluids fluids;
    protected final Fluids.InternalFluidTank fluidTank;
    
    public TileEntitySteamEngine() {
        this.power = 0;
        this.delta = 0;
        this.activityMeter = 0;
        this.ticksSinceLastActiveUpdate = IC2.random.nextInt(128);
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTankInsert("steam", 1000, InvSlot.InvSide.ANY, Fluids.fluidPredicate(FluidName.biomass.getInstance()));
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.delta = nbt.getInteger("delta");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("delta", this.delta);
        return nbt;
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        final boolean needsInventoryUpdate = false;
        final boolean newActive = this.work();
        if (needsInventoryUpdate) {
            this.markDirty();
        }
        if (!this.delayActiveUpdate()) {
            this.setActive(newActive);
        }
        else {
            if (this.ticksSinceLastActiveUpdate % 128 == 0) {
                this.setActive(this.activityMeter > 0);
                this.activityMeter = 0;
            }
            if (newActive) {
                ++this.activityMeter;
            }
            else {
                --this.activityMeter;
            }
            ++this.ticksSinceLastActiveUpdate;
        }
    }
    
    public boolean work() {
        if (this.fluidTank.getFluidAmount() > 1) {
            this.fluidTank.drainInternal(1, true);
            this.delta = Math.min(++this.delta, 200);
            this.power = (int)(this.getMaxPower() / 10.0 * (this.delta / 20));
            return true;
        }
        final int delta = this.delta - 1;
        this.delta = delta;
        this.delta = Math.max(delta, 0);
        this.power = (int)(this.getMaxPower() / 10.0 * (this.delta / 20));
        return false;
    }
    
    public boolean delayActiveUpdate() {
        return false;
    }
    
    @Override
    public int getProvidedPower(final EnumFacing side) {
        return (side == this.getFacing()) ? this.power : 0;
    }
    
    @Override
    public int getMaxPower() {
        return 4;
    }
}
