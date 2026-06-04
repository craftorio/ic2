// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import java.util.Map;
import net.minecraftforge.fluids.Fluid;

public interface ISemiFluidFuelManager extends ILiquidAcceptManager
{
    void addFluid(final String p0, final long p1, final long p2);
    
    void removeFluid(final String p0);
    
    FuelProperty getFuelProperty(final Fluid p0);
    
    Map<String, FuelProperty> getFuelProperties();
    
    public static final class FuelProperty
    {
        public final long energyPerMb;
        public final long energyPerTick;
        
        public FuelProperty(final long energyPerMb, final long energyPerTick) {
            this.energyPerMb = energyPerMb;
            this.energyPerTick = energyPerTick;
        }
    }
}
