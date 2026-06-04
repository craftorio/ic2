package ic2.api.upgrade;

import java.util.Collection;
import java.util.Set;

import net.minecraft.item.ItemStack;

public interface IUpgradeItem
{
	boolean isSuitableFor(ItemStack paramItemStack, Set<UpgradableProperty> paramSet);

	boolean onTick(ItemStack paramItemStack, IUpgradableBlock paramIUpgradableBlock);

	Collection<ItemStack> onProcessEnd(ItemStack paramItemStack, IUpgradableBlock paramIUpgradableBlock, Collection<ItemStack> paramCollection);
}
