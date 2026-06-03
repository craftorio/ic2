package ic2.core.item.armor.jetpack;

import net.minecraft.item.ItemStack;

public interface IJetpack {
  public static final int EU_ENERGY_INCREASE = 6;
  
  boolean drainEnergy(ItemStack paramItemStack, int paramInt);
  
  float getPower(ItemStack paramItemStack);
  
  float getDropPercentage(ItemStack paramItemStack);
  
  double getChargeLevel(ItemStack paramItemStack);
  
  boolean isJetpackActive(ItemStack paramItemStack);
  
  float getHoverMultiplier(ItemStack paramItemStack, boolean paramBoolean);
  
  float getWorldHeightDivisor(ItemStack paramItemStack);
}
