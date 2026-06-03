package ic2.core.ref;

import net.minecraft.block.state.IBlockState;

public interface IMultiBlock<T extends ic2.core.block.state.IIdProvider> extends IMultiItem<T> {
  IBlockState getState(T paramT);
  
  IBlockState getState(String paramString);
}
