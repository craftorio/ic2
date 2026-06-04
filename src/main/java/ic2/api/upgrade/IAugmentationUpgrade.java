package ic2.api.upgrade;

import net.minecraft.item.ItemStack;

public interface IAugmentationUpgrade extends IUpgradeItem
{
	int getAugmentation(ItemStack paramItemStack, IUpgradableBlock paramIUpgradableBlock);
}
