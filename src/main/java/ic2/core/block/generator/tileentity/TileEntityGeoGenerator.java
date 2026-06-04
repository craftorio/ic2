// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.generator.tileentity;

import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlot;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.Fluid;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.network.GuiSynced;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotConsumableLiquid;

public class TileEntityGeoGenerator extends TileEntityBaseGenerator
{
    private static final int fluidPerTick = 2;
    public final InvSlotConsumableLiquid fluidSlot;
    public final InvSlotOutput outputSlot;
    @GuiSynced
    protected final FluidTank fluidTank;
    protected final Fluids fluids;
    
    public TileEntityGeoGenerator() {
        super(20.0, 1, 2400);
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTankInsert("fluid", 8000, Fluids.fluidPredicate(FluidRegistry.LAVA));
        this.production = Math.round(20.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/geothermal"));
        this.fluidSlot = new InvSlotConsumableLiquidByTank(this, "fluidSlot", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, InvSlotConsumableLiquid.OpType.Drain, (IFluidTank)this.fluidTank);
        this.outputSlot = new InvSlotOutput(this, "output", 1);
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (this.fluidSlot.processIntoTank((IFluidTank)this.fluidTank, this.outputSlot)) {
            this.markDirty();
        }
    }
    
    @Override
    public boolean gainFuel() {
        boolean dirty = false;
        final FluidStack ret = this.fluidTank.drainInternal(2, false);
        if (ret != null && ret.amount >= 2) {
            this.fluidTank.drainInternal(2, true);
            ++this.fuel;
            dirty = true;
        }
        return dirty;
    }
    
    @Override
    public String getOperationSoundFile() {
        return "Generators/GeothermalLoop.ogg";
    }
    
    @Override
    protected void onBlockBreak() {
        super.onBlockBreak();
        FluidEvent.fireEvent((FluidEvent)new FluidEvent.FluidSpilledEvent(new FluidStack(FluidRegistry.LAVA, 1000), this.getWorld(), this.pos));
    }
}
