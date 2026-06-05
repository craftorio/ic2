package ic2.api.recipe;

import java.util.Map;
import net.minecraft.item.ItemStack;

public interface IScrapboxManager extends IBasicMachineRecipeManager {
   void addDrop(ItemStack var1, float var2);

   ItemStack getDrop(ItemStack var1, boolean var2);

   Map<ItemStack, Float> getDrops();
}
