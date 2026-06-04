// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import java.util.Map;
import net.minecraftforge.fluids.Fluid;

public interface ILiquidHeatExchangerManager extends ILiquidAcceptManager
{
    void addFluid(final String p0, final String p1, final int p2);
    
    HeatExchangeProperty getHeatExchangeProperty(final Fluid p0);
    
    Map<String, HeatExchangeProperty> getHeatExchangeProperties();
    
    ILiquidAcceptManager getSingleDirectionLiquidManager();
    
    public static class HeatExchangeProperty
    {
        public final Fluid outputFluid;
        public final int huPerMB;
        
        public HeatExchangeProperty(final Fluid outputFluid, final int huPerMB) {
            this.outputFluid = outputFluid;
            this.huPerMB = huPerMB;
        }
    }
}
