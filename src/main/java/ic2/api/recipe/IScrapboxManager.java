package ic2.api.recipe;

import java.util.Map;
import net.minecraft.item.ItemStack;

public interface IScrapboxManager extends IBasicMachineRecipeManager {
  void addDrop(ItemStack paramItemStack, float paramFloat);
  
  ItemStack getDrop(ItemStack paramItemStack, boolean paramBoolean);
  
  Map<ItemStack, Float> getDrops();
}
