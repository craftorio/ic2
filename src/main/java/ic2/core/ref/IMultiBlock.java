package ic2.core.ref;

import ic2.core.block.state.IIdProvider;
import net.minecraft.block.state.IBlockState;

public interface IMultiBlock<T extends IIdProvider> extends IMultiItem<T> {
   IBlockState getState(T var1);

   IBlockState getState(String var1);
}
