package ic2.api.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IElectricItemManager
{
	double charge(ItemStack var1, double var2, int var4, boolean var5, boolean var6);

	double discharge(ItemStack var1, double var2, int var4, boolean var5, boolean var6, boolean var7);

	double getCharge(ItemStack var1);

	double getStackCharge(ItemStack var1);

	double getMaxCharge(ItemStack var1);

	default double getChargeLevel(ItemStack stack)
	{
		return stack.getItem() instanceof IElectricItem ? this.getStackChargeLevel(stack) : 0.0;
	}

	default double getStackChargeLevel(ItemStack stack)
	{
		IElectricItem item = (IElectricItem) stack.getItem();
		assert item.getMaxCharge(stack) > 0.0;
		return Math.max(0.0, Math.min(1.0, this.getStackCharge(stack) / item.getMaxCharge(stack)));
	}

	boolean canUse(ItemStack var1, double var2);

	boolean use(ItemStack var1, double var2, LivingEntity var4);

	void chargeFromArmor(ItemStack var1, LivingEntity var2);

	String getToolTip(ItemStack var1);

	int getTier(ItemStack var1);
}
