// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.transport;

import net.minecraftforge.fluids.FluidTank;

public interface IFluidPipe extends IPipe
{
    int getTransferRate();
    
    FluidTank getTank();
    
    int getCurrentInnerCapacity();
    
    int getMaxInnerCapacity();
}
