package ic2.core.item.armor.jetpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IBoostingJetpack extends IJetpack {
  float getBaseThrust(ItemStack paramItemStack, boolean paramBoolean);
  
  float getBoostThrust(EntityPlayer paramEntityPlayer, ItemStack paramItemStack, boolean paramBoolean);
  
  boolean useBoostPower(ItemStack paramItemStack, float paramFloat);
  
  float getHoverBoost(EntityPlayer paramEntityPlayer, ItemStack paramItemStack, boolean paramBoolean);
}
