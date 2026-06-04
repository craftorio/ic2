// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import java.util.Set;
import net.minecraftforge.fluids.Fluid;

public interface ILiquidAcceptManager
{
    boolean acceptsFluid(final Fluid p0);
    
    Set<Fluid> getAcceptedFluids();
}
