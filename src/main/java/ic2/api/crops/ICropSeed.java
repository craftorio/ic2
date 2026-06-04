// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.crops;

import net.minecraft.item.ItemStack;

public interface ICropSeed
{
    CropCard getCropFromStack(final ItemStack p0);
    
    void setCropFromStack(final ItemStack p0, final CropCard p1);
    
    int getGrowthFromStack(final ItemStack p0);
    
    void setGrowthFromStack(final ItemStack p0, final int p1);
    
    int getGainFromStack(final ItemStack p0);
    
    void setGainFromStack(final ItemStack p0, final int p1);
    
    int getResistanceFromStack(final ItemStack p0);
    
    void setResistanceFromStack(final ItemStack p0, final int p1);
    
    int getScannedFromStack(final ItemStack p0);
    
    void setScannedFromStack(final ItemStack p0, final int p1);
    
    void incrementScannedFromStack(final ItemStack p0);
}
