package ic2.api.info;

import net.minecraft.world.item.ItemStack;

public interface IInfoProvider
{
	double getEnergyValue(ItemStack var1);

	int getFuelValue(ItemStack var1, boolean var2);
}
