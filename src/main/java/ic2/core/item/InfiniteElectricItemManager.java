package ic2.core.item;

import ic2.api.item.IElectricItemManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class InfiniteElectricItemManager implements IElectricItemManager {
  public double charge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
    return amount;
  }
  
  public double discharge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean externally, boolean simulate) {
    return amount;
  }
  
  public double getCharge(ItemStack stack) {
    return Double.POSITIVE_INFINITY;
  }
  
  public double getMaxCharge(ItemStack stack) {
    return Double.POSITIVE_INFINITY;
  }
  
  public boolean canUse(ItemStack stack, double amount) {
    return true;
  }
  
  public boolean use(ItemStack stack, double amount, EntityLivingBase entity) {
    return true;
  }
  
  public void chargeFromArmor(ItemStack stack, EntityLivingBase entity) {}
  
  public String getToolTip(ItemStack stack) {
    return "infinite EU";
  }
  
  public int getTier(ItemStack stack) {
    return Integer.MAX_VALUE;
  }
}
