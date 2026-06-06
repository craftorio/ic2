package ic2.core.item.armor;

import ic2.core.ref.ItemName;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class ItemArmorBatpack extends ItemArmorElectric
{
	public ItemArmorBatpack()
	{
		super(ItemName.batpack, "batpack", EntityEquipmentSlot.CHEST, 60000.0, 100.0, 1);
	}

	@Override
	public boolean canProvideEnergy(ItemStack stack)
	{
		return true;
	}

	@Override
	public double getDamageAbsorptionRatio()
	{
		return 0.0;
	}

	@Override
	public int getEnergyPerDamage()
	{
		return 0;
	}
}
