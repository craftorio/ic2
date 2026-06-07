package ic2.api.upgrade;

import java.util.Collection;
import java.util.Set;

import net.minecraft.world.item.ItemStack;

public interface IUpgradeItem
{
	boolean isSuitableFor(ItemStack var1, Set<UpgradableProperty> var2);

	boolean onTick(ItemStack var1, IUpgradableBlock var2);

	Collection<ItemStack> onProcessEnd(ItemStack var1, IUpgradableBlock var2, Collection<ItemStack> var3);
}
