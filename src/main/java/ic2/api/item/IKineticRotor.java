// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.item;

import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;

public interface IKineticRotor
{
    int getDiameter(final ItemStack p0);
    
    ResourceLocation getRotorRenderTexture(final ItemStack p0);
    
    float getEfficiency(final ItemStack p0);
    
    int getMinWindStrength(final ItemStack p0);
    
    int getMaxWindStrength(final ItemStack p0);
    
    boolean isAcceptedType(final ItemStack p0, final GearboxType p1);
    
    public enum GearboxType
    {
        WATER, 
        WIND;
    }
}
