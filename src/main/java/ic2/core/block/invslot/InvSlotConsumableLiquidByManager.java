// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import net.minecraftforge.fluids.Fluid;
import ic2.core.block.IInventorySlotHolder;
import ic2.api.recipe.ILiquidAcceptManager;

public class InvSlotConsumableLiquidByManager extends InvSlotConsumableLiquid
{
    private final ILiquidAcceptManager manager;
    
    public InvSlotConsumableLiquidByManager(final IInventorySlotHolder<?> base1, final String name1, final int count, final ILiquidAcceptManager manager1) {
        super(base1, name1, count);
        this.manager = manager1;
    }
    
    public InvSlotConsumableLiquidByManager(final IInventorySlotHolder<?> base1, final String name1, final Access access1, final int count, final InvSide preferredSide1, final OpType opType, final ILiquidAcceptManager manager1) {
        super(base1, name1, access1, count, preferredSide1, opType);
        this.manager = manager1;
    }
    
    @Override
    protected boolean acceptsLiquid(final Fluid fluid) {
        return this.manager.acceptsFluid(fluid);
    }
    
    @Override
    protected Iterable<Fluid> getPossibleFluids() {
        return this.manager.getAcceptedFluids();
    }
}
