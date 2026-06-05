package ic2.core.item.armor.jetpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IBoostingJetpack extends IJetpack {
   float getBaseThrust(ItemStack var1, boolean var2);

   float getBoostThrust(EntityPlayer var1, ItemStack var2, boolean var3);

   boolean useBoostPower(ItemStack var1, float var2);

   float getHoverBoost(EntityPlayer var1, ItemStack var2, boolean var3);
}
