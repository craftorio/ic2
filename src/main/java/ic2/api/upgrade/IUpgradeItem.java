// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.upgrade;

import java.util.Collection;
import java.util.Set;
import net.minecraft.item.ItemStack;

public interface IUpgradeItem
{
    boolean isSuitableFor(final ItemStack p0, final Set<UpgradableProperty> p1);
    
    boolean onTick(final ItemStack p0, final IUpgradableBlock p1);
    
    Collection<ItemStack> onProcessEnd(final ItemStack p0, final IUpgradableBlock p1, final Collection<ItemStack> p2);
}
