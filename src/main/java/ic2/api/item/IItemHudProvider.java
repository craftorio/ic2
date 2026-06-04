// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.item;

import net.minecraft.item.ItemStack;

public interface IItemHudProvider
{
    boolean doesProvideHUD(final ItemStack p0);
    
    HudMode getHudMode(final ItemStack p0);
    
    public interface IItemHudBarProvider
    {
        int getBarPercent(final ItemStack p0);
    }
}
