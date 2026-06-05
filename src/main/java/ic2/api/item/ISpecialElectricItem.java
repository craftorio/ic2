package ic2.api.item;

import net.minecraft.item.ItemStack;

public interface ISpecialElectricItem {
   IElectricItemManager getManager(ItemStack var1);
}
