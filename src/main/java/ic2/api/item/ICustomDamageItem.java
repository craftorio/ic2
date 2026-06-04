// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public interface ICustomDamageItem
{
    int getCustomDamage(final ItemStack p0);
    
    int getMaxCustomDamage(final ItemStack p0);
    
    void setCustomDamage(final ItemStack p0, final int p1);
    
    boolean applyCustomDamage(final ItemStack p0, final int p1, final EntityLivingBase p2);
}
