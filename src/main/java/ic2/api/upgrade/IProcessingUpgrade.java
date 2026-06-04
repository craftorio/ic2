// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.upgrade;

import net.minecraft.item.ItemStack;

public interface IProcessingUpgrade extends IUpgradeItem
{
    int getExtraProcessTime(final ItemStack p0, final IUpgradableBlock p1);
    
    double getProcessTimeMultiplier(final ItemStack p0, final IUpgradableBlock p1);
    
    int getExtraEnergyDemand(final ItemStack p0, final IUpgradableBlock p1);
    
    double getEnergyDemandMultiplier(final ItemStack p0, final IUpgradableBlock p1);
}
