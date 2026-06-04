package ic2.api.upgrade;

import net.minecraft.item.ItemStack;

public interface IRemoteAccessUpgrade extends IUpgradeItem
{
	int getRangeAmplification(ItemStack paramItemStack, IUpgradableBlock paramIUpgradableBlock, int paramInt);
}
