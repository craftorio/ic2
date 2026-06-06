package ic2.core.item.armor.jetpack;

import net.minecraft.item.ItemStack;

public interface IJetpack
{
	int EU_ENERGY_INCREASE = 6;

	boolean drainEnergy(ItemStack var1, int var2);

	float getPower(ItemStack var1);

	float getDropPercentage(ItemStack var1);

	double getChargeLevel(ItemStack var1);

	boolean isJetpackActive(ItemStack var1);

	float getHoverMultiplier(ItemStack var1, boolean var2);

	float getWorldHeightDivisor(ItemStack var1);
}
