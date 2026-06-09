package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.core.item.armor.jetpack.IJetpack;
import ic2.core.ref.Ic2ArmorMaterials;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ItemArmorJetpackElectric extends ItemArmorElectric implements IJetpack
{
	public ItemArmorJetpackElectric()
	{
		super(Ic2ArmorMaterials.JET_PACK, EquipmentSlot.CHEST, new Properties(), 30000.0, 60.0, 1);
	}

	@Override
	public boolean drainEnergy(ItemStack pack, int amount)
	{
		return ElectricItem.manager.discharge(pack, amount + 6, Integer.MAX_VALUE, true, false, false) > 0.0;
	}

	@Override
	public float getPower(ItemStack stack)
	{
		return 0.7F;
	}

	@Override
	public float getDropPercentage(ItemStack stack)
	{
		return 0.05F;
	}

	@Override
	public boolean isJetpackActive(ItemStack stack)
	{
		return true;
	}

	@Override
	public double getChargeLevel(ItemStack stack)
	{
		return ElectricItem.manager.getCharge(stack) / this.getMaxCharge(stack);
	}

	@Override
	public float getHoverMultiplier(ItemStack stack, boolean upwards)
	{
		return 0.1F;
	}

	@Override
	public float getWorldHeightDivisor(ItemStack stack)
	{
		return 1.28F;
	}

	@Override
	public int getEnergyPerDamage()
	{
		return 0;
	}

	@Override
	public double getDamageAbsorptionRatio()
	{
		return 0.0;
	}
}
