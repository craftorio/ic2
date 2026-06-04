// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.upgrade;

import net.minecraft.item.ItemStack;

public interface IAugmentationUpgrade extends IUpgradeItem
{
    int getAugmentation(final ItemStack p0, final IUpgradableBlock p1);
}
