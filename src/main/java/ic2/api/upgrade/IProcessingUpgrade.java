package ic2.api.upgrade;

import net.minecraft.item.ItemStack;

public interface IProcessingUpgrade extends IUpgradeItem {
  int getExtraProcessTime(ItemStack paramItemStack, IUpgradableBlock paramIUpgradableBlock);
  
  double getProcessTimeMultiplier(ItemStack paramItemStack, IUpgradableBlock paramIUpgradableBlock);
  
  int getExtraEnergyDemand(ItemStack paramItemStack, IUpgradableBlock paramIUpgradableBlock);
  
  double getEnergyDemandMultiplier(ItemStack paramItemStack, IUpgradableBlock paramIUpgradableBlock);
}
