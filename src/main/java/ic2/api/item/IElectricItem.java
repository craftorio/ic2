package ic2.api.item;

import net.minecraft.item.ItemStack;

public interface IElectricItem
{
	boolean canProvideEnergy(ItemStack paramItemStack);

	double getMaxCharge(ItemStack paramItemStack);

	int getTier(ItemStack paramItemStack);

	double getTransferLimit(ItemStack paramItemStack);
}
