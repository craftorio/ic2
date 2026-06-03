package ic2.core.ref;

import ic2.core.block.state.IIdProvider;
import ic2.core.util.StackUtil;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.item.ItemStack;

public interface IMultiItem<T extends IIdProvider> {
  ItemStack getItemStack(T paramT);
  
  ItemStack getItemStack(String paramString);
  
  String getVariant(ItemStack paramItemStack);
  
  Set<T> getAllTypes();
  
  default Set<ItemStack> getAllStacks() {
    Set<ItemStack> ret = new HashSet<>();
    for (IIdProvider iIdProvider : getAllTypes())
      ret.add(getItemStack((T)iIdProvider)); 
    ret.remove(null);
    ret.remove(StackUtil.emptyStack);
    return ret;
  }
}
