// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor.jetpack;

import net.minecraft.item.ItemStack;

public interface IJetpack
{
    public static final int EU_ENERGY_INCREASE = 6;
    
    boolean drainEnergy(final ItemStack p0, final int p1);
    
    float getPower(final ItemStack p0);
    
    float getDropPercentage(final ItemStack p0);
    
    double getChargeLevel(final ItemStack p0);
    
    boolean isJetpackActive(final ItemStack p0);
    
    float getHoverMultiplier(final ItemStack p0, final boolean p1);
    
    float getWorldHeightDivisor(final ItemStack p0);
}
