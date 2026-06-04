// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.upgrade;

import net.minecraft.item.ItemStack;

public interface ITransformerUpgrade extends IUpgradeItem
{
    int getExtraTier(final ItemStack p0, final IUpgradableBlock p1);
}
