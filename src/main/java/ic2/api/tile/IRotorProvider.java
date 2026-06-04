// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.tile;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumFacing;

public interface IRotorProvider
{
    int getRotorDiameter();
    
    EnumFacing getFacing();
    
    float getAngle();
    
    ResourceLocation getRotorRenderTexture();
}
