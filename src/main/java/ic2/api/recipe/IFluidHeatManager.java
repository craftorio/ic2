// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import java.util.Map;
import net.minecraftforge.fluids.Fluid;

public interface IFluidHeatManager extends ILiquidAcceptManager
{
    void addFluid(final String p0, final int p1, final int p2);
    
    BurnProperty getBurnProperty(final Fluid p0);
    
    Map<String, BurnProperty> getBurnProperties();
    
    public static class BurnProperty
    {
        public final int amount;
        public final int heat;
        
        public BurnProperty(final int amount1, final int heat1) {
            this.amount = amount1;
            this.heat = heat1;
        }
    }
}
