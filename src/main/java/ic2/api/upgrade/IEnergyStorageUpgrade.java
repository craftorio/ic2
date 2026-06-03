package ic2.api.upgrade;

import net.minecraft.item.ItemStack;

public interface IEnergyStorageUpgrade extends IUpgradeItem {
  int getExtraEnergyStorage(ItemStack paramItemStack, IUpgradableBlock paramIUpgradableBlock);
  
  double getEnergyStorageMultiplier(ItemStack paramItemStack, IUpgradableBlock paramIUpgradableBlock);
}
