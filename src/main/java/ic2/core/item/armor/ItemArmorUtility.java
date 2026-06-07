package ic2.core.item.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item.Properties;

public class ItemArmorUtility extends ArmorItem
{
	public ItemArmorUtility(ArmorMaterial material, Properties settings, EquipmentSlot type)
	{
		super(material, type, settings);
	}
}
