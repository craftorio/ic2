// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.item;

import net.minecraft.item.ItemStack;

public interface IElectricItem
{
    boolean canProvideEnergy(final ItemStack p0);
    
    double getMaxCharge(final ItemStack p0);
    
    int getTier(final ItemStack p0);
    
    double getTransferLimit(final ItemStack p0);
}
