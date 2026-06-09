package ic2.core.item.armor;

import ic2.core.ref.Ic2ArmorMaterials;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ItemArmorBatpack extends ItemArmorElectric
{
	public ItemArmorBatpack()
	{
		super(Ic2ArmorMaterials.BAT_PACK, EquipmentSlot.CHEST, new Properties(), 60000.0, 100.0, 1);
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
