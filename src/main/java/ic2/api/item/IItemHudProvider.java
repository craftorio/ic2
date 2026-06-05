package ic2.api.item;

import net.minecraft.item.ItemStack;

public interface IItemHudProvider {
   boolean doesProvideHUD(ItemStack var1);

   HudMode getHudMode(ItemStack var1);

   interface IItemHudBarProvider {
      int getBarPercent(ItemStack var1);
   }
}
