package ic2.api.upgrade;

import net.minecraft.item.ItemStack;

public interface IRedstoneSensitiveUpgrade extends IUpgradeItem {
  boolean modifiesRedstoneInput(ItemStack paramItemStack, IUpgradableBlock paramIUpgradableBlock);
  
  int getRedstoneInput(ItemStack paramItemStack, IUpgradableBlock paramIUpgradableBlock, int paramInt);
}
