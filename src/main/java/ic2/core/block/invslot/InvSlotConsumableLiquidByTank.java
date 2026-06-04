// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import java.util.Arrays;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.Fluid;
import ic2.core.block.IInventorySlotHolder;
import net.minecraftforge.fluids.IFluidTank;

public class InvSlotConsumableLiquidByTank extends InvSlotConsumableLiquid
{
    public final IFluidTank tank;
    
    public InvSlotConsumableLiquidByTank(final IInventorySlotHolder<?> base1, final String name1, final Access access1, final int count, final InvSide preferredSide1, final OpType opType, final IFluidTank tank1) {
        super(base1, name1, access1, count, preferredSide1, opType);
        this.tank = tank1;
    }
    
    @Override
    protected boolean acceptsLiquid(final Fluid fluid) {
        final FluidStack fs = this.tank.getFluid();
        return fs == null || fs.getFluid() == fluid;
    }
    
    @Override
    protected Iterable<Fluid> getPossibleFluids() {
        final FluidStack fs = this.tank.getFluid();
        if (fs == null) {
            return null;
        }
        return Arrays.asList(fs.getFluid());
    }
}
