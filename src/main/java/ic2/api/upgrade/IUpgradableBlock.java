// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.upgrade;

import java.util.Set;

public interface IUpgradableBlock
{
    double getEnergy();
    
    boolean useEnergy(final double p0);
    
    Set<UpgradableProperty> getUpgradableProperties();
}
