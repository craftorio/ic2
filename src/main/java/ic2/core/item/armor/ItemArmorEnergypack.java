package ic2.core.item.armor;

import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

@NotClassic
public class ItemArmorEnergypack extends ItemArmorElectric
{
	public ItemArmorEnergypack()
	{
		super(ItemName.energy_pack, "energypack", EntityEquipmentSlot.CHEST, 2000000.0, 1000.0, 3);
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
