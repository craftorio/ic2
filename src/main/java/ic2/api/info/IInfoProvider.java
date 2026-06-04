package ic2.api.info;

import net.minecraft.item.ItemStack;

public interface IInfoProvider
{
	double getEnergyValue(ItemStack paramItemStack);

	int getFuelValue(ItemStack paramItemStack, boolean paramBoolean);
}
