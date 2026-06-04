// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.upgrade;

import net.minecraft.item.ItemStack;

public interface IEnergyStorageUpgrade extends IUpgradeItem
{
    int getExtraEnergyStorage(final ItemStack p0, final IUpgradableBlock p1);
    
    double getEnergyStorageMultiplier(final ItemStack p0, final IUpgradableBlock p1);
}
