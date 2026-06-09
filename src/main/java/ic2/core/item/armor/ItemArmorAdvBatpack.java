package ic2.core.item.armor;

import ic2.core.ref.Ic2ArmorMaterials;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ItemArmorAdvBatpack extends ItemArmorElectric
{
	public ItemArmorAdvBatpack()
	{
		super(Ic2ArmorMaterials.ADVANCED_BAT_PACK, EquipmentSlot.CHEST, new Properties(), 600000.0, 1000.0, 2);
	}

	@Override
	public boolean canProvideEnergy(ItemStack stack)
	{
		return true;
	}

	@Override
	public int getEnergyPerDamage()
	{
		return 0;
	}
}
