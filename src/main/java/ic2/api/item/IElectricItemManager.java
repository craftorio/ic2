// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public interface IElectricItemManager
{
    double charge(final ItemStack p0, final double p1, final int p2, final boolean p3, final boolean p4);
    
    double discharge(final ItemStack p0, final double p1, final int p2, final boolean p3, final boolean p4, final boolean p5);
    
    double getCharge(final ItemStack p0);
    
    double getMaxCharge(final ItemStack p0);
    
    boolean canUse(final ItemStack p0, final double p1);
    
    boolean use(final ItemStack p0, final double p1, final EntityLivingBase p2);
    
    void chargeFromArmor(final ItemStack p0, final EntityLivingBase p1);
    
    String getToolTip(final ItemStack p0);
    
    int getTier(final ItemStack p0);
}
