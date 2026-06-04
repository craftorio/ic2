// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import ic2.api.item.IElectricItemManager;

public class InfiniteElectricItemManager implements IElectricItemManager
{
    @Override
    public double charge(final ItemStack stack, final double amount, final int tier, final boolean ignoreTransferLimit, final boolean simulate) {
        return amount;
    }
    
    @Override
    public double discharge(final ItemStack stack, final double amount, final int tier, final boolean ignoreTransferLimit, final boolean externally, final boolean simulate) {
        return amount;
    }
    
    @Override
    public double getCharge(final ItemStack stack) {
        return Double.POSITIVE_INFINITY;
    }
    
    @Override
    public double getMaxCharge(final ItemStack stack) {
        return Double.POSITIVE_INFINITY;
    }
    
    @Override
    public boolean canUse(final ItemStack stack, final double amount) {
        return true;
    }
    
    @Override
    public boolean use(final ItemStack stack, final double amount, final EntityLivingBase entity) {
        return true;
    }
    
    @Override
    public void chargeFromArmor(final ItemStack stack, final EntityLivingBase entity) {
    }
    
    @Override
    public String getToolTip(final ItemStack stack) {
        return "infinite EU";
    }
    
    @Override
    public int getTier(final ItemStack stack) {
        return Integer.MAX_VALUE;
    }
}
