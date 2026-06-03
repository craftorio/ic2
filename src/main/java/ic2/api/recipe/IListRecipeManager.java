package ic2.api.recipe;

import java.util.List;
import net.minecraft.item.ItemStack;

public interface IListRecipeManager extends Iterable<IRecipeInput> {
  void add(IRecipeInput paramIRecipeInput);
  
  boolean contains(ItemStack paramItemStack);
  
  boolean isEmpty();
  
  List<IRecipeInput> getInputs();
}
