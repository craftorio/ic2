// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.ref;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IBlockModelProvider
{
    @SideOnly(Side.CLIENT)
    void registerModels(final BlockName p0);
}
