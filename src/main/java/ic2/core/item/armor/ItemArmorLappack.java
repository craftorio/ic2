package ic2.core.item.armor;

import ic2.core.ref.Ic2ArmorMaterials;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class ItemArmorLappack extends ItemArmorElectric
{
	public ItemArmorLappack()
	{
		super(Ic2ArmorMaterials.LAP_PACK, EquipmentSlot.CHEST, new Properties().rarity(Rarity.UNCOMMON), 2.0E7, 2500.0, 4);
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
