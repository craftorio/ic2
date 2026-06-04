// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.ref;

import net.minecraft.block.state.IBlockState;
import ic2.core.block.state.IIdProvider;

public interface IMultiBlock<T extends IIdProvider> extends IMultiItem<T>
{
    IBlockState getState(final T p0);
    
    IBlockState getState(final String p0);
}
