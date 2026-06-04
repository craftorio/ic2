// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.steam;

import net.minecraft.util.EnumFacing;

public interface IKineticProvider
{
    int getProvidedPower(final EnumFacing p0);
    
    int getMaxPower();
}
