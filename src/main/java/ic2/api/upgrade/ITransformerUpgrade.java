package ic2.api.upgrade;

import net.minecraft.item.ItemStack;

public interface ITransformerUpgrade extends IUpgradeItem
{
	int getExtraTier(ItemStack paramItemStack, IUpgradableBlock paramIUpgradableBlock);
}
