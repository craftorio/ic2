package ic2.api.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public interface IElectricItemManager
{
	double charge(ItemStack paramItemStack, double paramDouble, int paramInt, boolean paramBoolean1, boolean paramBoolean2);

	double discharge(ItemStack paramItemStack, double paramDouble, int paramInt, boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3);

	double getCharge(ItemStack paramItemStack);

	double getMaxCharge(ItemStack paramItemStack);

	boolean canUse(ItemStack paramItemStack, double paramDouble);

	boolean use(ItemStack paramItemStack, double paramDouble, EntityLivingBase paramEntityLivingBase);

	void chargeFromArmor(ItemStack paramItemStack, EntityLivingBase paramEntityLivingBase);

	String getToolTip(ItemStack paramItemStack);

	int getTier(ItemStack paramItemStack);
}
