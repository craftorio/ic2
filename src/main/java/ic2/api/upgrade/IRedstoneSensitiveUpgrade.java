// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.upgrade;

import net.minecraft.item.ItemStack;

public interface IRedstoneSensitiveUpgrade extends IUpgradeItem
{
    boolean modifiesRedstoneInput(final ItemStack p0, final IUpgradableBlock p1);
    
    int getRedstoneInput(final ItemStack p0, final IUpgradableBlock p1, final int p2);
}
