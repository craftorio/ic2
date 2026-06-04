// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import java.util.Collection;
import java.util.HashSet;
import java.util.Arrays;
import ic2.core.block.IInventorySlotHolder;
import net.minecraftforge.fluids.Fluid;
import java.util.Set;

public class InvSlotConsumableLiquidByList extends InvSlotConsumableLiquid
{
    private final Set<Fluid> acceptedFluids;
    
    public InvSlotConsumableLiquidByList(final IInventorySlotHolder<?> base1, final String name1, final int count, final Fluid... fluidlist) {
        super(base1, name1, count);
        this.acceptedFluids = new HashSet<Fluid>(Arrays.asList(fluidlist));
    }
    
    public InvSlotConsumableLiquidByList(final IInventorySlotHolder<?> base1, final String name1, final Access access1, final int count, final InvSide preferredSide1, final OpType opType, final Fluid... fluidlist) {
        super(base1, name1, access1, count, preferredSide1, opType);
        this.acceptedFluids = new HashSet<Fluid>(Arrays.asList(fluidlist));
    }
    
    @Override
    protected boolean acceptsLiquid(final Fluid fluid) {
        return this.acceptedFluids.contains(fluid);
    }
    
    @Override
    protected Iterable<Fluid> getPossibleFluids() {
        return this.acceptedFluids;
    }
}
