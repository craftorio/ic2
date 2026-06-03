package ic2.api.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public interface ICustomDamageItem {
  int getCustomDamage(ItemStack paramItemStack);
  
  int getMaxCustomDamage(ItemStack paramItemStack);
  
  void setCustomDamage(ItemStack paramItemStack, int paramInt);
  
  boolean applyCustomDamage(ItemStack paramItemStack, int paramInt, EntityLivingBase paramEntityLivingBase);
}
