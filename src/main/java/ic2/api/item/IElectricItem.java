package ic2.api.item;

import net.minecraft.item.ItemStack;

public interface IElectricItem
{
	boolean canProvideEnergy(ItemStack var1);

	double getMaxCharge(ItemStack var1);

	int getTier(ItemStack var1);

	double getTransferLimit(ItemStack var1);
}
