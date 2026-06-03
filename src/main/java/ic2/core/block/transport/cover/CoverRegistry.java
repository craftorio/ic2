package ic2.core.block.transport.cover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.ItemStack;

public class CoverRegistry {
  public static ItemStack register(ItemStack stack) {
    if (!(stack.func_77973_b() instanceof ICoverItem))
      throw new IllegalArgumentException("The stack must represent an ICoverItem."); 
    covers.add(stack);
    return stack;
  }
  
  public static Iterable<ItemStack> getCovers() {
    return Collections.unmodifiableCollection(covers);
  }
  
  private static final List<ItemStack> covers = new ArrayList<>();
}
