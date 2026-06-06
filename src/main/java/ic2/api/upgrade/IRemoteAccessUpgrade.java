package ic2.api.upgrade;

import net.minecraft.item.ItemStack;

public interface IRemoteAccessUpgrade extends IUpgradeItem
{
	int getRangeAmplification(ItemStack var1, IUpgradableBlock var2, int var3);
}
